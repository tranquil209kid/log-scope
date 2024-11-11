/*
 * Copyright (c) 2024 tranquil209kid
 * Licensed under the EUPL v1.2
 */

package io.logscope;

import io.logscope.message.MessageLevel;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public interface LogScopeSink {
    void logMessage(@NotNull MessageLevel level, @NotNull Text text);
}