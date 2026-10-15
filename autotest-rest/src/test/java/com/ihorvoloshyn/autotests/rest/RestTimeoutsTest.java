package com.ihorvoloshyn.autotests.rest;

import com.ihorvoloshyn.autotests.core.config.Environment;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RestTimeoutsTest {

    @Test
    void usesDefaultsWhenTimeoutPropertiesAreMissing() {
        var timeouts = RestTimeouts.from(FrameworkConfig.defaults());

        assertEquals(5_000, timeouts.connectionRequestMillis());
        assertEquals(5_000, timeouts.connectMillis());
        assertEquals(10_000, timeouts.responseMillis());
    }

    @Test
    void readsAllTimeoutsFromConfiguration() {
        var config = new FrameworkConfig(Environment.TEST, "", "", "", Map.of(
                RestTimeouts.CONNECTION_REQUEST_PROPERTY, "1200",
                RestTimeouts.CONNECT_PROPERTY, "2300",
                RestTimeouts.RESPONSE_PROPERTY, "4500"));

        var timeouts = RestTimeouts.from(config);

        assertEquals(1200, timeouts.connectionRequestMillis());
        assertEquals(2300, timeouts.connectMillis());
        assertEquals(4500, timeouts.responseMillis());
    }

    @Test
    void rejectsNonNumericTimeoutConfiguration() {
        var config = new FrameworkConfig(Environment.TEST, "", "", "", Map.of(
                RestTimeouts.CONNECT_PROPERTY, "soon"));

        assertThrows(IllegalArgumentException.class, () -> RestTimeouts.from(config));
    }

    @Test
    void rejectsZeroOrNegativeTimeouts() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new RestTimeouts(0, 1, 1)),
                () -> assertThrows(IllegalArgumentException.class, () -> new RestTimeouts(1, -1, 1)),
                () -> assertThrows(IllegalArgumentException.class, () -> new RestTimeouts(1, 1, 0)));
    }

    @Test
    void rejectsNullFrameworkConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> RestTimeouts.from(null));
    }
}
