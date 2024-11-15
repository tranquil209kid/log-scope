/*
 * Copyright (c) 2024 tranquil209kid
 * Licensed under the EUPL v1.2
 */

package io.logscope;

import io.logscope.color.ColorARGB;
import io.logscope.color.ColorU8;
import io.logscope.input.MouseInputHandler;
import io.logscope.message.Message;
import io.logscope.message.MessageLevel;
import io.logscope.util.Dim2i;
import io.logscope.widget.FlatButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

public class LogScopeRenderer {
    public static final LogScopeRenderer INSTANCE = new LogScopeRenderer();
    private final MouseInputHandler mouseHandler = new MouseInputHandler();
    private final LinkedList<ActiveMessage> activeMessages = new LinkedList<>();
    private final Map<MessageLevel, Boolean> messageFilters = new EnumMap<>(MessageLevel.class);
    private final List<FlatButtonWidget> filterButtons = new ArrayList<>();

    private static final int MAX_HEIGHT = 150;
    private static final int MAX_VISIBLE_MESSAGES = 6;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int CONSOLE_WIDTH = 300;
    private static final int CONSOLE_PADDING = 8;
    private static final int MESSAGE_SPACING = 2;
    private static final int HEADER_HEIGHT = 20;
    private static final int MESSAGE_PADDING = 6;
    private static final int FILTER_BUTTON_WIDTH = 60;
    private static final int FILTER_BUTTON_HEIGHT = 16;
    private static final int FILTER_BUTTON_SPACING = 4;

    private int scrollOffset = 0;
    private static boolean isVisible = false;
    private float animationProgress = 0.0f;
    private static final float ANIMATION_SPEED = 0.2f;

    public LogScopeRenderer() {
        for (MessageLevel level : MessageLevel.values()) {
            messageFilters.put(level, true);
        }
    }

    public void update(LogScope logScope, double currentTime) {
        this.pollMessages(logScope, currentTime);
        var filteredMessages = activeMessages.stream()
                .filter(msg -> messageFilters.get(msg.level()))
                .collect(Collectors.toList());
        this.mouseHandler.updateState(filteredMessages.size(), MAX_VISIBLE_MESSAGES);

        if (isVisible && animationProgress < 1.0f) {
            animationProgress = Math.min(1.0f, animationProgress + ANIMATION_SPEED);
        } else if (!isVisible && animationProgress > 0.0f) {
            animationProgress = Math.max(0.0f, animationProgress - ANIMATION_SPEED);
        }
    }

    private void pollMessages(LogScope logScope, double currentTime) {
        var log = logScope.getMessageDrain();
        while (!log.isEmpty()) {
            this.activeMessages.add(ActiveMessage.create(log.poll(), currentTime));
        }
    }

    private void initFilterButtons(int x, int y) {
        filterButtons.clear();
        int buttonX = x + CONSOLE_WIDTH - (MessageLevel.values().length * (FILTER_BUTTON_WIDTH + FILTER_BUTTON_SPACING));

        for (MessageLevel level : MessageLevel.values()) {
            FlatButtonWidget button = new FlatButtonWidget(
                    new Dim2i(buttonX, y + 2, FILTER_BUTTON_WIDTH, FILTER_BUTTON_HEIGHT),
                    Text.literal(level.name()),
                    () -> toggleFilter(level)
            );

            var style = FlatButtonWidget.Style.defaults();
            style.bgDefault = getFilterButtonColor(level, false);
            style.bgHovered = getFilterButtonColor(level, true);
            button.setStyle(style);
            button.setSelected(messageFilters.get(level));

            filterButtons.add(button);
            buttonX += FILTER_BUTTON_WIDTH + FILTER_BUTTON_SPACING;
        }
    }

    private int getFilterButtonColor(MessageLevel level, boolean hovered) {
        int alpha = hovered ? 0xE0 : 0x90;
        return switch (level) {
            case INFO -> ColorARGB.pack(76, 175, 80, alpha);
            case WARN -> ColorARGB.pack(255, 152, 0, alpha);
            case SEVERE -> ColorARGB.pack(244, 67, 54, alpha);
        };
    }

    private void toggleFilter(MessageLevel level) {
        messageFilters.put(level, !messageFilters.get(level));
        filterButtons.stream()
                .filter(button -> button.getLabel().getString().equals(level.name()))
                .findFirst()
                .ifPresent(button -> button.setSelected(messageFilters.get(level)));
    }

