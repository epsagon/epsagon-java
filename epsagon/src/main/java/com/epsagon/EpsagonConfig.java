package com.epsagon;

import java.util.Optional;

/**
 * Epsagon's configuration object. a singleton.
 */
public class EpsagonConfig {
    // Breaking the builder pattern for usability here
    private String _token = System.getenv("EPSAGON_TOKEN");
    private String _appName = Optional
            .ofNullable(System.getenv("EPSAGON_APP_NAME"))
            .orElse("Epsagon Application");
    private String _traceCollectorURL = Optional
            .ofNullable(System.getenv("EPSAGON_TRACE_COLLECTOR_URL"))
            .orElse("https://" + Region.getRegion() + ".tc.epsagon.com");
    private boolean _metadataOnly = false;

    private static EpsagonConfig _instance = new EpsagonConfig();

    /**
     * @return returns a reference to the singleton.
     */
    public static EpsagonConfig getInstance() {
        return _instance;
    }

    /**
     * @return The currently configured token.
     */
    public String getToken() {
        return _token;
    }

    /**
     * Sets the currently configured token.
     * @param token The new token to set.
     * @return A reference to the config object.
     */
    public synchronized EpsagonConfig setToken(String token) {
        _token = token;
        return this;
    }

    /**
     * @return The current configured application name.
     */
    public String getAppName() {
        return _appName;
    }

    /**
     * Sets the currently configured application name.
     * @param appName The new application name to set.
     * @return A reference to the config object.
     */
    public synchronized EpsagonConfig setAppName(String appName) {
        _appName = appName;
        return this;
    }

    /**
     * @return The current configured metadata only flag.
     */
    public boolean isMetadataOnly() {
        return _metadataOnly;
    }

    /**
     * Changes the metadata only configuration
     * @param metadataOnly True if the instrumentation should send only metadata, False otherwise.
     * @return A reference to the config object.
     */
    public synchronized EpsagonConfig setMetadataOnly(boolean metadataOnly) {
        _metadataOnly = metadataOnly;
        return this;
    }

    /**
     * @return The current configured trace collector URL.
     */
    public String getTraceCollectorURL() {
        return _traceCollectorURL;
    }

    /**
     * Sets the currently configured trace collector URL.
     * @param traceCollectorURL The new trace collector URL to set.
     * @return A reference to the config object.
     */
    public synchronized EpsagonConfig setTraceCollectorURL(String traceCollectorURL) {
        _traceCollectorURL = traceCollectorURL;
        return this;
    }

}
