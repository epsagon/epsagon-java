package com.epsagon.executors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * A class representing a client's handler executor.
 */
public abstract class Executor {
    protected static Class<?> _userHandlerClass;
    protected static Object _userHandlerObj;
    protected static Method _userHandlerMethod;

    /**
     * @param userHandlerClass The class of the user handler.
     * @throws ExecutorException Raised when executor initialization fails.
     */
    public Executor(Class<?> userHandlerClass) throws ExecutorException {
        _userHandlerClass = userHandlerClass;
        Constructor<?> ctor;
        try {
            ctor = _userHandlerClass.getConstructor();
            _userHandlerObj = ctor.newInstance();
        } catch (Exception e) {
            throw new ExecutorException("Error instancing class" + userHandlerClass.getCanonicalName());
        }

    }

    /**
     * Executes the client handler.
     * @param input The input stream for the Lambda.
     * @param output The output stream for the Lambda.
     * @param context The execution context for the Lambda.
     * @throws Throwable Any error raised by the client execution
     */
    public abstract void execute(InputStream input, OutputStream output, Context context) throws Throwable;

    /**
     * A Factory for creating executors.
     */
    public static class Factory {
        /**
         * Creates an appropriate {@link Executor} from a given entry point.
         * @param entryPoint A string of the form "package.Class::method". If the class implements
         *                   a predefined AWS Lambda interface, doesn't have to include method.
         * @return an appropriate {@link Executor} for the client's handler.
         * @throws ExecutorException Raised when executor initialization fails.
         */
        public Executor createExecutor(String entryPoint) throws ExecutorException {
            String[] wrappedClassComponents = entryPoint.split(":");
            if (wrappedClassComponents.length < 1) {
                throw new ExecutorException("Invalid entry point: " + entryPoint);
            }

            String wrappedClassPath = wrappedClassComponents[0];
            String wrappedHandlerName = (
                    wrappedClassComponents.length > 1 ?
                            wrappedClassComponents[wrappedClassComponents.length - 1] : null
            );

            Class <?> userHandlerClass;
            try {
                userHandlerClass = Class.forName(wrappedClassPath);
            } catch (ClassNotFoundException e) {
                throw new ExecutorException("Class " + wrappedClassPath + " was not found by the loader.");
            }


            if (wrappedHandlerName != null) {
                if (wrappedHandlerName == "handleRequest") {
                    if (RequestStreamHandler.class.isAssignableFrom(userHandlerClass)) {
                        return new RequestStreamHandlerExecutor(userHandlerClass);
                    } else if (RequestHandler.class.isAssignableFrom(userHandlerClass)) {
                        return new RequestHandlerExecutor(userHandlerClass);
                    }
                }
                return new POJOExecutor(userHandlerClass, wrappedHandlerName);
            } else if (RequestStreamHandler.class.isAssignableFrom(userHandlerClass)) {
                return new RequestStreamHandlerExecutor(userHandlerClass);
            } else if (RequestHandler.class.isAssignableFrom(userHandlerClass)) {
                return new RequestHandlerExecutor(userHandlerClass);
            } else {
                // no function name provided, and the class implements no interface
                throw new ExecutorException("No handler name give, and no interface implemented");
            }
        }
    }
}
