/*
 * Copyright (c) 2024 tranquil209kid
 * Licensed under the EUPL v1.2
 */

package io.logscope.message;

import net.minecraft.text.Text;

public record Message(MessageLevel level, Text text) {

}