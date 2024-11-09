package io.logscope;

import io.logscope.message.MessageLevel;
import net.fabricmc.api.ClientModInitializer;
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

public class LogScopeClient implements ClientModInitializer {
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final double LOG_DURATION = Double.MAX_VALUE;

	@Override
	public void onInitializeClient() {
		setupLogInterceptor();
	}

	private void setupLogInterceptor() {
		Logger rootLogger = LogManager.getRootLogger();

		AbstractAppender appender = new AbstractAppender(
				"ConsoleOverlayAppender",
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

				if (MinecraftClient.getInstance() != null) {
					MinecraftClient.getInstance().execute(() ->
							LogScope.instance().logMessage(
									level,
									Text.literal(logMessage),
									LOG_DURATION
							)
					);
				}
			}
		};

		appender.start();
		((org.apache.logging.log4j.core.Logger) rootLogger).addAppender(appender);
	}
}