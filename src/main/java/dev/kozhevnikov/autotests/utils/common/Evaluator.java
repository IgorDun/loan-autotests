package dev.kozhevnikov.autotests.utils.common;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Выполняет Groovy-скрипты для преобразования объектов.
 */
@Component
@Lazy
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Evaluator {

    private final GroovyScriptEvaluator groovyScriptEvaluator;

    /**
     * Выполняет указанную команду Groovy и возвращает результат в виде объекта заданного класса.
     * @param command команда Groovy, которую нужно выполнить
     * @param resultClazz класс, в который нужно преобразовать результат
     * @return результат выполнения команды Groovy, приведенный к указанному классу
     */
    public <T> T evaluate(String command, Class<T> resultClazz) {
        StaticScriptSource staticScriptSource = new StaticScriptSource(command);
        return resultClazz.cast(groovyScriptEvaluator.evaluate(staticScriptSource));
    }

    /**
     * Выполняет указанную команду Groovy с предоставленными аргументами и возвращает результат в виде объекта заданного класса.
     * @param command команда Groovy, которую нужно выполнить
     * @param resultClazz класс, в который нужно преобразовать результат
     * @param arguments Map из аргументов, которые будут переданы в команду Groovy
     * @return результат выполнения команды Groovy, приведенный к указанному классу
     */
    public <T> T evaluate(String command, Class<T> resultClazz, Map<String, Object> arguments) {
        StaticScriptSource staticScriptSource = new StaticScriptSource(command);
        return resultClazz.cast(groovyScriptEvaluator.evaluate(staticScriptSource, arguments));
    }
}

