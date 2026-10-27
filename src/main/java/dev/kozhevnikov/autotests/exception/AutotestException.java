package dev.kozhevnikov.autotests.exception;

/**
 * Описывает исключения, выбрасываемые в ходе выполнения автотестов.
 */

public class AutotestException extends RuntimeException {
    /**
     * Конструктор для создания исключения с указанным сообщением.
     * @param message - сообщение с текстом
     */
    public AutotestException(String message) {
        super(message);
    }
    /**
     * Конструктор для создания исключения с указанным сообщением и причиной.
     * @param message - сообщение с текстом
     * @param cause - причина исключения
     */
    public AutotestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Конструктор для создания объекта исключения с указанной причиной.
     * @param cause причина исключения
     */
    public AutotestException(Throwable cause) {
        super(cause);
    }
}