    public void handleScroll(double mouseX, double mouseY, double amount) {
        if (isMouseOverConsole(mouseX, mouseY)) {
            var filteredCount = activeMessages.stream()
                    .filter(msg -> messageFilters.get(msg.level()))
                    .count();
            scrollOffset = MathHelper.clamp(scrollOffset - (int)amount,
                    0,
                    Math.max(0, (int)filteredCount - MAX_VISIBLE_MESSAGES));
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        for (FlatButtonWidget button : filterButtons) {
            if (button.mouseClicked(mouseX, mouseY, 0)) {
                return true;
            }
        }
        if (isMouseOverScrollbar(mouseX, mouseY)) {
            mouseHandler.startDrag(mouseY);
            mouseDragged(mouseX, mouseY);
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        mouseHandler.endDrag();
    }

    public void mouseDragged(double mouseX, double mouseY) {
        if (mouseHandler.isDragging()) {
            var filteredCount = activeMessages.stream()
                    .filter(msg -> messageFilters.get(msg.level()))
                    .count();
            mouseHandler.updateDrag(mouseY, (int)filteredCount, MAX_VISIBLE_MESSAGES);
            scrollOffset = mouseHandler.getScrollOffset((int)filteredCount, MAX_VISIBLE_MESSAGES);
        }
    }

    private boolean isMouseOverConsole(double mouseX, double mouseY) {
        return mouseX >= CONSOLE_PADDING &&
                mouseX < CONSOLE_PADDING + CONSOLE_WIDTH &&
                mouseY >= CONSOLE_PADDING &&
                mouseY < CONSOLE_PADDING + MAX_HEIGHT;
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        return mouseX >= CONSOLE_PADDING + CONSOLE_WIDTH - SCROLLBAR_WIDTH &&
                mouseX < CONSOLE_PADDING + CONSOLE_WIDTH &&
                mouseY >= CONSOLE_PADDING + HEADER_HEIGHT &&
                mouseY < CONSOLE_PADDING + MAX_HEIGHT;
    }

    public static void toggleVisibility() {
        isVisible = !isVisible;
    }

    public static boolean isVisible() {
        return isVisible;
    }

    public void draw(DrawContext context) {
        if (animationProgress <= 0.0f) return;

        var currentTime = GLFW.glfwGetTime();
        MinecraftClient client = MinecraftClient.getInstance();
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0f, 0.0f, 1000.0f);

        int x = CONSOLE_PADDING;
        int y = CONSOLE_PADDING;

        drawConsoleBackground(context, x, y);
        drawConsoleHeader(context, client, x, y);

        if (filterButtons.isEmpty()) {
            initFilterButtons(x, y);
        }

        for (FlatButtonWidget button : filterButtons) {
            button.render(context, (int) client.mouse.getX(), (int) client.mouse.getY(), 0);
        }

        y += HEADER_HEIGHT + 2;
        int availableHeight = MAX_HEIGHT - HEADER_HEIGHT - CONSOLE_PADDING * 2;

        List<ActiveMessage> filteredMessages = activeMessages.stream()
                .filter(msg -> messageFilters.get(msg.level()))
                .collect(Collectors.toList());

        List<ActiveMessage> visibleMessages = new ArrayList<>();
        int endIndex = Math.min(filteredMessages.size(), scrollOffset + MAX_VISIBLE_MESSAGES);
        for (int i = scrollOffset; i < endIndex; i++) {
            visibleMessages.add(filteredMessages.get(i));
        }

        int contentY = y;
        for (ActiveMessage message : visibleMessages) {
            double opacity = getMessageOpacity(message, currentTime) * animationProgress;
            if (opacity < 0.025D) continue;

            List<OrderedText> lines = new ArrayList<>();
            var messageWidth = CONSOLE_WIDTH - SCROLLBAR_WIDTH - MESSAGE_PADDING * 2;

            TextHandler textHandler = client.textRenderer.getTextHandler();
            textHandler.wrapLines(message.text(), messageWidth, Style.EMPTY, (text, lastLineWrapped) -> {
                lines.add(Language.getInstance().reorder(text));
            });

            var messageHeight = (client.textRenderer.fontHeight * lines.size()) + (MESSAGE_SPACING * 2);

            if (contentY + messageHeight > y + availableHeight) {
                break;
            }

            drawMessage(context, client, x, contentY, messageWidth + MESSAGE_PADDING, messageHeight,
                    message.level(), lines, opacity);
            contentY += messageHeight + MESSAGE_SPACING;
        }

        if (filteredMessages.size() > MAX_VISIBLE_MESSAGES) {
            drawScrollbar(context, x + CONSOLE_WIDTH - SCROLLBAR_WIDTH,
                    y, SCROLLBAR_WIDTH,
                    availableHeight,
                    scrollOffset / (float)(filteredMessages.size() - MAX_VISIBLE_MESSAGES));
        }

        matrices.pop();
    }

    private void drawConsoleBackground(DrawContext context, int x, int y) {
        float alpha = 0.95f * animationProgress;

        context.fill(x, y, x + CONSOLE_WIDTH, y + MAX_HEIGHT,
                ColorARGB.pack(20, 20, 20, (int)(alpha * 255)));

        context.fill(x, y, x + CONSOLE_WIDTH, y + 1,
                ColorARGB.pack(70, 70, 70, (int)(alpha * 255)));
        context.fill(x, y + MAX_HEIGHT - 1, x + CONSOLE_WIDTH, y + MAX_HEIGHT,
                ColorARGB.pack(70, 70, 70, (int)(alpha * 255)));
        context.fill(x, y, x + 1, y + MAX_HEIGHT,
                ColorARGB.pack(70, 70, 70, (int)(alpha * 255)));
        context.fill(x + CONSOLE_WIDTH - 1, y, x + CONSOLE_WIDTH, y + MAX_HEIGHT,
                ColorARGB.pack(70, 70, 70, (int)(alpha * 255)));
    }

    private void drawConsoleHeader(DrawContext context, MinecraftClient client, int x, int y) {
        float alpha = animationProgress;

        context.fill(x, y, x + CONSOLE_WIDTH, y + HEADER_HEIGHT,
                ColorARGB.pack(30, 30, 30, (int)(alpha * 255)));

        context.drawTextWithShadow(client.textRenderer, Text.literal("Logs"),
                x + 8, y + 6, ColorARGB.pack(200, 200, 200, (int)(alpha * 255)));

        context.fill(x, y + HEADER_HEIGHT, x + CONSOLE_WIDTH, y + HEADER_HEIGHT + 1,
                ColorARGB.pack(50, 50, 50, (int)(alpha * 255)));
    }

    private void drawMessage(DrawContext context, MinecraftClient client, int x, int y,
                             int width, int height, MessageLevel level,
                             List<OrderedText> lines, double opacity) {
        var colors = COLORS.get(level);
        int alpha = weightAlpha(opacity);

        int messageWidth = Math.min(width, CONSOLE_WIDTH - SCROLLBAR_WIDTH - MESSAGE_PADDING * 2);

        context.fill(x, y, x + messageWidth, y + height,
                ColorARGB.withAlpha(colors.background(), alpha));
        context.fill(x, y, x + 2, y + height,
                ColorARGB.withAlpha(colors.foreground(), alpha));

        int textY = y + MESSAGE_SPACING;
        int textX = x + MESSAGE_PADDING;

        for (OrderedText line : lines) {
            context.drawText(client.textRenderer, line,
                    textX, textY,
                    ColorARGB.withAlpha(colors.text(), alpha), false);
            textY += client.textRenderer.fontHeight;
        }
    }

    private void drawScrollbar(DrawContext context, int x, int y, int width, int height, float progress) {
        float alpha = animationProgress;

        context.fill(x, y, x + width, y + height,
                ColorARGB.pack(40, 40, 40, (int)(alpha * 200)));

        int totalMessages = (int)activeMessages.stream()
                .filter(msg -> messageFilters.get(msg.level()))
                .count();
        int visiblePercentage = Math.min(100, (MAX_VISIBLE_MESSAGES * 100) / Math.max(1, totalMessages));
        int handleHeight = Math.max(30, (height * visiblePercentage) / 100);
        int handleY = y + (int)((height - handleHeight) * progress);

        int handleColor = mouseHandler.isDragging() ?
                ColorARGB.pack(180, 180, 180, (int)(alpha * 200)) :
                ColorARGB.pack(140, 140, 140, (int)(alpha * 200));

        context.fill(x + 1, handleY, x + width - 1, handleY + handleHeight, handleColor);
    }

    private static double getMessageOpacity(ActiveMessage message, double time) {
        return 1.0;
    }

    private static int weightAlpha(double scale) {
        return ColorU8.normalizedFloatToByte((float) scale);
    }

    private record ActiveMessage(MessageLevel level, Text text, double timestamp) {
        public static ActiveMessage create(Message message, double timestamp) {
            var text = message.text()
                    .copy()
                    .styled((style) -> style.withFont(MinecraftClient.UNICODE_FONT_ID));
            return new ActiveMessage(message.level(), text, timestamp);
        }
    }

    private static final EnumMap<MessageLevel, ColorPalette> COLORS = new EnumMap<>(MessageLevel.class);

    static {
        COLORS.put(MessageLevel.INFO, new ColorPalette(
                ColorARGB.pack(220, 220, 220),
                ColorARGB.pack(76, 175, 80),
                ColorARGB.pack(25, 25, 25)
        ));

        COLORS.put(MessageLevel.WARN, new ColorPalette(
                ColorARGB.pack(255, 235, 180),
                ColorARGB.pack(255, 152, 0),
                ColorARGB.pack(30, 25, 0)
        ));

        COLORS.put(MessageLevel.SEVERE, new ColorPalette(
                ColorARGB.pack(255, 180, 180),
                ColorARGB.pack(244, 67, 54),
                ColorARGB.pack(35, 20, 20)
        ));
    }

    private record ColorPalette(int text, int foreground, int background) {}
}