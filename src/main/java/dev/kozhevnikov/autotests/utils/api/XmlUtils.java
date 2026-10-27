package dev.kozhevnikov.autotests.utils.api;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import dev.kozhevnikov.autotests.exception.AutotestException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Обрабатывает данные в формате XML
 */
@Component
@Lazy
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class XmlUtils {

    private final DocumentBuilderFactory documentBuilderFactory;
    private final XPathFactory xPathFactory;

    /**
     * Получает значения по указанному пути из XML-файла.
     * @param xmlFile - строковое представление XML-файла
     * @param xPath - путь для поиска значения
     * @return значение найденное по указанному пути или {@code null}, если значение не найдено
     */
    @SneakyThrows
    public String getByPath(String xmlFile, String xPath) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(xmlFile.getBytes(StandardCharsets.UTF_8))) {
            Document doc = documentBuilderFactory.newDocumentBuilder().parse(input);
            doc.getDocumentElement().normalize();

            XPathExpression expression = xPathFactory.newXPath().compile(xPath);

            return (String) expression.evaluate(doc, XPathConstants.STRING);
        }
    }

    /**
     * Преобразует XML-строку к общему виду.
     * @param xml строковое представление XML-данных
     * @return преобразованная XML-строка
     */
    public String unify(String xml) {
        return xml
                .replace("\\\\n", "")
                .replaceAll("^[^<]*", "")
                .replaceAll("<\\?xml[^>]+>", "");
    }

}