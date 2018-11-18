package com.epsagon.executors;

import com.amazonaws.services.lambda.runtime.Context;
import com.epsagon.TimeHelper;
import com.epsagon.Trace;
import com.epsagon.events.EventBuildHelper;
import com.epsagon.events.runners.LambdaRunner;
import com.epsagon.events.triggers.TriggerFactory;
import com.epsagon.protocol.EventOuterClass;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
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
     * {@inheritDoc}
     */
    public void execute(InputStream input, OutputStream output, Context context) throws Throwable {
        Trace trace = Trace.getInstance();

        EventOuterClass.Event.Builder runnerBuilder = LambdaRunner.newBuilder(context);
        runnerBuilder.setStartTime(TimeHelper.getCurrentTime());
        Type inputType = _userHandlerMethod.getParameterTypes()[0];

        // Not trying and catching here. If malformed input was given we should explode.
        Object realInput = _objectMapper.readValue(input, (Class<?>) inputType);

        try {
            trace.addEvent(
                    TriggerFactory.newBuilder(realInput, context)
            );
        } catch (Exception e) {
            trace.addException(e);
        }

        try {
            Object result = _userHandlerMethod.invoke(_userHandlerObj, realInput, context);
            Type outputType = _userHandlerMethod.getReturnType();
            _objectMapper.writeValue(output, ((Class) outputType).cast(result));
            runnerBuilder.getResourceBuilder().putMetadata(
                    "return_value",
                    _objectMapper.writeValueAsString(output)
            );
        } catch (IllegalAccessException | InvocationTargetException e) {
            trace.addException(e); // we could not invoke the function
            throw e;
        } catch (Throwable e) {
            EventBuildHelper.setException(runnerBuilder, e);
            throw e;
        } finally {
            trace.addEvent(EventBuildHelper.setDuration(runnerBuilder));
        }
    }
}
