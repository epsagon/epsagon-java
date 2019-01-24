package com.epsagon.executors;

import com.amazonaws.services.lambda.runtime.Context;
import com.epsagon.protocol.EventOuterClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * An executor that cast the input to a POJO before executing the client request,
 * and gets a POJO as a response from the client request.
 */
public abstract class BasePOJOExecutor extends Executor {

    /**
     * @param userHandlerClass The class of the user handler.
     * @throws ExecutorException Raised when executor initialization fails.
     */
    public BasePOJOExecutor(Class<?> userHandlerClass) throws ExecutorException {
        super(userHandlerClass);
    }

    /**
     * Parses the user's input to an object
     * @param input The user's input stream
     * @return The input as an object
     * @throws IOException
     */
    protected Object parseInput(InputStream input) throws IOException {
        Type inputType = _userHandlerMethod.getParameterTypes()[0];

        // Not trying and catching here. If malformed input was given we should explode.
        return _objectMapper.readValue(input, (Class<?>) inputType);
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
     * @throws IOException
     */
    protected void handleResult(
            OutputStream output,
            EventOuterClass.Event.Builder runnerBuilder,
            Object result
    ) throws IOException {
        Type outputType = _userHandlerMethod.getReturnType();
        _objectMapper.writeValue(output, ((Class) outputType).cast(result));
        runnerBuilder.getResourceBuilder().putMetadata(
                "return_value",
                _objectMapper.writeValueAsString(result)
        );
    }
}
