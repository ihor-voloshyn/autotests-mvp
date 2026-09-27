package com.ihorvoloshyn.autotests.core.config;

public final class Configuration {

    private static volatile FrameworkConfig current = FrameworkConfig.defaults();

    private Configuration() {
    }

    public static FrameworkConfig get() {
        return current;
    }

    public static void load(ConfigLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException("ConfigLoader must not be null");
        }
        current = loader.load();
    }
}
