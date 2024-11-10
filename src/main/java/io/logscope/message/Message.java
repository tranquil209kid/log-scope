package io.logscope.message;

import net.minecraft.text.Text;

public record Message(MessageLevel level, Text text) {

}