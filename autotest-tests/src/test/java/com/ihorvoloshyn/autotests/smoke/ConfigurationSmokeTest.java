package com.ihorvoloshyn.autotests.smoke;

import com.ihorvoloshyn.autotests.core.config.Configuration;
import com.ihorvoloshyn.autotests.core.config.EnvironmentConfigLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationSmokeTest {

    @Test
    void configurationCanBeLoaded() {
        Configuration.load(new EnvironmentConfigLoader());
        assertEquals("TEST", Configuration.get().environment().name());
        assertEquals("http://localhost", Configuration.get().baseUrl());
    }
}
