# Client Income Confirmation NBU Autotest

Reusable Java 21 / Maven automation framework for bank service and process testing.

## Modules

- **autotest-core** — shared configuration, models, assertions and test utilities.
- **autotest-rest** — REST Assured clients and REST-specific helpers.
- **autotest-soap** — Apache CXF SOAP clients and authentication.
- **autotest-vault** — Vault authentication and configuration retrieval.
- **autotest-db** — PostgreSQL and Oracle JDBC access.
- **autotest-camunda** — Camunda REST/process helpers and audit verification.
- **autotest-infrastructure** — OKD, Tomcat, pod/log/restart integrations.
- **autotest-reporting** — Allure and reporting integration.
- **autotest-tests** — business scenarios and end-to-end tests.

## Baseline

- Java 21
- Maven
- JUnit 5
- Multi-module architecture

The framework is intentionally separated into infrastructure adapters and business test scenarios so the same foundation can be reused for other services/processes.
