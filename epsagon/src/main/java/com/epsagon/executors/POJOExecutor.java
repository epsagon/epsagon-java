package com.epsagon.executors;

import com.amazonaws.services.lambda.runtime.Context;
import com.epsagon.TimeHelper;
import com.epsagon.events.EventBuildHelper;
import com.epsagon.events.runners.LambdaRunner;
import com.epsagon.protocol.EventOuterClass;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * An executor for client request that don't implement an AWS interface, POJO requests.
 */
public class POJOExecutor extends BasePOJOExecutor {
    private static final Logger _LOG = LogManager.getLogger(POJOExecutor.class);

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

    /**
     * {@inheritDoc}
     */
    public void execute(InputStream input, OutputStream output, Context context) throws Throwable {
        EventOuterClass.Event.Builder runnerBuilder = LambdaRunner.newBuilder(context);
        runnerBuilder.setStartTime(TimeHelper.getCurrentTime());

        try {
            Object realInput = handleInput(input, context);
            Object result = null;
            switch (_userHandlerMethod.getParameterCount()) {
                case 0:
                    // no parameters
                    _LOG.debug(
                            "[Epsagon] Invoking original handler method {} with no arguments.",
                            _userHandlerMethod);
                    result = _userHandlerMethod.invoke(_userHandlerObj);
                    break;
                case 1:
                    // only context or event
                    if (_userHandlerMethod.getParameterTypes()[0] == Context.class) {
                        _LOG.debug(
                                "[Epsagon] Invoking original handler method {} with the following arguments: " +
                                        "context: {}",
                                _userHandlerMethod,
                                context);
                        result = _userHandlerMethod.invoke(_userHandlerObj, context);
                    } else {
                        _LOG.debug(
                                "[Epsagon] Invoking original handler method {} with the following arguments: " +
                                        "input: {}",
                                _userHandlerMethod, realInput);
                        result = _userHandlerMethod.invoke(_userHandlerObj, realInput);
                    }
                    break;
                case 2:
                    // both event and context
                    _LOG.debug(
                            "[Epsagon] Invoking original handler method {} with the following arguments: " +
                                    "input: {}, context: {}",
                            _userHandlerMethod, realInput, context);
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


    /**
     * Handles the user's input object and registers the trigger
     * @param input The input stream
     * @param context The lambda's context
     * @return The user's deserialized input
     * @throws IOException
     */
    protected Object handleInput(InputStream input, Context context) throws IOException {
        // Input only exists if there are more then zero parameters, and the first
        // parameter isn't a context object
        Type[] parameterTypes = _userHandlerMethod.getParameterTypes();
        _LOG.debug("[Epsagon] User handler method {} has the following parameter types: {}",
                _userHandlerMethod,
                parameterTypes);
        if (
            parameterTypes.length > 0 &&
            parameterTypes[0] != Context.class &&
            parameterTypes[0] != InputStream.class
        ) {
            _LOG.debug("[Epsagon] Parameter types are valid");
            return super.handleInput(input, context);
        }

        _LOG.debug("[Epsagon] Returning null input because of invalid parameter types");
        registerTrigger(
            IOUtils.toString(input, "UTF-8"),
            context
        );
        return null;
    }
}
