package io.logscope.mixin.client.render;

import io.logscope.LogScopeHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final MinecraftClient client;

	@Shadow @Final private BufferBuilderStorage buffers;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V", shift = At.Shift.AFTER))
	private void onRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		if (client.currentScreen instanceof TitleScreen) {
			this.client.getProfiler().push("logscope_overlay");

			DrawContext drawContext = new DrawContext(this.client, this.buffers.getEntityVertexConsumers());
			LogScopeHooks.render(drawContext, GLFW.glfwGetTime());
			drawContext.draw();

			this.client.getProfiler().pop();
		}
	}
}