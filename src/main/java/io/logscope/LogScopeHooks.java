package io.logscope;

import net.minecraft.client.gui.DrawContext;

public class LogScopeHooks {
    public static void render(DrawContext drawContext, double currentTime) {
        LogScopeRenderer.INSTANCE.update(LogScope.INSTANCE, currentTime);
        LogScopeRenderer.INSTANCE.draw(drawContext);
    }
}