package com.epsagon;

import com.epsagon.protocol.EventOuterClass;
import com.epsagon.protocol.ExceptionOuterClass;
import com.epsagon.protocol.TraceOuterClass;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import com.google.protobuf.util.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The trace, collecting the execution events and reports them when the execution is done.
 * a singleton.
 */
public class Trace {
    private static final int SEND_TIMEOUT_MILLISECONDS = 5000;
    private static final Logger _LOG = LogManager.getLogger(EpsagonRequestHandler.class);
    private static Trace _instance = new Trace();
    private TraceOuterClass.Trace.Builder _core = TraceOuterClass.Trace.newBuilder();

    // _config is ALIASED, so it is READ ONLY for this class
    private EpsagonConfig _config = EpsagonConfig.getInstance();

    /**
     * @return Reference to the trace instance.
     */
    public static Trace getInstance() {
        return _instance;
    }

    /**
     * Resets the trace (clears out all the events and exceptions).
     */
    public synchronized void reset() {
        _core = TraceOuterClass.Trace.newBuilder()
                .setPlatform("java " + System.getProperty("java.version"))
                .setVersion(_config.getVersion());
    }

    /**
     * Adds an event to the trace, if not null.
     * @param event The event to add.
     */
    public synchronized void addEvent(EventOuterClass.Event event) {
        if (event != null) {
            _core.addEvents(event);
        }
    }

    /**
     * Adds an event to the trace from a builder, if not null.
     * @param eventBuilder The builder of the event to add.
     */
    public synchronized void addEvent(EventOuterClass.Event.Builder eventBuilder) {
        if (eventBuilder != null) {
            _core.addEvents(eventBuilder.build());
        }
    }

    /**
     * Adds an exception to the trace.
     * @param e The exception to add
     */
    public synchronized void addException(Throwable e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        _core.addExceptions(
            ExceptionOuterClass.Exception.newBuilder()
                .setType(e.getClass().toString())
                .setMessage(Optional.ofNullable(e.getMessage()).orElse(""))
                .setTraceback(stackTrace.toString())
                .setTime(TimeHelper.getCurrentTime())
        );
    }

    /**
     * Adds an exception to the trace, from an {@link ExceptionOuterClass.Exception} object.
     * @param e The exception to add
     */
    public synchronized void addException(ExceptionOuterClass.Exception e) {
        _core.addExceptions(e);
    }

    /**
     * Adds an exception to the trace, from an {@link ExceptionOuterClass.Exception} object builder.
     * @param e The builder of the exception to add
     */
    public synchronized void addException(ExceptionOuterClass.Exception.Builder e) {
        _core.addExceptions(e.build());
    }

    /**
     * Sends the trace.
     */
    public void send() {
        _LOG.trace("sending trace");
        if (_core == null) {
            _LOG.error("Trace must be restarted before sending.");
            return;
        }
        if (_core.getToken() == null) {
            _LOG.error("Epsagon token not set. A trace won't be sent.");
            return;
        }

        _core.setToken(_config.getToken()).setAppName(_config.getAppName());

        try {
            URL tc = new URL(_config.getTraceCollectorURL());
            HttpURLConnection con = (HttpURLConnection) tc.openConnection();
            try {
                con.setRequestMethod("POST");
                con.setConnectTimeout(SEND_TIMEOUT_MILLISECONDS);
                con.setReadTimeout(SEND_TIMEOUT_MILLISECONDS);
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
                try {
                    String traceJson = JsonFormat.printer()
                            .includingDefaultValueFields()
                            .preservingProtoFieldNames()
                            .printingEnumsAsInts()
                            .print(_core);
                    _LOG.debug(
                    "trace JSON:" +
                        traceJson +
                        "\n collector URL: " +
                        _config.getTraceCollectorURL()
                    );
                    writer.write(traceJson);
                    writer.flush();
                    _LOG.debug("Response code: " + con.getResponseCode());
                } finally {
                    writer.close();
                }
            } finally {
                con.disconnect();
            }
        } catch (IOException e) {
            _LOG.error(_config.getTraceCollectorURL());
            _LOG.error("Cannot connect to Trace collector URL. Cannot report to Epsagon.");
            _LOG.error(e.getMessage());
        }
    }
}
