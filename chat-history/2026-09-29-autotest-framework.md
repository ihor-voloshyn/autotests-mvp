# История чата — разработка Autotest Framework

Дата сохранения: 2026-09-29

## Назначение

Этот каталог хранит рабочую историю решений и требований, сформированных в чате при разработке репозитория `ihor-voloshyn/autotests-mvp`.

Проект сейчас **не привязан к конкретному бизнес-проекту**. Цель — полноценный переиспользуемый Java/Maven framework для автоматизации тестирования сервисов и процессов.

## Основные требования

- Java 21
- Maven
- JUnit 5
- REST Assured для REST
- Apache CXF для SOAP
- Basic Authentication
- Vault
- PostgreSQL JDBC
- Oracle JDBC
- Camunda REST
- RabbitMQ
- OKD / `oc`
- Tomcat
- ELK / Elasticsearch
- Allure
- многомодульная архитектура
- возможность последующего подключения Spring Boot, если это будет оправдано архитектурой

## Архитектура

Текущие Maven-модули:

- `autotest-core`
- `autotest-rest`
- `autotest-soap`
- `autotest-vault`
- `autotest-db`
- `autotest-camunda`
- `autotest-health`
- `autotest-messaging`
- `autotest-infrastructure`
- `autotest-reporting`
- `autotest-connectors`
- `autotest-tests`

Корневой artifact: `autotest-framework`, версия `0.1.0-SNAPSHOT`.

## Важное архитектурное решение

Framework должен оставаться generic.

В него не должны попадать:

- бизнесовые endpoint'ы конкретного проекта;
- конкретные Camunda process key;
- бизнесовые таблицы БД;
- конкретные тестовые сценарии;
- client-income-confirmation и другие project-specific сущности.

Такая логика должна находиться в проектах-потребителях framework.

## Endpoint model

Введена универсальная модель `ConnectionEndpoint` и `EndpointResolver`.

Поддерживаются:

- hostname;
- IPv4;
- IPv6;
- URL со scheme;
- endpoint с портом;
- endpoint без порта.

Примеры:

- `example.com`
- `example.com:8080`
- `10.20.30.40`
- `10.20.30.40:8080`
- `[2001:db8::10]`
- `[2001:db8::10]:8443`
- `https://example.com/api`
- `https://example.com:8443/api`

## Default ports

Если порт не задан, framework назначает default по типу соединения:

| Тип | Default |
|---|---:|
| HTTP | 80 |
| HTTPS | 443 |
| SOAP HTTP | 80 |
| SOAP HTTPS | 443 |
| Vault | 8200 |
| PostgreSQL | 5432 |
| Oracle | 1521 |
| Camunda | 8143 |
| Elasticsearch / ELK | 9200 |
| RabbitMQ | 5672 |
| RabbitMQ TLS | 5671 |

Явно указанный порт всегда имеет приоритет.

Отдельно зафиксировано решение пользователя: **Camunda использует default port 8143**.

## Database

Для БД schema является обязательным параметром.

Модель `DatabaseEndpoint`:

- type;
- host;
- port;
- database/service name;
- schema — mandatory;
- username;
- password.

Поддерживаются PostgreSQL и Oracle.

Default:

- PostgreSQL — 5432;
- Oracle — 1521.

Проверка schema должна выполняться как часть health check.

## Vault

Требуемый flow:

1. Basic Authentication в Vault.
2. Получение Vault token.
3. Последующие запросы выполняются с полученным token.
4. Token используется для получения конфигурации и credentials.
5. Поддерживаются KV v1 и KV v2.
6. Тесты framework не должны зависеть от конкретной версии KV.

Компоненты:

- `VaultAuthenticator`
- `BasicAuthVaultAuthenticator`
- `VaultToken`
- `VaultClient`
- `VaultSecretProvider`
- `VaultSecretStore`
- `VaultSecretResolver`
- `AuthenticatedVaultClient`

KV endpoints:

- KV1: `/v1/<mount>/<path>`
- KV2: `/v1/<mount>/data/<path>`

Default Vault port: 8200.

## REST

`RestClient` построен на REST Assured.

Поддерживает:

- base URL;
- base path;
- GET;
- query parameters;
- Basic Authentication;
- HTTP/HTTPS default ports.

Добавлены локальные тесты через Java `HttpServer`, без внешнего сервиса.

## SOAP

SOAP построен на Apache CXF.

`SoapClientFactory`:

- принимает JAX-WS service class;
- принимает endpoint;
- поддерживает Basic Auth;
- использует default 80/443;
- сохраняет явно заданный порт.

Для health check WSDL проверяется через HTTP.

Добавлен локальный `SoapConnectionCheckTest` для:

- успешного HTTP ответа;
- Basic Auth;
- ошибки 500;
- недоступного endpoint;
- validation пустого URL.

