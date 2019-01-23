package com.epsagon.executors;

import com.amazonaws.services.lambda.runtime.Context;
import com.epsagon.TimeHelper;
import com.epsagon.events.EventBuildHelper;
import com.epsagon.events.runners.LambdaRunner;
import com.epsagon.protocol.EventOuterClass;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An executor for client request that don't implement an AWS interface, POJO requests.
 */
public class POJOExecutor extends BasePOJOExecutor {

    /**
     * @param userHandlerClass The class of the user handler.
     * @param handlerName The name of the method for the user handler.
     * @throws ExecutorException Raised when executor initialization fails.
     */
    public POJOExecutor(Class<?> userHandlerClass, String handlerName) throws ExecutorException {
        super(userHandlerClass);
        Method candidate = null;
        for (Method m : _userHandlerClass.getMethods()) {
            if (m.getName().equals(handlerName)) {
                Class<?>[] parameterTypes = m.getParameterTypes();
                if (
                    (candidate == null) ||
                    (m.getParameterCount() > candidate.getParameterCount()) ||
                    (
                        m.getParameterCount() == candidate.getParameterCount() &&
                            parameterTypes.length > 0 &&
                            parameterTypes[0] != Object.class && // can't be object, has to be JSON serializable.
                            parameterTypes[parameterTypes.length - 1] == Context.class
                    )
                ) {
                    System.out.println("Switching candidate: " + candidate + " with m: " + m);
                    candidate = m;
                }
            }
        }

        if (candidate == null) {
            throw new ExecutorException(
                "No appropriate function with the name " +
                        handlerName +
                        " was found in " +
                        userHandlerClass.getCanonicalName()
            );
        }
        _userHandlerMethod = candidate;
    }

    public void execute(InputStream input, OutputStream output, Context context) throws Throwable {
        EventOuterClass.Event.Builder runnerBuilder = LambdaRunner.newBuilder(context);
        runnerBuilder.setStartTime(TimeHelper.getCurrentTime());

        try {
            Object realInput = parseInput(input, context);
            Object result = null;
            switch (_userHandlerMethod.getParameterCount()) {
                case 0:
                    // no parameters
                    result = _userHandlerMethod.invoke(_userHandlerObj);
                    break;
                case 1:
                    // only context or event
                    if (_userHandlerMethod.getParameterTypes()[0] == Context.class) {
                        result = _userHandlerMethod.invoke(_userHandlerObj, context);
                    } else {
                        result = _userHandlerMethod.invoke(_userHandlerObj, realInput);
                    }
                    break;
                case 2:
                    // both event and context
                    result = _userHandlerMethod.invoke(_userHandlerObj, realInput, context);
                    break;
            }

            handleResult(output, runnerBuilder, result);

        } catch (IllegalAccessException | InvocationTargetException e) {
            _trace.addException(e);
            throw e;
        } catch (Throwable e) {
            EventBuildHelper.setException(runnerBuilder, e);
            throw e;
        } finally {
            _trace.addEvent(EventBuildHelper.setDuration(runnerBuilder));
        }

    }
}
