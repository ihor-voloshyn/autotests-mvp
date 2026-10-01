# Reusable Autotest Framework

Reusable Java 21 / Maven automation framework for service, integration and process testing.

## Architecture

- **autotest-core** — configuration, endpoints, credentials and secret-resolution abstractions.
- **autotest-rest** — REST Assured clients.
- **autotest-soap** — Apache CXF SOAP clients with Basic Authentication.
- **autotest-vault** — Vault Basic Auth, token handling, KV v1/v2 and secret providers.
- **autotest-db** — PostgreSQL and Oracle JDBC access with mandatory schema configuration.
- **autotest-camunda** — generic Camunda REST/process helpers.
- **autotest-messaging** — RabbitMQ connectivity.
- **autotest-infrastructure** — OKD/oc, ELK and command execution helpers.
- **autotest-reporting** — Allure integration.
- **autotest-health** — reusable connection health checks.
- **autotest-connectors** — unified connection factory for the framework adapters.
- **autotest-junit** — JUnit 5 integration, including the reusable infrastructure health-check extension.
- **autotest-tests** — framework-level and project-specific test scenarios.

## Connectivity

Network endpoints can be configured by hostname, IPv4 or IPv6, with an optional port, or as a full URL.

Examples:

- https://service.example.com
- https://service.example.com:8443/api
- 10.20.30.40:8080
- [2001:db8::10]:8443

Default ports are used only when a port is omitted and the protocol has a defined default.

Database configuration requires an explicit schema. PostgreSQL and Oracle clients apply the configured schema to the JDBC session.

## Vault

Vault authentication is separated from secret retrieval:

1. HTTP Basic Authentication obtains a Vault token.
2. The token is used for subsequent API calls.
3. KV v1 and KV v2 are supported behind the same secret-provider abstraction.

## JUnit 5 health checks

The framework can run configured infrastructure checks before a JUnit 5 test class:

```java
@ExtendWith(HealthCheckExtension.class)
class MyIntegrationTest {
    // tests run only after configured health checks succeed
}
```

The extension supports two policies:

- `FAIL_ON_ANY_FAILURE` — infrastructure failures fail the test class startup.
- `REPORT_ONLY` — results are published to Allure without failing startup.

Health checks can run sequentially, in parallel, or in parallel fail-fast mode through `HealthRunOptions`.

## Baseline

- Java 21
- Maven
- JUnit 5
- REST Assured
- Apache CXF
- PostgreSQL / Oracle JDBC
- RabbitMQ client
- Allure
- Multi-module architecture

The framework is intentionally generic. Business-specific endpoints, process keys, database tables and scenarios should be implemented by projects that consume this framework rather than embedded in its core modules.
