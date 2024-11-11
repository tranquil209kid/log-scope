/*
 * Copyright (c) 2024 tranquil209kid
 * Licensed under the EUPL v1.2
 */

package io.logscope;

import io.logscope.logging.LogInterceptor;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class LogScopePreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        LogInterceptor.setupInterceptor(true);
    }
}