## Camunda

`CamundaClient` использует generic endpoint.

Default port: **8143**.

Проверяется отсутствие scheme и корректность default/explicit port.

## RabbitMQ

`RabbitMqClient` использует RabbitMQ Java client.

Поддерживаются:

- AMQP;
- AMQPS;
- username/password;
- virtual host;
- TLS для AMQPS;
- default port 5672;
- default TLS port 5671.

Добавлены unit tests для:

- default 5672;
- default 5671 для AMQPS;
- TLS;
- explicit AMQP/AMQPS ports;
- endpoint без scheme;
- validation null endpoint.

`RabbitMqConnectionCheck` проверяет реальное AMQP-соединение, но unit test использует недоступный localhost port и не требует внешнего RabbitMQ.

## ELK / Elasticsearch

`ElkClient` поддерживает:

- cluster health;
- search;
- Basic Authorization;
- default port 9200.

## OKD

`OkdClient` работает через `oc`:

- получение pod list/status;
- получение logs;
- restart pod через удаление pod;
- проверка exit code команды.

Команды выполняются через generic `CommandExecutor`.

## Connection Health Checks

Предусмотрена единая модель:

```
ConnectionCheck
  ├── HttpConnectionCheck
  ├── SoapConnectionCheck
  ├── VaultConnectionCheck
  ├── DatabaseConnectionCheck
  ├── CamundaConnectionCheck
  ├── RabbitMqConnectionCheck
  ├── ElkConnectionCheck
  └── OkdConnectionCheck
```

Результат:

- success/failure;
- message;
- duration.

`ConnectionHealthService` агрегирует результаты и превращает exceptions/null results в failure.

Добавлены локальные tests для HTTP, SOAP, Vault, ELK и RabbitMQ health checks.

Целевой запуск:

```
mvn test -Dtest=ConnectionHealthTest
```

Health checks должны интегрироваться с Allure.

## Configuration

Есть:

- `FrameworkConfig`
- `Configuration`
- `ConfigLoader`
- `EnvironmentConfigLoader`
- `SecretResolver`

Конфигурация поддерживает properties и environment variables.

Secrets могут разрешаться:

1. из локальной configuration;
2. через Vault при необходимости.

Vault подключается лениво, чтобы создание `TestContext` не требовало доступности Vault.

## Unified ConnectionFactory

`autotest-connectors` содержит `ConnectionFactory`.

Factory уже умеет создавать:

- REST client;
- Camunda client;
- ELK client;
- JDBC client;
- RabbitMQ client;
- Vault client;
- SOAP client.

Цель — дать потребляющим проектам единый способ получать подключения, не зная деталей создания клиентов.

## TestContext / BaseTest

`TestContext` хранит:

- FrameworkConfig;
- ConnectionFactory;
- lazy clients;
- secret resolution.

`BaseTest` загружает EnvironmentConfigLoader и создаёт TestContext.

## Testing strategy

Для framework-тестов предпочтительно:

- локальный HTTP server;
- mock/stub;
- unit tests;
- проверка endpoint parsing;
- проверка default ports;
- проверка credentials;
- проверка validation.

Не следует делать обязательными внешние подключения к реальным:

- Vault;
- DB;
- RabbitMQ;
- ELK;
- OKD;
- Camunda.

Реальные подключения должны выполняться только отдельными integration/health tests при наличии конфигурации.

## CI

GitHub Actions:

- checkout;
- Java 21 Temurin;
- Maven cache;
- `mvn -B verify`.

CI должен оставаться зелёным после каждого логического этапа.

В процессе работы обнаружено:

- commit `7892a0d` имел зелёный CI;
- следующий REST test commit временно падал из-за нестабильного `204` ответа локального `HttpServer`;
- тест изменён на стабильный `200 + body`;
- RabbitMQ test сначала не компилировался из-за неоднозначного `null` между двумя конструкторами;
- добавлен явный cast `(String) null`.

После последнего исправления новый CI run должен подтвердить итоговое состояние.

## История важных решений

### 1. Generic framework вместо конкретного проекта

Пользователь явно уточнил:

> «мы пока не привязываемся ни к какому проекту, сейчас цель полноценный фреймворк»

Это является текущим главным архитектурным ограничением.

### 2. Endpoint может быть URL или host/IP

Пользователь уточнил:

> «подключение может происходить как по урлу, так и по IP, как с указанием порта, так и без»

После этого введена универсальная endpoint-модель.

### 3. Default ports

Пользователь запросил:

> «задай дефолтные порты, если они не заданы, в зависимости от типа соединения»

Default ports стали частью framework, а не отдельных project-specific конфигураций.

### 4. Camunda

Пользователь отдельно исправил:

> «камунда 8143»

Поэтому default для Camunda — 8143.

### 5. Database schema

Пользователь указал:

