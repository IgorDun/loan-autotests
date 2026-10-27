package dev.kozhevnikov.autotests.steps.api;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import dev.kozhevnikov.autotests.exception.AutotestException;
import dev.kozhevnikov.autotests.utils.CucumberUtils;
import dev.kozhevnikov.autotests.utils.CucumberUtils.KeyDisposition;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.XmlUtils;
import dev.kozhevnikov.autotests.utils.common.AutoTestContext;
import dev.kozhevnikov.autotests.utils.common.CommonUtils;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class CommonApiSteps {
    private final CommonUtils commonUtils;
    private final JsonUtils jsonUtils;
    private final XmlUtils xmlUtils;
    private final Enricher enricher;
    private final CucumberUtils cucumberUtils;
    private final FileUtils fileUtils;

    /**
     * ---------
     * --When---
     * ---------
     */

    /**
     * @param jsonName key json'а из контекста
     * @param params   параметры в виде jsonPath|key, для сохранения в контекст<br>
     *                 Пример степа:<br>
     *                 And Сохраняю из json 'anyJson':<br>
     *                 | $.id                           | anyId        |<br>
     *                 | $.name                         | name         |<br>
     *                 | $.persons[?(@.type == 'TEST')] | anyJsonArray |<br>
     */
    @When("^Сохраняю объект из json '(.+)' по пути:$")
    public void saveFromJson(String jsonName, Map<String, String> params) {
        Object json = Optional.ofNullable(AutoTestContext.get(jsonName))
                .orElseThrow(() -> new RuntimeException("В контексте отсутствует запись с ключом '" + jsonName + "'"));
        enricher.enrich(params).forEach((name, path) -> {
            Object fragment = jsonUtils.getAsTypeByPath(json, path);
            if (fragment == null || (fragment instanceof String && ((String) fragment).isEmpty()) || fragment.equals("null")) {
                throw new RuntimeException("Не удалось найти объект по пути: " + path);
            }
            if (fragment instanceof String) {
                fragment = jsonUtils.unify(jsonUtils.toJsonString(fragment));
            } else {
                fragment = jsonUtils.toJson(fragment);
            }
            AutoTestContext.put(name, fragment);
            log.info("Сохранено как '{}': {}", name, fragment);
        });
    }


    /**
     * @param docs параметры в виде | название файла из resources.files | название для сохранения в контекст |
     *             Пример степа:<br>
     *             And Конвертирую файлы в base64 и сохраняю:<br>
     *             | green.jpeg | name1 |<br>
     *             | green.jpeg | name2 |<br>
     */
    @When("^Конвертирую файлы в base64 и сохраняю:$")
    public void saveBase64File(Map<String, String> docs) {
        enricher.enrich(docs)
                .entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(commonUtils.fileToBase64(entry.getKey()),
                        entry.getValue()))
                .peek(entry -> log.info("{}: {}", entry.getValue(), entry.getKey()))
                .forEach(entry -> AutoTestContext.put(entry.getValue(), entry.getKey()));
    }

    @When("^Конвертирую файлы из base64 и сохраняю:$")
    public void FromBase64File(Map<String, String> docs) {
        enricher.enrich(docs)
                .entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(commonUtils.fromBase64(entry.getKey()),
                        entry.getValue()))
                .peek(entry -> log.info("{}: {}", entry.getValue(), entry.getKey()))
                .forEach(entry -> AutoTestContext.put(entry.getValue(), entry.getKey()));
    }

    @When("^Создаю CSV файл с параметрами и сохраняю как '(.+)':$")
    public void saveCSV(String fileName, List<List<String>> params) throws IOException {
        Path csvOutputFile = Files.createTempFile(fileName, ".csv");
        List<String> lines = params
                .stream()
                .map(line -> line
                        .stream()
                        .map(enricher::enrich)
                        .collect(Collectors.joining(";"))
                )
                .collect(Collectors.toList());

        try {
            Files.write(csvOutputFile, lines, Charset.forName("Windows-1251"));
        } catch (IOException e) {
            throw new AutotestException(e);
        }

        Assertions.assertThat(Files.exists(csvOutputFile))
                .withFailMessage("Не удалось создать временный csv-файл")
                .isTrue();

        AutoTestContext.put(fileName, csvOutputFile.toAbsolutePath());

        log.info("Создан временный файл {} ,путь до файла {} ", fileName, csvOutputFile.toAbsolutePath());
    }

    @When("^Удаляю файл '(.+)'$")
    public void deleteFile(String fileName) throws IOException {
        String enrichedFileName = enricher.enrich(fileName);
        Path file = Path.of(enrichedFileName);
        Files.deleteIfExists(file);

        Assertions.assertThat(Files.notExists(file))
                .withFailMessage("Не удалось удалить временный файл")
                .isTrue();

        log.info("Удален временный файл {} ", enrichedFileName);
    }

    @When("^Обновляю в json '(.+)' поля:$")
    public void setIntoJson(String jsonName, Map<String, String> params) {
        Object json = jsonUtils.toJson(AutoTestContext.get(jsonName));
        Map<String, Object> enrichedParams = commonUtils.toEnrichedJsonMap(params);
        for (Map.Entry<String, Object> param : enrichedParams.entrySet()) {
            json = jsonUtils.setValue(json, param.getKey(), param.getValue());
        }

        AutoTestContext.put(jsonName, json);
        log.info("Обновленный json: {}", json);
    }

    @When("^Формирую (объект|массив) json и сохраняю как '(.+)':$")
    public void createJson(String jsonType, String jsonName, DataTable params) {
        List<Map<String, String>> json = cucumberUtils.asMaps(params, KeyDisposition.VERTICAL);
        Function<List<Map<String, String>>, List<Map<String, Object>>> function = list -> list.stream()
                .map(commonUtils::toEnrichedJsonMap)
                .collect(Collectors.toList());
        Object resultJson = jsonType.equalsIgnoreCase("объект") ? commonUtils.toEnrichedJsonMap(json.get(0)) : function.apply(json);
        AutoTestContext.put(jsonName, jsonUtils.toJson(resultJson));
        log.info("Сохранено как '{}': {}", jsonName, jsonUtils.toJson(resultJson));
    }

    @When("^Сохраняю значения в переменную:$")
    public void saveValueAsVariable(Map<String, String> params) {
        enricher.enrich(params).forEach((key, value) -> {
            AutoTestContext.put(key, value);
            log.info("Значение '{}' сохранено, как '{}'", key, value);
        });
    }

    @When("^Сохраняю значения с шифрованием 'sha1':$")
    public void saveSha1Text(Map<String, String> params) {
        enricher.enrich(params).forEach((key, value) -> {
            String textSha1 = DigestUtils.sha1Hex(value);
            AutoTestContext.put(key, textSha1);
            log.info("Значение '{}' сохранено, как '{}'", key, textSha1);
        });
    }

    @When("^Преобразовываю дату '(.+)' по формату '(.+)' и сохраняю как '(.+)'$")
    public void convertDateFormat(String unformattedDate, String format, String valueName) {
        LocalDate localDate = LocalDate.parse(enricher.enrich(unformattedDate));
        formatAndStoreDate(localDate, format, valueName);
        log.info("Дата преобразована и сохранена как {}", valueName);
    }

    @When("^Преобразовываю дату '(.+)' из формата '(.+)' в формат '(.+)' и сохраняю ее как '(.+)'$")
    public void convertDateFormat(String unformattedDate, String sourceFormat, String format, String valueName) {
        LocalDate localDate = LocalDate.parse(enricher.enrich(unformattedDate),
                DateTimeFormatter.ofPattern(sourceFormat));
        formatAndStoreDate(localDate, format, valueName);
        log.info("Дата преобразована и сохранена как {}", valueName);
    }

    @When("^Формирую json-массив объектов по шаблону '(.+)' и сохраняю как '(.+)':$")
    public void saveBasket(String templatePath, String variableName, List<Map<String, String>> params) {
        List<Object> list = commonUtils.createListFromTestTemplate(templatePath, params);
        AutoTestContext.put(variableName, list);
        log.info("Создан json-массив '{}':\n{}", variableName, list);
    }

    @When("^Создаю по шаблону '(.+)' Json и называем его '(.+)':$")
    public void saveTemplateToJson(String path, String nameJson) {
        String templatePath = "lk-front-api/applications/" + path;
        String templateText = jsonUtils.unify(enricher.enrich(fileUtils.readResourceTemplate(templatePath)));
        AutoTestContext.put(nameJson, templateText);
        log.info("Сохраняю текст шаблона '{}' в переменную '{}'", path, nameJson);
        log.info("Содержимое шаблона для переменной '{}':{}", nameJson, templateText);
        fileUtils.saveJsonToFile(nameJson, templateText);
    }

    @When("^Удаляю файл '(.+)' из папки с файлами$")
    public void deleteTemporaryFile(String fileName) {
        fileUtils.deleteJsonFile(fileName);
    }

    /**
     * ---------
     * --Then---
     * ---------
     */

    /**
     * @param jsonName key json'а из контекста
     * @param params   параметры в виде jsonPath|requiredValue<br>
     *                 Где requiredValue - regex<br>
     *                 Пример степа:<br>
     *                 And Проверяю в json 'anyJson' поля:<br>
     *                 | $.name                         | (\\w)+       |<br>
     *                 | $.persons[?(@.type == 'TEST')] | anyJsonArray |<br>
     */
    @Then("^Проверяю в json '(.+)' поля:$")
    public void checkJson(String jsonName, Map<String, String> params) {
        Object json = Optional.ofNullable(AutoTestContext.get(jsonName))
                .orElseThrow(() -> new RuntimeException("В контексте отсутствует '" + jsonName + "'"));
        String assertErrorMessage = "Значение '%s' не соответствует регулярному выражению '%s'";
        List<AbstractMap.SimpleEntry<Object, String>> paramsList = enricher.enrich(params)
                .entrySet()
                .stream()
                .peek(entry -> log.info("Проверка поля '{}'", entry.getKey()))
                .map(entry -> new AbstractMap.SimpleEntry<>(jsonUtils.findAsTypeByPath(jsonUtils.toJson(json), entry.getKey()).orElse("null"),
                        entry.getValue()))
                .peek(entry -> log.info("Фактическое значение: '{}'\nОжидаемое значение (регулярное выражение): '{}'",
                        entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        SoftAssertions.assertSoftly(assertions -> paramsList.forEach(
                        entry -> assertions.assertThat(entry.getKey().toString())
                                .withFailMessage(String.format(assertErrorMessage, entry.getKey(), entry.getValue()))
                                .matches(entry.getValue())
                )
        );
        log.info("Все поля в json '{}' соответствуют регулярным выражениям", jsonName);
    }

    @Then("^Проверяю текст '(.+)' на соответствие регулярным выражениям:$")
    public void checkTextByRegex(String textName, List<String> params) {
        final String assertErrorMessage = "Текст не соответствует регулярному выражению '%s'";

        String text = String.valueOf(AutoTestContext.getNotNull(enricher.enrich(textName)));
        List<String> paramsList = enricher.enrich(params);
        SoftAssertions.assertSoftly(assertions ->
                paramsList.forEach(
                        regex -> assertions.assertThat(text)
                                .withFailMessage(String.format(assertErrorMessage, regex))
                                .matches(regex)
                )
        );
        log.info("Текст '{}' соответствуют регулярным выражениям", textName);
    }

    @Then("^Проверяю в xml '(.+)' поля:$")
    public void checkXml(String xmlName, Map<String, String> params) {
        String xml = Optional.ofNullable((String) AutoTestContext.get(xmlName))
                .map(xmlUtils::unify)
                .orElseThrow(() -> new RuntimeException("В контексте отсутствует '" + xmlName + "'"));
        String assertErrorMessage = "Значение '%s' не соответствует регулярному выражению '%s'";
        enricher.enrich(params)
                .entrySet()
                .stream()
                .peek(entry -> log.info("Проверка поля '{}'", entry.getKey()))
                .map(entry -> new AbstractMap.SimpleEntry<>(xmlUtils.getByPath(xml, entry.getKey()),
                        entry.getValue()))
                .peek(entry -> log.info("Фактическое значение: '{}'\nОжидаемое значение (регулярное выражение): '{}'",
                        entry.getKey(), entry.getValue()))
                .collect(Collectors.toList())
                .forEach(entry -> SoftAssertions.assertSoftly(
                        assertions -> assertions.assertThat(entry.getKey())
                                .withFailMessage(String.format(assertErrorMessage, entry.getKey(), entry.getValue()))
                                .matches(entry.getValue())
                ));
        log.info("Все поля в xml '{}' соответствуют регулярным выражениям", xmlName);
    }

    @Then("^Сохраняю объект из xml '(.+)' по пути:$")
    public void saveFromXml(String xmlName, Map<String, String> params) {
        String xml = Optional.ofNullable((String) AutoTestContext.get(xmlName))
                .map(xmlUtils::unify)
                .orElseThrow(() -> new RuntimeException("В контексте отсутствует запись с ключом '" + xmlName + "'"));
        enricher.enrich(params).forEach((name, path) -> {
            Object fragment = xmlUtils.getByPath(xml, path);
            if (fragment == null || fragment.equals("null")) {
                throw new RuntimeException("Не удалось найти объект по пути: " + path);
            }
            AutoTestContext.put(name, fragment);
            log.info("Сохранено как '{}': {}", name, fragment);
        });
    }

    @Then("^Проверяю, что значение переменных равны:$")
    public void compareValues(Map<String, String> params) {
        enricher.enrich(params).forEach((key, value) -> {
            log.info("Сравнил переменную - '{}', с переменной - '{}'", key, value);
            Assertions.assertThat(value).withFailMessage("Значения не равны").isEqualTo(key);
        });
    }

    //TODO: Убрать метод, путём переноса ожидания в нужное метод
    @Then("^Ожидаю '(\\d+)' секунд$")
    public void waitingLogs(Long time) throws InterruptedException {
        Thread.sleep(time * 1000);
    }

    @Then("Унифицирую json {string} и сохраняю как {string}")
    public void unifyJson(String jsonName, String resultJsonName) {
        String json = AutoTestContext.getNotNull(jsonName).toString();
        Object unifyJson = jsonUtils.toJson(jsonUtils.unify(json));
        AutoTestContext.put(resultJsonName, unifyJson);
        log.info("Унифицированный json: {}", unifyJson);
    }

    @Then("^Декодирую url '(.+)' и сохраняю как: '(.+)'$")
    public void decodingUrl(String url, String result) {
        String decodedUrl = URLDecoder.decode((String) AutoTestContext.get(url), StandardCharsets.UTF_8);
        AutoTestContext.put(result, decodedUrl);
        log.info("Расшифрованный url: {}", decodedUrl);
    }

    @Then("^Создаю json по шаблону '(.+)' и сохраняю как: '(.+)':$")
    public void createJsonFromTemplate(String templateName, String jsonName, Map<String, String> params) {
        String result = commonUtils.getEnrichedTemplate(templateName, params);
        AutoTestContext.put(jsonName, result);
        log.info("Созданный json: {}", result);
    }

    private void formatAndStoreDate(LocalDate localDate, String format, String valueName) {
        String formattedDate = DateTimeFormatter.ofPattern(format)
                .format(localDate);
        AutoTestContext.put(valueName, formattedDate);
    }
}