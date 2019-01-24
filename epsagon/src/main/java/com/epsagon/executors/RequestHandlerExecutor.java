package com.epsagon.executors;

import com.amazonaws.services.lambda.runtime.Context;
import com.epsagon.TimeHelper;
import com.epsagon.events.EventBuildHelper;
import com.epsagon.events.runners.LambdaRunner;
import com.epsagon.events.triggers.TriggerFactory;
import com.epsagon.protocol.EventOuterClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * An executor for handlers implementing the
 * {@link com.amazonaws.services.lambda.runtime.RequestHandler} interface.
 */
public class RequestHandlerExecutor extends BasePOJOExecutor {
    /**
     * @param userHandlerClass The class of the user handler.
     * @throws ExecutorException Raised when executor initialization fails.
     */
    public RequestHandlerExecutor(Class<?> userHandlerClass) throws ExecutorException {
        super(userHandlerClass);

        Type inputType = null;
        Type[] interfaces = _userHandlerClass.getGenericInterfaces();
        for (Type genericInterface : interfaces) {
            // TODO make sure its the right interface
            if (genericInterface instanceof ParameterizedType) {
                inputType = ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
                break;
            }
        }
        if (inputType == null) {
            throw new ExecutorException("input type was null or not Class even though class implements RequestHandler");
        }

        Class<?> inputClass;
        if (inputType instanceof Class) {
            inputClass = (Class<?>) inputType;
        } else {

            inputClass = (Class<?>) ((ParameterizedType) inputType).getRawType();
        }

        try {
            _userHandlerMethod = _userHandlerClass.getMethod(
                "handleRequest",
                inputClass,
                Context.class
            );
        } catch (NoSuchMethodException e) { // can never occur
           throw new ExecutorException("could not find handleRequest method");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void execute(InputStream input, OutputStream output, Context context) throws Throwable {
        EventOuterClass.Event.Builder runnerBuilder = LambdaRunner.newBuilder(context);
        runnerBuilder.setStartTime(TimeHelper.getCurrentTime());

        // Not trying and catching here. If malformed input was given we should explode.
        Object realInput = handleInput(input, context);

        try {
            Object result = _userHandlerMethod.invoke(_userHandlerObj, realInput, context);
            handleResult(output, runnerBuilder, result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            _trace.addException(e); // we could not invoke the function
            throw e;
        } catch (Throwable e) {
            EventBuildHelper.setException(runnerBuilder, e);
            throw e;
        } finally {
            _trace.addEvent(EventBuildHelper.setDuration(runnerBuilder));
        }
    }

}
