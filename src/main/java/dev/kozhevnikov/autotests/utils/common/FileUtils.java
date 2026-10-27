package dev.kozhevnikov.autotests.utils.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.exception.AutotestException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс для работы с файлами.
 */
@Component
@Lazy
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class FileUtils {
    /**
     * Возвращает путь к файлу по его имени.
     *
     * @param fileName имя файла
     * @return путь к файлу
     */
    public File getResourceFile(String fileName) {
        return getFile(CommonFilePaths.TEST_FILES, fileName);
    }

    /**
     * Возвращает файл по указанному пути и имени файла.
     *
     * @param filePathWithOutName путь к файлу
     * @param fileName            имя файла
     * @return файл
     * @throws dev.kozhevnikov.autotests.exception.AutotestException если файл не найден
     */
    public File getFile(String filePathWithOutName, String fileName) {
        String fullPath = Path.of(filePathWithOutName, fileName).toString();
        int lastIndexSlash = fullPath.lastIndexOf(File.separator);

        String path = fullPath.substring(0, lastIndexSlash);
        String name = fullPath.substring(lastIndexSlash + 1);

        try (Stream<Path> files = Files.walk(Paths.get(path))) {
            return files.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> file.getName().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new AutotestException(String.format("Не удалось найти файл '%s' в %s", name, path)));
        } catch (Exception e) {
            throw new AutotestException(e);
        }
    }

    /**
     * Возвращает содержимое файла в виде Map, используя указанный путь к файлу.
     *
     * @param filePath путь к файлу
     * @return Map
     * @throws dev.kozhevnikov.autotests.exception.AutotestException если произошла ошибка ввода-вывода при обработке файла
     */
    public Map<String, String> getResourceTemplateFileAsMap(Path filePath) {
        Map<String, String> map = new HashMap<>();
        try (Stream<String> lines = Files.lines(Paths.get(CommonFilePaths.TEST_TEMPLATES + filePath))) {
            lines.forEach(line -> {
                String[] keyValuePair = line.split("=", 2);
                map.put(keyValuePair[0], keyValuePair[1]);
            });
        } catch (IOException e) {
            throw new AutotestException(e);
        }
        return map;
    }

    /**
     * Возвращает содержимое файла шаблона расположенного в папке test по указанному относительному пути.
     *
     * @param relativePath относительный путь к файлу шаблона
     * @return содержимое файла шаблона в виде строки
     */
    public String readResourceTemplate(String relativePath) {
        return readFile(new File(CommonFilePaths.TEST_TEMPLATES + relativePath));
    }

    /**
     * Возвращает содержимое файла шаблона расположенного в папке main по указанному относительному пути.
     *
     * @param relativePath относительный путь к файлу шаблона
     * @return содержимое файла шаблона в виде строки
     */
    public String readMainResourceTemplate(String relativePath) {
        return readFile(new File(CommonFilePaths.MAIN_TEMPLATES + relativePath));
    }

    /**
     * Cчитывает содержимое файла и возвращает его в виде строки.
     *
     * @param file файл, содержимое которого нужно прочитать
     * @return содержимое файла в виде строки
     * @throws dev.kozhevnikov.autotests.exception.AutotestException если произошла ошибка при чтении файла
     */
    public String readFile(File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            return bufferedReader.lines().collect(Collectors.joining());
        } catch (Exception e) {
            throw new AutotestException(e);
        }
    }

    /**
     * Возвращает количество строк в указанном файле.
     *
     * @param file файл, для которого нужно получить количество строк
     * @return количество строк в файле
     * @throws dev.kozhevnikov.autotests.exception.AutotestException если произошла ошибка при чтении файла
     */
    public long getNumberOfRows(File file) {
        try (Stream<String> lines = Files.lines(file.toPath(), StandardCharsets.ISO_8859_1)) {
            return lines.count();
        } catch (IOException e) {
            throw new AutotestException(e);
        }
    }

    /**
     * Конвертирует содержимое указанного файла в формат Base64.
     *
     * @param file файл, содержимое которого нужно конвертировать
     * @return содержимое файла в формате Base64 в виде строки символов
     */
    public String toBase64(File file) {
        return Base64.getEncoder().encodeToString(toByteArray(file));
    }

    /**
     * Считывает файла и возвращает его содержимое в виде массива байтов.
     *
     * @param file файл, содержимое которого нужно преобразовать в массив байтов
     * @return массива байтов
     * @throws dev.kozhevnikov.autotests.exception.AutotestException если произошла ошибка при чтении файла
     */
    public byte[] toByteArray(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            throw new AutotestException(e);
        }
    }

    /**
     * Загружает изображение из указанного URL, сохраняет его под указанным именем файла и в указанном формате.
     *
     * @param url        URL, указывающий на изображение
     * @param nameFile   имя файла, под которым нужно сохранить изображение
     * @param formatName формат, в котором нужно сохранить изображение
     */
    @SneakyThrows
    public void saveImage(URL url, String nameFile, String formatName) {
        BufferedImage image = ImageIO.read(url);
        saveImage(image, nameFile, formatName);
    }

    @SneakyThrows
    private void saveImage(BufferedImage image, String nameFile, String formatName) {
        ImageIO.write(image, formatName, new File(CommonFilePaths.TEST_FILES + nameFile + "." + formatName));
    }

    /**
     * Метод сохраняет предоставленный JSON-контент в файл с заданным именем в директории TEST_FILES.
     * Если файл с таким именем уже существует, то он будет перезаписан.
     * Расширение файла автоматически добавляется как ".json".
     *
     * @param fileName    имя файла, в который нужно сохранить содержимое JSON
     * @param jsonContent содержимое JSON для сохранения в файл
     */
    @SneakyThrows
    public void saveJsonToFile(String fileName, String jsonContent) {
        Files.writeString(Paths.get(CommonFilePaths.TEST_FILES + fileName + ".json"),
                jsonContent);
        log.info("Файл '{}' успешно сохранен", fileName);
    }

    /**
     * Метод удаляет файл с заданным именем из каталога TEST_FILES.
     * Расширение файла автоматически добавляется как ".json".
     *
     * @param fileName имя файла для удаления
     */
    @SneakyThrows
    public void deleteJsonFile(String fileName) {
        Files.delete(Paths.get(CommonFilePaths.TEST_FILES + fileName + ".json"));
        log.info("Файл '{}' успешно удален", fileName);
    }
}