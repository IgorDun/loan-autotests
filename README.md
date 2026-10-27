# loan-autotests

[![CI](https://github.com/IgorDun/loan-autotests/actions/workflows/ci.yml/badge.svg)](https://github.com/IgorDun/loan-autotests/actions/workflows/ci.yml)

Фреймворк BDD-автотестов для микросервисной платформы: API, UI и БД в одном проекте.
Сценарии пишутся на русском Gherkin, шаги переиспользуются между командами,
стартовые данные описываются декларативно в YAML.

Проект основан на фреймворке, который я спроектировал и развиваю в продакшене
(единый фреймворк на 1500+ автотестов для 6 продуктовых команд). Из публичной версии убраны
бизнес-сценарии, внутренние сервисы, адреса и учётные данные. Примеры работают на открытых
тренажёрах [dummyjson.com](https://dummyjson.com) (API) и [saucedemo.com](https://www.saucedemo.com) (UI).

## Стек

| Область | Технологии |
|---|---|
| Язык и сборка | Java 21, Maven |
| Каркас | Spring Boot 4.1 (DI, конфигурация, WebClient, JdbcTemplate) |
| BDD | Cucumber 7 на JUnit Platform 6, параллельный запуск сценариев |
| API | Spring WebClient, Jackson, JsonPath, XPath |
| UI | Selenium 4, Atlas (page objects на интерфейсах), WebDriverManager, Testcontainers (браузер в Docker с записью видео), Chrome DevTools Protocol |
| Данные | YAML + find-or-create фабрики, шаблоны JSON с подстановками и Groovy-выражениями |
| Отчёты | Allure 3 (шаги, логи, запросы и скриншоты как вложения), ReportPortal (опционально) |
| Прочее | Awaitility, AssertJ, WireMock admin API, Elasticsearch (поиск логов сервисов) |

## Архитектура

```
src/main/java/dev/kozhevnikov/autotests
├── config/          реестр сервисов и БД, настройки окружения, бины WebClient/ObjectMapper/Groovy
├── api/model/       модель HTTP-запроса и ответа, описания сервиса и БД
├── utils/api/       ServiceUtils — единый шлюз HTTP; JsonUtils, XmlUtils
├── utils/common/    контекст сценария, подстановки ${var} и @{groovy}, ожидания, файлы, генераторы данных
├── utils/db/        DbUtils поверх JdbcTemplate
├── utils/startdata/ стартовые данные: YAML → collector → factory (find-or-create) → фасад с кешем
├── elastic/         поиск логов в Elasticsearch через REST _search
├── wiremock/        модели для WireMock admin API
└── logger/          AllureAppender — каждая строка лога становится вложением шага

src/test/java/dev/kozhevnikov/autotests
├── RunCucumberTest  точка запуска через JUnit Platform Suite
├── config/          Spring-контекст Cucumber, WebDriver, Atlas (scope cucumber-glue)
├── glue/, hooks/    хуки: стартовые данные, очистка контекста и заглушек, браузер, вложения при падении
├── steps/           шаги сервисов и сайтов (DummyJsonApiSteps, SauceDemoSteps) и общие: JSON/XML, переменные, файлы, UI, DevTools, WireMock, Elastic
└── web/             элементы, сайты и страницы Atlas, матчеры
```

Ключевые решения:

- **Реестр сервисов.** Все сервисы описаны в `services.properties`: URL с плейсхолдером стенда,
  таймаут и заголовки по умолчанию. Часто используемые сервисы доступны бинами, например `dummyjsonService`.
- **Шаг — это бизнес-действие.** Сценарий не знает про URI, HTTP-метод и коды ответа:
  `dummyjson > Создаю товар и сохраняю как 'created'`. Эндпоинт, метод, шаблон тела и ожидаемый код
  зашиты в шаге сервиса (`DummyJsonApiSteps`). Сценарии читаются как бизнес-процесс,
  а изменение API правится в одном месте.
- **Один HTTP-шлюз.** Все запросы идут через `ServiceUtils`, поэтому логирование, таймауты
  и вложения в отчёт настроены в одном месте. 4xx и 5xx не бросают исключение, код проверяется внутри шага.
- **Контекст сценария.** Переменные хранятся в `AutoTestContext` на `ThreadLocal`, а WebDriver
  и Atlas живут в области `cucumber-glue`. Поэтому сценарии изолированы и запускаются параллельно.
- **Подстановки.** В шагах, таблицах и шаблонах работают `${var}`, `${var:значение по умолчанию}`
  и Groovy-выражения `@{RandomUtils.randomInt(1, 10)}`.
- **Тела запросов из шаблонов.** JSON тела лежит в `templates/`, сценарий передаёт только значения
  таблицей `Map<String, String>`. Структура запроса меняется в одном файле, а не во всех сценариях.
- **Декларативные данные.** Настройки, справочники и прочие предусловия описываются в YAML.
  Фабрика ищет сущность, создаёт недостающую и обновляет отличающуюся. Пример — `settings.yaml`.
- **Самоочистка.** Заглушки WireMock, созданные в сценарии, удаляются в `@After` даже при падении.

## Запуск

Нужны JDK 21, Maven 3.9+ и Chrome для UI.

```bash
# все сценарии
mvn test

# только API или только UI
mvn test -Dcucumber.filter.tags="@api"
mvn test -Dcucumber.filter.tags="@ui" -Dheadless=true

# браузер в Docker с записью видео упавших сценариев
mvn test -Dcucumber.filter.tags="@ui" -Denv.webDriverType=testcontainers -Denv.webDriverVideoRecording=ERRORS

# отчёт Allure
allure serve target/allure-results
```

Стенд выбирается параметром `-Denv.inventoryName=test`, параллелизм настраивается
в `src/test/resources/junit-platform.properties`.

## Как добавить сервис и сценарий

1. Добавить сервис в `src/main/resources/services.properties`:
   ```properties
   api.services[4].name=orders
   api.services[4].url=https://orders.${inventoryName}.example.com/api/
   api.services[4].headers.authorization=Bearer ${ORDERS_TOKEN:}
   ```
2. Положить шаблон тела в `src/test/resources/templates/orders/create`:
   ```json
   {
     "productId": ${productId},
     "quantity": ${quantity: 1},
     "comment": "${comment: "автотест"}"
   }
   ```
   Плейсхолдер заполняется из таблицы шага, затем из контекста сценария,
   затем значением по умолчанию после двоеточия (Groovy-выражение).
3. Добавить бин сервиса в `ServiceConfiguration` и класс шагов `OrdersApiSteps`:
   ```java
   @When("^orders > Создаю заказ и сохраняю как '(.+)':$")
   public void createOrder(String responseName, Map<String, String> params) {
       String uri = "/orders";
       String body = commonUtils.getEnrichedTemplate("orders/create", params);

       AtHttpRequest request = serviceUtils.generateHttpRequest(ordersService)
               .resolveUri(uri)
               .setMethod(HttpMethod.POST)
               .setBody(jsonUtils.toJson(jsonUtils.unify(body)));
       AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
       Assertions.assertThat(response.getStatusCode())
               .withFailMessage("Неверный код ответа при создании заказа")
               .isEqualTo(201);

       AutoTestContext.put(responseName, response.getBody());
   }
   ```
4. Написать сценарий:
   ```gherkin
   * orders > Создаю заказ и сохраняю как 'order':
     | productId | ${productId} |
     | quantity  | 2            |
   * Сохраняю объект из json 'order' по пути:
     | orderId | $.id |
   ```
5. Для нового фронта: интерфейс сайта в `web/sites/front`, имя в `Sites`, страницы в `web/pages`.

Примеры: `src/test/resources/features/api/dummyjson.feature`, `src/test/resources/features/ui/saucedemo.feature`.

## Основные шаги

| Шаг | Назначение |
|---|---|
| `dummyjson > Авторизуюсь, сохраняю токен как '<имя>' и ответ как '<имя>':` | пример шага сервиса: POST по шаблону, проверка кода, сохранение токена |
| `dummyjson > Ищу товары и сохраняю результат как '<имя>':` | пример шага сервиса: GET с query-параметрами из таблицы |
| `Проверяю в json '<имя>' поля:` | JsonPath → регулярное выражение, мягкие проверки |
| `Сохраняю объект из json '<имя>' по пути:` | извлечение значений в контекст |
| `Создаю json по шаблону '<шаблон>' и сохраняю как: '<имя>':` | шаблоны тел запросов |
| `wiremock > Создаю заглушку:` | заглушка с автоудалением после сценария |
| `elastic-search > Получаю N логов из filebeat ...` | поиск логов сервиса по условиям |
| `<сайт> > Нажимаю на '<текст>'`, `<сайт> > Ввожу текст ...`, `<сайт> > Проверяю, что '<текст>' есть на странице` | общие UI-шаги |
| `<сайт> > Заполняю анкету:` | форма из таблицы «название поля → значение», поле ищется по `@Name` на странице |
| `devTools > Сохраняю тело ответа '<метод>' как '<имя>'` | перехват сетевых запросов браузера через CDP |

## Конфигурация и секреты

Учётные данные в репозиторий не попадают. Токены и пароли передаются
через переменные окружения или `-D`-параметры: `START_TOKEN`, `DB_PASSWORD_*`,
`ELASTIC_USER`, `ELASTIC_PASSWORD`, `RP_API_KEY`.
