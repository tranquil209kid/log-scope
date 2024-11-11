/*
 * Copyright (c) 2024 tranquil209kid
 * Licensed under the EUPL v1.2
 */

package io.logscope.mixin.client.gui;

import io.logscope.LogScopeRenderer;
import io.logscope.util.Dim2i;
import io.logscope.widget.FlatButtonWidget;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    private FlatButtonWidget logsButton;

    @Inject(method = "init", at = @At("RETURN"))
    private void addLogsButton(CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;

        int buttonWidth = 100;
        int buttonHeight = 20;
        int padding = 5;

        Dim2i buttonDim = new Dim2i(
                screen.width - buttonWidth - padding,
                padding,
                buttonWidth,
                buttonHeight
        );

        logsButton = new FlatButtonWidget(
                buttonDim,
                getButtonText(),
                this::toggleLogs
        );

        screen.addDrawableChild(logsButton);
    }

    private void toggleLogs() {
        LogScopeRenderer.toggleVisibility();
        if (logsButton != null) {
            logsButton.setLabel(getButtonText());
        }
    }

    private Text getButtonText() {
        return Text.literal(LogScopeRenderer.isVisible() ? "Hide Logs" : "Show Logs");
    }
}