package io.logscope.logging;

import io.logscope.LogScope;
import io.logscope.message.MessageLevel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogInterceptor {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static final Queue<CachedLogMessage> CACHED_MESSAGES = new ConcurrentLinkedQueue<>();

    public static void setupInterceptor(boolean isPreLaunch) {
        Logger rootLogger = LogManager.getRootLogger();

        AbstractAppender appender = new AbstractAppender(
                isPreLaunch ? "LogScopePreLaunchAppender" : "LogScopeOverlayAppender",
                null,
                PatternLayout.createDefaultLayout(),
                true,
                Property.EMPTY_ARRAY
        ) {
            @Override
            public void append(LogEvent event) {
                String timestamp = LocalTime.now().format(TIME_FORMATTER);
                String logMessage = String.format("[%s] [%s] %s",
                        timestamp,
                        event.getLevel().toString(),
                        event.getMessage().getFormattedMessage());

                MessageLevel level = switch (event.getLevel().toString()) {
                    case "ERROR", "FATAL" -> MessageLevel.SEVERE;
                    case "WARN" -> MessageLevel.WARN;
                    default -> MessageLevel.INFO;
                };

                if (isPreLaunch) {
                    CACHED_MESSAGES.add(new CachedLogMessage(level, logMessage));
                } else {
                    if (MinecraftClient.getInstance() != null) {
                        MinecraftClient.getInstance().execute(() ->
                                LogScope.instance().logMessage(
                                        level,
                                        Text.literal(logMessage)
                                )
                        );
                    }
                }
            }
        };

        appender.start();
        ((org.apache.logging.log4j.core.Logger) rootLogger).addAppender(appender);
    }

    public static void processCachedMessages() {
        CACHED_MESSAGES.forEach(cached ->
                LogScope.instance().logMessage(
                        cached.level(),
                        Text.literal(cached.message())
                )
        );
        CACHED_MESSAGES.clear();
    }

    public record CachedLogMessage(MessageLevel level, String message) {}
}