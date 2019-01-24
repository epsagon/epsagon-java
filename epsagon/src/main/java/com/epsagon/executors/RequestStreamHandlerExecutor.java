package com.epsagon.executors;

import com.amazonaws.services.lambda.runtime.Context;
import com.epsagon.TimeHelper;
import com.epsagon.events.EventBuildHelper;
import com.epsagon.events.runners.LambdaRunner;
import com.epsagon.protocol.EventOuterClass;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * An executor for handlers implementing the
 * {@link com.amazonaws.services.lambda.runtime.RequestStreamHandler} interface.
 */
public class RequestStreamHandlerExecutor extends Executor {
    /**
     * @param userHandlerClass The class of the user handler.
     * @throws ExecutorException Raised when executor initialization fails.
     */
    public RequestStreamHandlerExecutor(Class<?> userHandlerClass) throws ExecutorException {
        super(userHandlerClass);
        try {
            _userHandlerMethod = _userHandlerClass.getMethod(
                    "handleRequest",
                    InputStream.class,
                    OutputStream.class,
                    Context.class
            );
        } catch (NoSuchMethodException e) {
            throw new ExecutorException(
                    "no handleRequest implementation found in " +
                            userHandlerClass.getCanonicalName()
            );
        }
    }

    private void finalizeStream(InputStream stream) {
        try {
            while (stream.read() != -1) {
            }
        } catch (IOException e) {
        }
    }

    /**
     * {@inheritDoc}
     */
    public void execute(InputStream input, OutputStream output, Context context) throws Throwable {
        EventOuterClass.Event.Builder runnerBuilder = LambdaRunner.newBuilder(context);
        runnerBuilder.setStartTime(TimeHelper.getCurrentTime());

        ByteArrayOutputStream outputTrack = new ByteArrayOutputStream();
        ByteArrayOutputStream inputTrack = new ByteArrayOutputStream();
        TeeOutputStream teeOutput = new TeeOutputStream(output, outputTrack);
        TeeInputStream teeInput = new TeeInputStream(input, inputTrack);

        try{
           _userHandlerMethod.invoke(_userHandlerObj, teeInput, teeOutput, context);
        } catch (IllegalAccessException | InvocationTargetException e) {
            _trace.addException(e); // we could not invoke the function
            throw e;
        } catch (Throwable e) {
            EventBuildHelper.setException(runnerBuilder, e);
            throw e;
        } finally {
            finalizeStream(teeInput);
            registerTrigger(inputTrack.toString("UTF-8"), context);
            runnerBuilder.getResourceBuilder().putMetadata(
                    "return_value",
                    outputTrack.toString("UTF-8")
            );
            _trace.addEvent(EventBuildHelper.setDuration(runnerBuilder));
        }

    }
}
