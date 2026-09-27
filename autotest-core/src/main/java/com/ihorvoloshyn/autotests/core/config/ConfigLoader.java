package com.ihorvoloshyn.autotests.core.config;

import java.util.Map;

public interface ConfigLoader {

    FrameworkConfig load();

    default Map<String, String> loadProperties() {
        return load().properties();
    }
}
