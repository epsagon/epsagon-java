package com.epsagon.executors;

import com.amazonaws.services.lambda.runtime.Context;
import com.epsagon.events.triggers.TriggerFactory;
import com.epsagon.protocol.EventOuterClass;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * An executor that cast the input to a POJO before executing the client request,
 * and gets a POJO as a response from the client request.
 */
public abstract class BasePOJOExecutor extends Executor {
    private static final ObjectMapper _objectMapper = new ObjectMapper()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .registerModule(new JodaModule());

    /**
     * @param userHandlerClass The class of the user handler.
     * @throws ExecutorException Raised when executor initialization fails.
     */
    public BasePOJOExecutor(Class<?> userHandlerClass) throws ExecutorException {
        super(userHandlerClass);
    }

    /**
     * Parses the user's input object and registers the trigger
     * @param input The input stream
     * @param context The lambda's context
     * @return The user's deserialized input
     * @throws IOException
     */
    protected Object parseInput(InputStream input, Context context) throws IOException {
        Object realInput = null;
        // Input only exists if there are more then zero parameters, and the first
        // parameter isn't a context object
        if (
            _userHandlerMethod.getParameterCount() > 0 &&
            _userHandlerMethod.getParameterTypes()[0] != Context.class
        ) {
            Type inputType = _userHandlerMethod.getParameterTypes()[0];
            // Not trying and catching here. If malformed input was given we should explode.
            realInput = _objectMapper.readValue(input, (Class<?>) inputType);

        }

        try {
            _trace.addEvent(
                    TriggerFactory.newBuilder(realInput, context)
            );
        } catch (Exception e) {
            _trace.addException(e);
        }

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
