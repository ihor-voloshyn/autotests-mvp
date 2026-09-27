package com.ihorvoloshyn.autotests.base;

import com.ihorvoloshyn.autotests.core.config.Configuration;
import com.ihorvoloshyn.autotests.core.config.EnvironmentConfigLoader;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseTest {

    @BeforeAll
    static void loadConfiguration() {
        Configuration.load(new EnvironmentConfigLoader());
    }
}