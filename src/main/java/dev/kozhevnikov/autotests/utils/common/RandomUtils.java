package dev.kozhevnikov.autotests.utils.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.math.BigInteger;

import static java.util.stream.Collectors.joining;
import static dev.kozhevnikov.autotests.utils.common.CommonFilePaths.TEST_FILES;

/**
 * Класс для генерации случайных тестовых данных по заданным правилам.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RandomUtils {
    private static final Random random = new Random();

    /**
     * Возвращает случайное целое число в заданном диапазоне.
     * @param min минимальное значение диапазона (включительно)
     * @param max максимальное значение диапазона (исключая)
     * @return случайное целое число в заданном диапазоне
     */
    public static int number(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    /**
     * Возвращает случайное целое число заданной длины.
     * @param length длина случайного числа
     * @return случайное целое число указанной длины
     */
    public static long number(long length) {
        long min = (long) Math.pow(10, (double) length - 1);
        long max = ((long) Math.pow(10, length)) - 1;
        return randomLong(min, max);
    }

    /**
     * Возвращает случайное десятизначное число, которое может использоваться в качестве номера паспорта.
     * @return случайное десятизначное число
     */
    public static long passport() {
        long min = 1000000000L;
        long max = 9999999999L;

        return min + (long) (Math.random() * (max - min));
    }

    /**
     * Генерирует случайное слово с заданной минимальной и максимальной длиной.
     * @param minLength минимальная длина слова
     * @param maxLength максимальная длина слова
     * @return случайно сгенерированное слово
     */
    public static String word(int minLength, int maxLength) {
        int minCharCode = 1072;
        int maxCharCode = 1103;

        return wordGenerate(minCharCode, maxCharCode, minLength, maxLength);
    }

    /**
     * Выбирает случайное имя из файла "name_test.txt".
     * @return случайно выбранное имя из файла
     * @throws IOException если произошла ошибка ввода-вывода при чтении файла
     */
    public static String selectRandomName() throws IOException {
        List<String> names = Files.readAllLines(Paths.get(TEST_FILES + "name_test.txt"));
        return getRandomElementFromList(names);
    }

    /**
     * Выбирает случайную фамилию из файла "surNames_test.txt".
     * @return случайно выбранная фамилия из файла
     * @throws IOException если произошла ошибка ввода-вывода при чтении файла
     */
    public static String selectRandomSurName() throws IOException {
        List<String> surNames = Files.readAllLines(Paths.get(TEST_FILES + "surNames_test.txt"));

        return getRandomElementFromList(surNames);
    }

    /**
     * Выбирает случайное отчество из файла "secondName_test.txt".
     * @return случайно выбранное отчество из файла
     * @throws IOException если произошла ошибка ввода-вывода при чтении файла
     */
    public static String selectRandomSecondName() throws IOException {
        List<String> secondName = Files.readAllLines(Paths.get(TEST_FILES + "secondName_test.txt"));

        return getRandomElementFromList(secondName);
    }

    /**
     * Генерирует случайное слово с первой заглавной буквой с заданной минимальной и максимальной длиной.
     * @param minLength минимальная длина слова
     * @param maxLength максимальная длина слова
     * @return случайно сгенерированное слово с первой заглавной буквой
     */
    public static String wordFirstUpperCase(int minLength, int maxLength) {
        int minCharUpperCode = 1040;
        int maxCharUpperCode = 1071;

        return (char) number(minCharUpperCode, maxCharUpperCode) + word(minLength, maxLength - 1);
    }

    /**
     * Генерирует случайное слово на английском языке с заданной минимальной и максимальной длиной.
     * @param minLength минимальная длина слова
     * @param maxLength максимальная длина слова
     * @return случайно сгенерированное слово на английском языке
     */
    public static String wordEng(int minLength, int maxLength) {
        int minCharCode = 97;
        int maxCharCode = 122;

        return wordGenerate(minCharCode, maxCharCode, minLength, maxLength);
    }

    private static String wordGenerate(int minCharCode, int maxCharCode, int minLength, int maxLength) {
        int wordLength = number(minLength, maxLength);

        StringBuilder wordBuilder = new StringBuilder(wordLength);
        for (int i = 0; i < wordLength; i++) {
            wordBuilder.append((char) number(minCharCode, maxCharCode));
        }

        return wordBuilder.toString();
    }

    /**
     * Генерирует случайный номер телефона.
     * @return случайно сгенерированный номер телефона
     */
    public static String phone() {
        return phone("");
    }

    /**
     * Генерирует случайный номер телефона с заданным префиксом.
     * @param prefix префикс номера телефона
     * @return случайно сгенерированный номер телефона префиксом
     */
    public static String phone(Object prefix) {
        long min = 9000000000L;
        long max = 9999999999L;

        return prefix.toString() + randomLong(min, max);
    }

    /**
     * Возвращает случайное целое число типа long в заданном диапазоне.
     * @param min минимальное значение диапазона (включительно)
     * @param max максимальное значение диапазона (исключая)
     * @return случайное целое число типа long в заданном диапазоне
     */
    public static long randomLong(long min, long max) {
        return min + (long) (Math.random() * (max - min));
    }

    /**
     * Возвращает случайное целое число типа int в заданном диапазоне.
     * @param min минимальное значение диапазона (включительно)
     * @param max максимальное значение диапазона (исключая)
     * @return случайное целое число типа int в заданном диапазоне
     */
    public static Integer randomInt(int min, int max) {
        return (int) (min + (Math.random() * (max - min)));
    }

    /**
     * Возвращает случайное логическое значение в виде строки.
     * @return "true" если сгенерированное значение меньше 0.5, иначе "false"
     */
    public static String randomBoolean() {
        return String.valueOf(Math.random() < 0.5);
    }

    /**
     * Генерирует уникальный идентификатор UUID.
     * @return уникальный идентификатор UUID в виде строки
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Генерирует случайный email адрес.
     * @return сгенерированный email адрес
     */
    public static String email() {
        return wordEng(10, 15) + "@test.test";
    }

    /**
     * Генерирует случайное ФИО (Фамилия, Имя, Отчество).
     * @return случайно сгенерированное ФИО
     */
    public static String randomFIO() {
        return wordFirstUpperCase(10, 15) + " " + wordFirstUpperCase(10, 15) + " " + wordFirstUpperCase(10, 15);
    }

    /**
     * Генерирует ИНН юридического лица.
     * @return ИНН юридического лица
     */
    public static String generateInnUL() {
        LinkedList<Integer> nums = new LinkedList<>();
        for (int i = 0; i < 9; i++) {
            nums.add(randomNumberForInn('u')[i]);
        }
        int n1 = getInnControlSumUL(nums, "n1_10");
        nums.add(n1);
        return nums.stream()
                .map(Object::toString)
                .collect(joining(""));
    }
    /**
     * Генерирует ИНН физического лица.
     * @return ИНН физического лица
     */
    public static String generateInnFL() {
        LinkedList<Integer> nums = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            nums.add(randomNumberForInn('f')[i]);
        }
        int n2 = getInnControlSumFl(nums, "n2_12");
        nums.add(n2);
        int n1 = getInnControlSumFl(nums, "n1_12");
        nums.add(n1);
        return nums.stream()
                .map(Object::toString)
                .collect(joining(""));
    }

    private static Integer getInnControlSumUL(List<Integer> nums, String type) {
        Map<String, List<Integer>> innControlType = Map.of(
                "n1_10", List.of(2, 4, 10, 3, 5, 9, 4, 6, 8)
        );
        int n = 0;
        List<Integer> l = innControlType.get(type);
        for (int i = 0; i < l.size(); i++) {
            n += nums.get(i) * l.get(i);
        }
        return n % 11 % 10;
    }

    private static Integer getInnControlSumFl(List<Integer> nums, String type) {
        Map<String, List<Integer>> innControlType = Map.of(
                "n2_12", List.of(7, 2, 4, 10, 3, 5, 9, 4, 6, 8),
                "n1_12", List.of(3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8)
        );
        int n = 0;
        List<Integer> l = innControlType.get(type);
        for (int i = 0; i < l.size(); i++) {
            n += nums.get(i) * l.get(i);
        }
        return n % 11 % 10;
    }

    private static Integer[] randomNumberForInn(char type) {
        StringBuilder innGen = new StringBuilder();
        int region = 10 + random.nextInt() * 75;
        int inspection = 10 + random.nextInt() * 89;
        int number = (type == 'u') ? (11111 + random.nextInt() * 77777) : (111111 + random.nextInt() * 777777);
        for (var i : new int[]{region, inspection, number}) {
            String s = String.valueOf(i);
            innGen.append(s);
        }
        Integer[] innGenToMassive = new Integer[innGen.length()];
        for (int j = 0; j < innGen.length(); j++) {
            innGenToMassive[j] = Math.abs(innGen.charAt(j) - '0');
        }
        return innGenToMassive;
    }

    private static String getRandomElementFromList(List<String> list) {
        Collections.shuffle(list);

        return list.get(0);
    }

    private static BigInteger randomBigInteger(BigInteger min, BigInteger max) {
        BigInteger bigInteger = max.subtract(min);
        int length = max.bitLength();
        BigInteger result = new BigInteger(length, random);
        if (result.compareTo(min) < 0) {
            result = result.add(min);
        }
        if (result.compareTo(bigInteger) >= 0) {
            result = result.mod(bigInteger).add(min);
        }
        return result;
    }
}
