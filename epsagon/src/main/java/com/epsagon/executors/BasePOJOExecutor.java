package com.epsagon.executors;

import com.amazonaws.services.lambda.runtime.Context;
import com.epsagon.Patcher;
import com.epsagon.protocol.EventOuterClass;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLClassLoader;

/**
 * An executor that cast the input to a POJO before executing the client request,
 * and gets a POJO as a response from the client request.
 */
public abstract class BasePOJOExecutor extends Executor {
    private URLClassLoader runtimeCL = null;
    private Class<?> eventConfiguration = null;
    private Class<?> jacksonFactory =  null;
    private Method isLambdaSupportedEvent = null;
    private Method getEventSerializerFor = null;
    private Method getInstance = null;
    private Method jacksonGetSerializer = null;

    /**
     * @param userHandlerClass The class of the user handler.
     * @throws ExecutorException Raised when executor initialization fails.
     */
    public BasePOJOExecutor(Class<?> userHandlerClass) throws ExecutorException {
        super(userHandlerClass);
        try {
            for (Class clazz : Patcher.instrumentation.getAllLoadedClasses()) {
                if (clazz.getName().endsWith("AWSLambda")) {
                    runtimeCL = (URLClassLoader) clazz.getClassLoader();
                    break;
                }
            }
            eventConfiguration = runtimeCL.loadClass("lambdainternal.events.EventConfiguration");
            jacksonFactory = runtimeCL.loadClass("lambdainternal.serializerfactories.JacksonFactory");
            isLambdaSupportedEvent = eventConfiguration.getMethod("isLambdaSupportedEvent", String.class);
            getEventSerializerFor = eventConfiguration.getMethod("getEventSerializerFor", Class.class);
            getInstance = jacksonFactory.getMethod("getInstance");
            jacksonGetSerializer = jacksonFactory.getMethod("getSerializer", Type.class);
        } catch (Throwable t) {
            _trace.addException(t);
        }
    }


    /**
     * Parses the user's input to an object
     * @param input The user's input stream
     * @return The input as an object
     */
    protected Object parseInput(InputStream input) throws IOException {
        Type inputType = _userHandlerMethod.getParameterTypes()[0];
        try {
            Object serializer = getSerializer(inputType);
            Method fromJson = serializer.getClass().getMethod("fromJson", InputStream.class);
            fromJson.setAccessible(true);
            Object result = fromJson.invoke(serializer, input);
            fromJson.setAccessible(false);
            return result;
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (cause instanceof UncheckedIOException) {
                throw ((UncheckedIOException) cause).getCause();
            }
            _trace.addException(t);
            return null;
        }
    }

    /**
     * Gets the appropriate serializer
     * @param inputType the type to serialize
     * @return an appropriate serializer
     * @throws Throwable
     */
    protected Object getSerializer(Type inputType) throws Throwable {
          if (inputType instanceof Class) {
              Class<Object> clazz = (Class) inputType;
              if ((boolean) isLambdaSupportedEvent.invoke(null, clazz.getName())) {
                  return getEventSerializerFor.invoke(
                      null,
                      clazz
                  );
              }
          }
          Object jacksonInstance = getInstance.invoke(null);
          return jacksonGetSerializer.invoke(jacksonInstance, inputType);
    }


    /**
     * Handles the user's input object and registers the trigger
     * @param input The input stream
     * @param context The lambda's context
     * @return The user's deserialized input
     * @throws IOException
     */
    protected Object handleInput(InputStream input, Context context) throws IOException {
        Object realInput = parseInput(input);
        registerTrigger(realInput, context);
        return realInput;
    }


    /**
     * Serializes and writes the execution result to the trace and the output
     * @param output The output stream
     * @param runnerBuilder The runner event builder
     * @param result The result we got
     */
    protected void handleResult(
            OutputStream output,
            EventOuterClass.Event.Builder runnerBuilder,
            Object result
    ) throws IOException {
        Type outputType = _userHandlerMethod.getReturnType();
        try {
            Object serializer = getSerializer(outputType);
            Method toJson = serializer.getClass().getMethod("toJson", Object.class, OutputStream.class);
            toJson.setAccessible(true);
            toJson.invoke(serializer, result, output);

            ByteArrayOutputStream returnValueStream = new ByteArrayOutputStream();
            toJson.invoke(serializer, result, returnValueStream);
            toJson.setAccessible(false);
            runnerBuilder.getResourceBuilder().putMetadata(
                "return_value",
                new String(returnValueStream.toByteArray())
            );
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (cause instanceof UncheckedIOException) {
                throw ((UncheckedIOException) cause).getCause();
            }
            _trace.addException(t);
        }
    }
}
