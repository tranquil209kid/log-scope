package io.logscope;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class LogScopePreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        LogInterceptor.setupInterceptor(true);
    }
}