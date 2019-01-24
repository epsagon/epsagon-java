package com.epsagon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.epsagon.executors.Executor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.amazonaws.services.lambda.runtime.Context;

/**
 * This is the base class used to create Lambdas wrapped with Epsagon.
 * Set this class as the entry class to your program or extend it
 * with a static initializer that calls {@link #init(String)}}
 */
public class EpsagonRequestHandler implements RequestStreamHandler {
    private static final Logger _LOG = LogManager.getLogger(EpsagonRequestHandler.class);
    private static final Trace _trace = Trace.getInstance();
    private static Executor _executor;

    static {
        String epsagonEntryPoint = System.getenv("EPSAGON_ENTRY_POINT");
        if (epsagonEntryPoint != null) {
            try {
                init(epsagonEntryPoint);
            } catch (EpsagonException e) {
                _LOG.error("Could not find class: " + epsagonEntryPoint + ". Please validate the path.");
            }
        }
    }

    /**
     * Initialized Epsagon. Installs the instrumentation agent and set's the client entry point.
     * @param entryPoint The binary name of the user's entry point to the Lambda
     * @return A reference to {@link EpsagonConfig} singleton for the user to configure.
     * @throws EpsagonException Raised for any error in creating the user's handler.
     */
    public static EpsagonConfig init(String entryPoint) throws EpsagonException {
        Installer.install();
        Executor.Factory executorFactory = new Executor.Factory();
        _executor = executorFactory.createExecutor(entryPoint);
        return EpsagonConfig.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void handleRequest(
            InputStream input,
            OutputStream output,
            Context context
    ) throws IOException {
        _trace.reset();

        try {
            _executor.execute(input, output, context);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new RuntimeException(e); // TODO: handle this better.
        } finally {
            _trace.send();
        }
    }
}
