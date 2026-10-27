package dev.kozhevnikov.autotests.logger;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import io.qameta.allure.Allure;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AllureAppender extends ConsoleAppender<LoggingEvent> {
    @Override
    protected void append(LoggingEvent event) {
        event.prepareForDeferredProcessing();

        String formattedDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Allure.attachment(String.format("%s [%s] %s", formattedDate, event.getLevel(), event.getLoggerName()), event.getFormattedMessage());
    }
}
