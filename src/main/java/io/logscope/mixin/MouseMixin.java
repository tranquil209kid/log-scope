package io.logscope.mixin;

import io.logscope.LogScopeRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.TitleScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow @Final private MinecraftClient client;

    private boolean shouldHandleInput() {
        return client.currentScreen instanceof TitleScreen &&
                LogScopeRenderer.isVisible();
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == MinecraftClient.getInstance().getWindow().getHandle() && shouldHandleInput()) {
            double mouseX = client.mouse.getX() / client.getWindow().getScaleFactor();
            double mouseY = client.mouse.getY() / client.getWindow().getScaleFactor();
            LogScopeRenderer.INSTANCE.handleScroll(mouseX, mouseY, vertical);
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (!shouldHandleInput()) return;

        double mouseX = client.mouse.getX() / client.getWindow().getScaleFactor();
        double mouseY = client.mouse.getY() / client.getWindow().getScaleFactor();

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (action == GLFW.GLFW_PRESS) {
                LogScopeRenderer.INSTANCE.mouseClicked(mouseX, mouseY);
            } else if (action == GLFW.GLFW_RELEASE) {
                LogScopeRenderer.INSTANCE.mouseReleased();
            }
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"))
    private void onMouseMove(long window, double x, double y, CallbackInfo ci) {
        if (!shouldHandleInput()) return;

        double mouseX = x / client.getWindow().getScaleFactor();
        double mouseY = y / client.getWindow().getScaleFactor();
        LogScopeRenderer.INSTANCE.mouseDragged(mouseX, mouseY);
    }
}