> «для баз данных обязательное указание схемы»

Schema обязательна для DatabaseEndpoint.

### 6. Vault authentication

Пользователь определил:

> «у волта базовая аутентификация для получения токена, а затем по токену можно получить конфиги и креды»

И отдельно:

> «на волте есть kv1 и kv2»

Поэтому Vault authentication и secret retrieval разделены.

## Текущая точка продолжения

1. Дождаться/проверить CI после RabbitMQ test fix.
2. Если CI красный — исправить причину до следующего функционального этапа.
3. Завершить покрытие health-check компонентов.
4. Проверить DatabaseConnectionCheck и schema behavior.
5. Проверить Camunda/OKD health checks.
6. Улучшить Allure/reporting integration.
7. Проверить `ConnectionFactory` как единый публичный API framework.
8. Улучшать архитектуру без привязки к бизнес-проекту.
9. После каждого существенного этапа проверять CI.
10. Новые решения сохранять в этот каталог истории.


## Последнее продолжение

Добавлены:
- `RabbitMqClientTest` — default 5672/5671, TLS, explicit ports, validation;
- `RabbitMqConnectionCheckTest`;
- `SoapConnectionCheckTest`;
- `VaultConnectionCheckTest`;
- `CamundaConnectionCheckTest`;
- `OkdConnectionCheckTest`.

При проверке CI обнаружены и исправлены две проблемы тестов:
- нестабильная проверка query string из-за недетерминированного порядка `Map.of()`;
- неоднозначный `null` при перегрузке конструкторов `RabbitMqClient`.

На момент последней проверки CI runs 43–45 находятся в работе; предыдущие runs 41–42 были красными по указанным тестовым причинам.


## Продолжение после Database Health Check

CI run #52 для commit `75302598d598ab0406e41c35ee0f03a6a352ef23` завершился **success**.

Исправлена проблема компиляции `DatabaseConnectionCheckTest`: overloaded constructors делали `null` неоднозначным. В тесте добавлен явный тип `Supplier<DatabaseHealthClient>`.

Database health architecture теперь включает:
- generic `DatabaseHealthClient`;
- JDBC implementation через `JdbcClient`;
- проверку JDBC connection;
- обязательную schema;
- проверку доступности schema;
- lazy creation клиента;
- unit tests без реальной БД.

### Reporting / Allure

`autotest-reporting` расширен generic API:
- `AllureSupport.step(String, Runnable)`;
- `AllureSupport.step(String, Supplier<T>)`;
- `AllureSupport.parameter(String, String)`;
- существующие text/json attachments сохранены.

Добавлены unit tests для execution/result/validation/null handling.

Reporting остаётся generic и не содержит бизнесовой логики.

### Текущая следующая задача

Продолжить проверку и укрепление публичного API `ConnectionFactory`, затем улучшать конфигурацию и интеграцию health/reporting. Framework по-прежнему не должен содержать project-specific business logic.


## 2026-09-29 — Unified ConnectionFactory and Allure health integration

- Strengthened `ConnectionFactory` as the single generic public entry point:
  - added overloads using the default configuration-backed `SecretResolver`;
  - added `elk(prefix)`, `database(prefix, type)`, `rabbitMq(prefix)`, and `soap(serviceClass, prefix)`;
  - centralized validation of service prefixes and secret resolvers;
  - database type is validated before endpoint construction;
  - existing lazy Vault authentication and caching remain unchanged.
- Expanded `ConnectionFactoryTest` for overloads, blank prefixes, and null database type.
- Added `AllureConnectionHealthReporter` in `autotest-reporting`:
  - accepts generic `ConnectionCheckResult` values;
  - creates a separate Allure step for each check;
  - records PASS/FAIL, duration and message;
  - validates null result lists/entries.
- `autotest-reporting` now depends on `autotest-health`, keeping the dependency direction one-way: health does not depend on reporting.
- Latest commits:
  - `0c32de35a7d624afe98afae098d5dc3ff22ce025` — ConnectionFactory API
  - `ffbd8ef384831062883067e2c1a4d6fc8dc7bc2a` — ConnectionFactory tests
  - `53837992175994e542cd571bdd8fe128f68fd1a7` — reporting/health dependency
  - `411d1600f187c3c92b77e75340fbea090cef31c2` — Allure health reporter
  - `170530db7c712155b18edd816eb6e4099cdb505e` — reporter tests
- CI verification is still pending for these post-#52 changes; connector visibility currently does not expose a push-triggered run for these commits.

- Additional hardening: `ConnectionHealthService` now rejects null check entries at construction time instead of allowing a later NullPointerException during aggregation; corresponding test added.
- Latest health hardening commits:
  - `4a35dd4c1d47e62df96f61ca20197499670e530a`
  - `d20430679ec8768b7784b636b336a0fcc719b7d2`
