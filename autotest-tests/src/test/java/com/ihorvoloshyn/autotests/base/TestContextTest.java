package com.ihorvoloshyn.autotests.base;

import com.ihorvoloshyn.autotests.core.config.Environment;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestContextTest {

    @Test
    void contextCreationDoesNotAuthenticateWithVault() {
        FrameworkConfig config = new FrameworkConfig(
                Environment.TEST,
                "http://localhost:8080",
                "http://localhost:8081",
                "http://127.0.0.1:1",
                Map.of(
                        "vault.url", "http://127.0.0.1:1",
                        "vault.auth.path", "auth/basic/login",
                        "vault.username", "user",
                        "vault.password", "password"));

        TestContext context = new TestContext(config);

        assertTrue(context.isVaultConfigured());
        assertTrue(context.hasVault());
    }

    @Test
    void vaultIsAuthenticatedOnlyWhenRequested() {
        FrameworkConfig config = new FrameworkConfig(
                Environment.TEST,
                "http://localhost:8080",
                "http://localhost:8081",
                "http://127.0.0.1:1",
                Map.of(
                        "vault.url", "http://127.0.0.1:1",
                        "vault.auth.path", "auth/basic/login",
                        "vault.username", "user",
                        "vault.password", "password"));

        TestContext context = new TestContext(config);

        assertThrows(RuntimeException.class, context::vault);
    }

    @Test
    void vaultIsNotConfiguredWhenBootstrapSettingsAreMissing() {
        FrameworkConfig config = new FrameworkConfig(
                Environment.TEST,
                "http://localhost",
                "http://localhost",
                "",
                Map.of());

        TestContext context = new TestContext(config);

        assertFalse(context.isVaultConfigured());
        assertFalse(context.hasVault());
        assertThrows(IllegalStateException.class, context::vault);
    }
}
