/*
 * Copyright (c) 2024 tranquil209kid
 * Licensed under the EUPL v1.2
 */

package io.logscope.mixin.client.gui;

import io.logscope.LogScopeRenderer;
import io.logscope.util.Dim2i;
import io.logscope.widget.FlatButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import io.logscope.color.ColorARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static final Identifier CONSOLE_ICON = new Identifier("logscope", "icon_transparent.png");

    @Unique
    private FlatButtonWidget logsButton;

    @Unique
    private float pulseAnimation = 0.0f;

    @Unique
    private long lastUpdateTime;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addLogsButton(CallbackInfo ci) {
        lastUpdateTime = System.currentTimeMillis();

        int buttonWidth = 110;
        int buttonHeight = 24;
        int padding = 5;

        Dim2i buttonDim = new Dim2i(
                this.width - buttonWidth - padding,
                padding,
                buttonWidth,
                buttonHeight
        );

        logsButton = new FlatButtonWidget(buttonDim, getButtonText(), this::toggleLogs) {
            @Override
            public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                if (!this.visible) return;

                this.hovered = this.dim.containsCursor(mouseX, mouseY);
                long currentTime = System.currentTimeMillis();
                float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
                lastUpdateTime = currentTime;

                pulseAnimation = (pulseAnimation + deltaTime * 2) % 1.0f;
                float pulse = (float) Math.sin(pulseAnimation * Math.PI * 2) * 0.1f + 0.9f;

                int baseColor = LogScopeRenderer.isVisible() ? 0xFF94E4D3 : 0xFF5C7AE6;
                int pulseColor = ColorARGB.withAlpha(baseColor, (int)(255 * pulse));
                int bgColor = this.hovered ? 0x90000000 : 0x70000000;
                int borderColor = this.hovered ? pulseColor : ColorARGB.withAlpha(baseColor, 180);

                var matrices = context.getMatrices();
                matrices.push();
                matrices.translate(0, 0, 100);

                context.fill(this.dim.x(), this.dim.y(), this.dim.getLimitX(), this.dim.getLimitY(), bgColor);

                MinecraftClient client = MinecraftClient.getInstance();
                int textWidth = client.textRenderer.getWidth(this.getLabel());
                int iconSize = 16;
                int iconPadding = 4;
                int totalWidth = textWidth + iconSize + iconPadding;
                int centerX = this.dim.x() + (this.dim.width() - totalWidth) / 2;
                int centerY = this.dim.y() + (this.dim.height() - iconSize) / 2;

                context.drawText(
                        client.textRenderer,
                        this.getLabel(),
                        centerX + iconSize + iconPadding,
                        this.dim.getCenterY() - 4,
                        this.hovered ? 0xFFFFFFFF : 0xE0FFFFFF,
                        true
                );

                float iconBrightness = this.hovered ? 1.0f : 0.9f;
                context.setShaderColor(iconBrightness, iconBrightness, iconBrightness, 1.0f);
                context.drawTexture(CONSOLE_ICON, centerX, centerY, 0, 0, 16, 16, 16, 16);
                context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

                int glowIntensity = this.hovered ? 40 : 20;
                for (int i = 0; i < 2; i++) {
                    context.fill(
                            this.dim.x() + i,
                            this.dim.y() + i,
                            this.dim.getLimitX() - i,
                            this.dim.getLimitY() - i,
                            ColorARGB.withAlpha(borderColor, glowIntensity - (i * 10))
                    );
                }

                if (LogScopeRenderer.isVisible()) {
                    int indicatorSize = 6;
                    int indicatorPadding = 4;
                    int indicatorX = this.dim.getLimitX() - indicatorSize - indicatorPadding;
                    int indicatorY = this.dim.y() + indicatorPadding;

                    float glowSize = 2.0f + (float)Math.sin(pulseAnimation * Math.PI * 2) * 0.5f;
                    for (float i = glowSize; i >= 0; i -= 0.5f) {
                        float glowAlpha = (1.0f - (i / glowSize)) * 0.3f;
                        context.fill(
                                (int)(indicatorX - i),
                                (int)(indicatorY - i),
                                (int)(indicatorX + indicatorSize + i),
                                (int)(indicatorY + indicatorSize + i),
                                ColorARGB.withAlpha(pulseColor, (int)(glowAlpha * 255))
                        );
                    }

                    context.fill(
                            indicatorX,
                            indicatorY,
                            indicatorX + indicatorSize,
                            indicatorY + indicatorSize,
                            pulseColor
                    );
                }

                matrices.pop();
            }
        };

        FlatButtonWidget.Style style = FlatButtonWidget.Style.defaults();
        style.bgDefault = 0x70000000;
        style.bgHovered = 0x90000000;
        logsButton.setStyle(style);

        this.addDrawableChild(logsButton);
    }

    @Unique
    private void toggleLogs() {
        LogScopeRenderer.toggleVisibility();
        if (logsButton != null) {
            logsButton.setLabel(getButtonText());
        }
    }

    @Unique
    private Text getButtonText() {
        return Text.literal(LogScopeRenderer.isVisible() ? "Hide Logs" : "Show Logs");
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void updatePulseAnimation(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (logsButton != null && logsButton.isHovered()) {
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
            lastUpdateTime = currentTime;
            pulseAnimation = (pulseAnimation + deltaTime * 2) % 1.0f;
        }
    }
}