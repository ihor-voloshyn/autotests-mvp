package com.ihorvoloshyn.autotests.connectors;

import com.ihorvoloshyn.autotests.core.config.Environment;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import com.ihorvoloshyn.autotests.core.config.SecretResolver;
import com.ihorvoloshyn.autotests.db.DatabaseEndpoint;
import com.ihorvoloshyn.autotests.db.DatabaseType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionFactoryTest {

    @Test
    void createsRestClientFromConfiguration() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "",
                Map.of("service.url", "https://example.com:8443/api"));
        assertNotNull(new ConnectionFactory(config).rest("service"));
    }

    @Test
    void createsRestClientUsingSecretResolver() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "",
                Map.of("service.url", "https://example.com:8443/api",
                        "service.username-key", "service-user",
                        "service.password-key", "service-password"));
        var secrets = SecretResolver.fromMap(Map.of("service-user", "user", "service-password", "password"));
        assertNotNull(new ConnectionFactory(config).rest("service", secrets));
    }

    @Test
    void createsCamundaClientFromConfiguredUrl() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "",
                Map.of("camunda.url", "http://10.20.30.40:8080"));
        assertNotNull(new ConnectionFactory(config).camunda());
    }

    @Test
    void createsElkClientWithoutExplicitSecretResolver() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "",
                Map.of("elk.url", "https://10.20.30.40:9200",
                        "elk.username", "user",
                        "elk.password", "password"));
        assertNotNull(new ConnectionFactory(config).elk("elk"));
    }

    @Test
    void createsElkClientWithCredentials() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "",
                Map.of("elk.url", "https://10.20.30.40:9200",
                        "elk.username-key", "elk-user",
                        "elk.password-key", "elk-password"));
        SecretResolver secrets = SecretResolver.fromMap(Map.of("elk-user", "user", "elk-password", "password"));
        assertNotNull(new ConnectionFactory(config).elk("elk", secrets));
    }

    @Test
    void databaseConfigurationAssignsMandatorySchemaAndDefaultPort() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "",
                Map.of("db.host", "10.20.30.40",
                        "db.database", "testdb",
                        "db.schema", "client",
                        "db.username", "user",
                        "db.password", "password"));

        DatabaseEndpoint endpoint = DatabaseEndpoint.from(
                config, "db", DatabaseType.POSTGRESQL,
                SecretResolver.fromMap(config.properties()));

        assertEquals(DatabaseType.POSTGRESQL, endpoint.type());
        assertEquals("10.20.30.40", endpoint.host());
        assertEquals(5432, endpoint.port());
        assertEquals("testdb", endpoint.database());
        assertEquals("client", endpoint.schema());
    }

    @Test
    void createsDatabaseClientUsingDefaultSecretResolver() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "",
                Map.of("db.host", "10.20.30.40",
                        "db.database", "testdb",
                        "db.schema", "client",
                        "db.username", "user",
                        "db.password", "password"));
        assertNotNull(new ConnectionFactory(config).database("db", DatabaseType.POSTGRESQL));
    }

    @Test
    void createsRabbitMqClientFromUrl() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "",
                Map.of("rabbit.url", "amqps://10.20.30.40:5671",
                        "rabbit.username", "user",
                        "rabbit.password", "password"));
        assertNotNull(new ConnectionFactory(config)
                .rabbitMq("rabbit", SecretResolver.fromMap(config.properties())));
    }

    @Test
    void createsRabbitMqClientFromEndpointAlias() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "",
                Map.of("rabbit.endpoint", "10.20.30.40:5672",
                        "rabbit.username", "user",
                        "rabbit.password", "password"));
        assertNotNull(new ConnectionFactory(config)
                .rabbitMq("rabbit", SecretResolver.fromMap(config.properties())));
    }

    @Test
    void createsRabbitMqClientUsingDefaultSecretResolver() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "",
                Map.of("rabbit.url", "amqp://10.20.30.40:5672",
                        "rabbit.username", "user",
                        "rabbit.password", "password"));
        assertNotNull(new ConnectionFactory(config).rabbitMq("rabbit"));
    }

    @Test
    void rejectsNullRabbitMqSecretResolver() {
        FrameworkConfig config = new FrameworkConfig(Environment.TEST, "", "", "",
                Map.of("rabbit.url", "amqp://10.20.30.40:5672"));
        assertThrows(IllegalArgumentException.class,
                () -> new ConnectionFactory(config).rabbitMq("rabbit", null));
    }

    @Test
    void rejectsBlankPrefixes() {
        ConnectionFactory factory = new ConnectionFactory(
                new FrameworkConfig(Environment.TEST, "", "", "", Map.of()));
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> factory.rest(" ")),
                () -> assertThrows(IllegalArgumentException.class, () -> factory.camunda(" ")),
                () -> assertThrows(IllegalArgumentException.class, () -> factory.elk(" ")),
                () -> assertThrows(IllegalArgumentException.class, () ->
                        factory.database(" ", DatabaseType.POSTGRESQL)),
                () -> assertThrows(IllegalArgumentException.class, () ->
                        factory.rabbitMq(" ")),
                () -> assertThrows(IllegalArgumentException.class, () ->
                        factory.vault(" ")));
    }

    @Test
    void rejectsNullDatabaseType() {
        ConnectionFactory factory = new ConnectionFactory(
                new FrameworkConfig(Environment.TEST, "", "", "", Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> factory.database("db", null));
    }

    @Test
    void rejectsNullConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new ConnectionFactory(null));
    }
}
