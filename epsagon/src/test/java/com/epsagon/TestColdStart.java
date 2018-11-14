package com.epsagon;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;


class TestColdStart {

    private ColdStart _coldStart = new ColdStart();

    @BeforeEach
    void resetColdStart() {
        _coldStart = new ColdStart();

    }

    @Test
    @DisplayName("First read should be true")
    void firstRead() {
        assertTrue(_coldStart.read());
    }

    @Test
    @DisplayName("Read after switch off should be false")
    void switchOff() {
        assertTrue(_coldStart.read());
        _coldStart.switchOff();
        assertFalse(_coldStart.read());

    }

    @Test
    @DisplayName("Read after read & switch off should be false")
    void readAndSwitch() {
        assertTrue(_coldStart.readAndSwitch());
        assertFalse(_coldStart.read());
    }

    @Test
    @DisplayName("Read & switch after read & switch should be false")
    void readAndSwitchTwice() {
        assertTrue(_coldStart.readAndSwitch());
        assertFalse(_coldStart.readAndSwitch());
    }

    @Test
    @DisplayName("Test actual singleton")
    void testSingleton() {
        ColdStart coldstart = ColdStart.getInstance();
        assertTrue(coldstart.read());
        assertTrue(coldstart.readAndSwitch());
        assertFalse(coldstart.readAndSwitch());
        assertFalse(coldstart.read());
        coldstart.switchOff();
        assertFalse(coldstart.readAndSwitch());
        assertFalse(coldstart.read());
    }
}