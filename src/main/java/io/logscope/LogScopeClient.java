package io.logscope;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogScopeClient implements ClientModInitializer {
	public static final String MOD_ID = "logscope";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LogInterceptor.processCachedMessages();
		LogInterceptor.setupInterceptor(false);
	}
}