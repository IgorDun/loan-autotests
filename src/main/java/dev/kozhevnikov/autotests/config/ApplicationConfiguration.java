package dev.kozhevnikov.autotests.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import dev.kozhevnikov.autotests.utils.common.AutoTestContext;
import dev.kozhevnikov.autotests.utils.common.DateUtils;
import dev.kozhevnikov.autotests.utils.common.RandomUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Класс содержит настройки и конфигурации для работы с объектами.
 */
@Configuration
@EqualsAndHashCode
@SuppressWarnings("unused")
public class ApplicationConfiguration {

    /**
     * Создает сервис конвертации для обеспечения преобразования объектов.
     *
     * @param objectMapper объект ObjectMapper, используемый для преобразования данных
     * @return ConversionService для конвертации объектов
     */
    @Bean
    public ConversionService conversionService(ObjectMapper objectMapper) {
        /**
         * * Вложенный класс, реализующий конвертер для преобразования строки в Map.
         */
        class StringToMapConverter implements Converter<String, Map<String, Object>> {
            /**
             * Преобразует строку в Map объект.
             *
             * @return Map<String, Object> - преобразованный объект Map
             */
            @Override
            @SneakyThrows
            @SuppressWarnings("all")
            public Map<String, Object> convert(String source) {
                return objectMapper.readValue(source, new TypeReference<Map<String, Object>>() {
                });
            }
        }

        DefaultConversionService service = new DefaultConversionService();
        service.addConverter(new StringToMapConverter());
        return service;
    }

    /**
     * Создает экземпляр WebClient для HTTP-запроса используя заданные стратегии обмена HTTP-запросами и ответами.
     *
     * @param httpExchangeStrategies стратегии обмена
     * @return WebClient для выполнения HTTP-запроса
     */

    @Bean
    @Lazy
    public WebClient httpWebClient(ExchangeStrategies httpExchangeStrategies) {
        return WebClient.builder().exchangeStrategies(httpExchangeStrategies).build();
    }

    /**
     * Создает стратегии обмена, настроенные для использования Jackson2JsonDecoder.
     *
     * @param jackson2JsonDecoder декодер Jackson2JsonDecoder, используемый для декодирования JSON данных
     * @return ExchangeStrategies, содержащий настроенные стратегии обмена
     */
    @Bean
    @Lazy
    public ExchangeStrategies httpExchangeStrategies(Jackson2JsonDecoder jackson2JsonDecoder) {
        return ExchangeStrategies.builder().codecs(configurer -> {
            configurer.defaultCodecs().jackson2JsonDecoder(jackson2JsonDecoder);
        }).build();
    }

    /**
     * Создает декодер Jackson2JsonDecoder для декодирования JSON объектов с использованием указанного объекта ObjectMapper.
     *
     * @param objectMapper объект ObjectMapper, используемый для декодирования JSON объектов
     * @return Jackson2JsonDecoder для декодирования JSON объектов
     */
    @Bean
    @Lazy
    public Jackson2JsonDecoder jackson2JsonDecoder(ObjectMapper objectMapper) {
        return new Jackson2JsonDecoder(objectMapper, MimeTypeUtils.parseMimeType(MediaType.ALL_VALUE));
    }

    /**
     * Создает объект {@code ObjectMapper} с настройками по умолчанию.
     *
     * @return объект ObjectMapper с настроенными параметрами по умолчанию
     */
    @Bean
    @Lazy
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    /**
     * Выполняет создание и настройку объекта {@code GroovyScriptEvaluator} для выполнения Groovy скриптов с использованием
     * переданного объекта ImportCustomizer.
     *
     * @param importCustomizer объект ImportCustomizer, содержащий настройки импорта для Groovy скриптов
     * @return GroovyScriptEvaluator для выполнения Groovy скриптов
     */
    @Bean
    @Lazy
    public GroovyScriptEvaluator groovyScriptEvaluator(ImportCustomizer importCustomizer) {
        GroovyScriptEvaluator groovyScriptEvaluator = new GroovyScriptEvaluator(ClassLoader.getSystemClassLoader());
        groovyScriptEvaluator.setCompilationCustomizers(importCustomizer);
        return groovyScriptEvaluator;
    }

    /**
     * Создает объект {@code ImportCustomizer} для импорта классов в Groovy скриптах.
     *
     * @return ImportCustomizer, содержащий настройки импорта для Groovy скриптов
     */
    @Bean
    @Lazy
    public ImportCustomizer importCustomizer() {
        ImportCustomizer importCustomizer = new ImportCustomizer();
        List.of(AutoTestContext.class, Predicate.class, RandomUtils.class, DateUtils.class, LocalDateTime.class, LocalTime.class, ZonedDateTime.class, ZoneId.class, DateTimeFormatter.class, ChronoUnit.class).forEach(clazz -> importCustomizer.addImport(clazz.getSimpleName(), clazz.getCanonicalName()));

        return importCustomizer;
    }

    /**
     * Создает объект {@code DocumentBuilderFactory} для использования в парсинге XML.
     *
     * @return DocumentBuilderFactory для создания DOM-деревьев из XML-документов
     */
    @Bean
    @Lazy
    @SneakyThrows
    public DocumentBuilderFactory documentBuilderFactory() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return documentBuilderFactory;
    }

    /**
     * Создает новый объект XPathFactory для работы с XPath выражениями.
     *
     * @return XPathFactory
     */
    @Bean
    @Lazy
    public XPathFactory xPathFactory() {
        return XPathFactory.newInstance();
    }
}
