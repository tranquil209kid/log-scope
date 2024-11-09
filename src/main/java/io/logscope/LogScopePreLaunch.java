package io.logscope;

import io.logscope.logging.LogInterceptor;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class LogScopePreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        LogInterceptor.setupInterceptor(true);
    }
}