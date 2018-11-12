package com.epsagon.executors;

import com.amazonaws.services.lambda.runtime.Context;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * An executor for handlers implementing the
 * {@link com.amazonaws.services.lambda.runtime.RequestHandler} interface.
 */
public class RequestHandlerExecutor extends BasePOJOExecutor {
    /**
     * @param userHandlerClass The class of the user handler.
     * @throws ExecutorException
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
        if (inputType == null || !(inputType instanceof Class)) { // can never occur
            throw new ExecutorException("input type was null or not Class even though class implements RequestHandler");
        }

        try {
            _userHandlerMethod = _userHandlerClass.getMethod(
                    "handleRequest",
                    (Class<?>) inputType,
                    Context.class
            );
        } catch (NoSuchMethodException e) { // can never occur
           throw new ExecutorException("could not find handleRequest method");
        }

    }
}
