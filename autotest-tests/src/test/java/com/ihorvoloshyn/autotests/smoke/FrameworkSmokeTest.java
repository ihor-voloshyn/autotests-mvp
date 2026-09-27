package com.ihorvoloshyn.autotests.smoke;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameworkSmokeTest {

    @Test
    void frameworkVersionIsDefined() {
        assertEquals(
                "0.1.0-SNAPSHOT",
                com.ihorvoloshyn.autotests.core.FrameworkVersion.VERSION
        );
    }
}
