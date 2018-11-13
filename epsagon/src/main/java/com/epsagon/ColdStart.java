package com.epsagon;

/**
 * Helper class for managing cold start state. A singleton.
 */
public class ColdStart {
    private static ColdStart _instance = new ColdStart();
    private boolean _isColdStart = true;

    /**
     * @return An instance of the ColdStart singleton.
     */
    public static ColdStart getInstance() {
        return _instance;
    }
    /**
     * Reads the cold start state
     * @return The cold start state
     */
    public boolean read() {
        return _isColdStart;
    }

    /**
     * Swithces off the cold start flag
     */
    public void switchOff() {
        _isColdStart = false;
    }

    /**
     * reads the cold start state, and switches it off
     * @return The cold start state.
     */
    public boolean readAndSwitch() {
        try {
            return read();
        } finally {
            switchOff();
        }
    }
}
