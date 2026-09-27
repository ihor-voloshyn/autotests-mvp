package com.ihorvoloshyn.autotests.db;

import com.ihorvoloshyn.autotests.core.config.Environment;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import com.ihorvoloshyn.autotests.core.config.SecretResolver;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseEndpointTest {
    @Test void schemaIsMandatory() {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseEndpoint(
                DatabaseType.POSTGRESQL,"db",null,"testdb","", "user","pass"));
    }
    @Test void postgresDefaultPort() {
        var e=new DatabaseEndpoint(DatabaseType.POSTGRESQL,"db",null,"testdb","client","u","p");
        assertEquals(5432,e.effectivePort());
    }
    @Test void oracleDefaultPort() {
        var e=new DatabaseEndpoint(DatabaseType.ORACLE,"db",null,"ORCL","CLIENT","u","p");
        assertEquals(1521,e.effectivePort());
    }
    @Test void customPortIsPreserved() {
        var e=new DatabaseEndpoint(DatabaseType.POSTGRESQL,"10.0.0.1",15432,"testdb","client","u","p");
        assertEquals(15432,e.effectivePort());
    }
    @Test void credentialsCanBeResolvedFromSecretResolver() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "", Map.of(
                "db.host", "10.0.0.10",
                "db.database", "testdb",
                "db.schema", "client",
                "db.username-key", "db-user",
                "db.password-key", "db-password"));
        SecretResolver secrets = SecretResolver.fromMap(Map.of(
                "db-user", "vault-user",
                "db-password", "vault-password"));

        var endpoint = DatabaseEndpoint.from(config, "db", DatabaseType.POSTGRESQL, secrets);

        assertEquals("vault-user", endpoint.username());
        assertEquals("vault-password", endpoint.password());
        assertEquals("client", endpoint.schema());
    }
}