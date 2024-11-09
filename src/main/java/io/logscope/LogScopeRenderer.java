package io.logscope;

import io.logscope.color.ColorARGB;
import io.logscope.color.ColorU8;
import io.logscope.message.Message;
import io.logscope.message.MessageLevel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

public class LogScopeRenderer {
    public static final LogScopeRenderer INSTANCE = new LogScopeRenderer();

    private final LinkedList<ActiveMessage> activeMessages = new LinkedList<>();
    private static final int MAX_HEIGHT = 120;
    private static final int MAX_VISIBLE_MESSAGES = 5;
    private static final int SCROLLBAR_WIDTH = 4;
    private int scrollOffset = 0;
    private boolean isDraggingScrollbar = false;
    private static boolean isVisible = false;
    private double lastMouseY = 0;

    public void update(LogScope logScope, double currentTime) {
        this.purgeMessages(currentTime);
        this.pollMessages(logScope, currentTime);
    }

    private void purgeMessages(double currentTime) {
        this.activeMessages.removeIf(message ->
                currentTime > message.timestamp() + message.duration());
    }

    private void pollMessages(LogScope logScope, double currentTime) {
        var log = logScope.getMessageDrain();

        while (!log.isEmpty()) {
            this.activeMessages.add(ActiveMessage.create(log.poll(), currentTime));
        }
    }

    public void handleScroll(double mouseX, double mouseY, double amount) {
        if (isMouseOverConsole(mouseX, mouseY)) {
            scrollOffset = MathHelper.clamp(scrollOffset - (int)amount,
                    0,
                    Math.max(0, activeMessages.size() - MAX_VISIBLE_MESSAGES));
        }
    }

    public void mouseClicked(double mouseX, double mouseY) {
        if (isMouseOverScrollbar(mouseX, mouseY)) {
            isDraggingScrollbar = true;
            lastMouseY = mouseY;
        }
    }

    public void mouseReleased() {
        isDraggingScrollbar = false;
    }

    public void mouseDragged(double mouseX, double mouseY) {
        if (isDraggingScrollbar) {
            double delta = mouseY - lastMouseY;
            int totalMessages = activeMessages.size();
            if (totalMessages > MAX_VISIBLE_MESSAGES) {
                double scrollFactor = delta / (MAX_HEIGHT * 1.0);
                int scrollAmount = (int)(scrollFactor * (totalMessages - MAX_VISIBLE_MESSAGES));
                scrollOffset = MathHelper.clamp(scrollOffset + scrollAmount,
                        0,
                        totalMessages - MAX_VISIBLE_MESSAGES);
            }
            lastMouseY = mouseY;
        }
    }

    private boolean isMouseOverConsole(double mouseX, double mouseY) {
        return mouseX >= 4 && mouseX < 274 && mouseY >= 4 && mouseY < MAX_HEIGHT + 4;
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        return mouseX >= 274 && mouseX < 274 + SCROLLBAR_WIDTH &&
                mouseY >= 4 && mouseY < MAX_HEIGHT + 4;
    }

    public static void toggleVisibility() {
        isVisible = !isVisible;
    }

    public static boolean isVisible() {
        return isVisible;
    }

    public void draw(DrawContext context) {
        if (!isVisible) return;
        var currentTime = GLFW.glfwGetTime();
        MinecraftClient client = MinecraftClient.getInstance();
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0f, 0.0f, 1000.0f);

        var paddingWidth = 3;
        var paddingHeight = 1;
        var renders = new ArrayList<MessageRender>();

        int x = 4;
        int y = 4;
        int totalHeight = 0;

        List<ActiveMessage> visibleMessages = new ArrayList<>();
        int endIndex = Math.min(activeMessages.size(), scrollOffset + MAX_VISIBLE_MESSAGES);
        for (int i = scrollOffset; i < endIndex; i++) {
            visibleMessages.add(activeMessages.get(i));
        }

        for (ActiveMessage message : visibleMessages) {
            double opacity = getMessageOpacity(message, currentTime);
            if (opacity < 0.025D) continue;

            List<OrderedText> lines = new ArrayList<>();
            var messageWidth = 270 - SCROLLBAR_WIDTH;

            TextHandler textHandler = client.textRenderer.getTextHandler();
            textHandler.wrapLines(message.text(), messageWidth - 20, Style.EMPTY, (text, lastLineWrapped) -> {
                lines.add(Language.getInstance().reorder(text));
            });

            var messageHeight = (client.textRenderer.fontHeight * lines.size()) + (paddingHeight * 2);
            if (totalHeight + messageHeight > MAX_HEIGHT) break;

            renders.add(new MessageRender(x, y, messageWidth, messageHeight, message.level(), lines, opacity));
            y += messageHeight;
            totalHeight += messageHeight;
        }

        renders.forEach(render -> {
            drawMessage(context, client, render, paddingWidth, paddingHeight);
        });

        if (activeMessages.size() > MAX_VISIBLE_MESSAGES) {
            drawScrollbar(context, 274, 4, SCROLLBAR_WIDTH, MAX_HEIGHT,
                    scrollOffset / (float)(activeMessages.size() - MAX_VISIBLE_MESSAGES));
        }

        matrices.pop();
    }

    private void drawMessage(DrawContext context, MinecraftClient client, MessageRender render,
                             int paddingWidth, int paddingHeight) {
        var colors = COLORS.get(render.level());
        var opacity = render.opacity();

        context.fill(render.x(), render.y(),
                render.x() + render.width(), render.y() + render.height(),
                ColorARGB.withAlpha(colors.background(), weightAlpha(opacity)));

        context.fill(render.x(), render.y(),
                render.x() + 1, render.y() + render.height(),
                ColorARGB.withAlpha(colors.foreground(), weightAlpha(opacity)));

        int textY = render.y() + paddingHeight;
        for (var line : render.lines()) {
            context.drawText(client.textRenderer, line,
                    render.x() + paddingWidth + 3, textY,
                    ColorARGB.withAlpha(colors.text(), weightAlpha(opacity)), false);
            textY += client.textRenderer.fontHeight;
        }
    }

    private void drawScrollbar(DrawContext context, int x, int y, int width, int height, float progress) {
        context.fill(x, y, x + width, y + height,
                ColorARGB.pack(30, 30, 30, 180));

        int handleHeight = Math.max(20, height / Math.max(1, activeMessages.size() / MAX_VISIBLE_MESSAGES));
        int handleY = y + (int)((height - handleHeight) * progress);
        context.fill(x, handleY, x + width, handleY + handleHeight,
                ColorARGB.pack(128, 128, 128, 200));
    }

    private static double getMessageOpacity(ActiveMessage message, double time) {
        double age = time - message.timestamp();
        double fadeStart = message.duration() - 1.0;

        if (age >= fadeStart) {
            return 1.0 - ((age - fadeStart) / 1.0);
        }
        return 1.0;
    }

    private static int weightAlpha(double scale) {
        return ColorU8.normalizedFloatToByte((float) scale);
    }

    private record ActiveMessage(MessageLevel level, Text text, double duration, double timestamp) {
        public static ActiveMessage create(Message message, double timestamp) {
            var text = message.text()
                    .copy()
                    .styled((style) -> style.withFont(MinecraftClient.UNICODE_FONT_ID));
            return new ActiveMessage(message.level(), text, message.duration(), timestamp);
        }
    }

    private static final EnumMap<MessageLevel, ColorPalette> COLORS = new EnumMap<>(MessageLevel.class);

    static {
        COLORS.put(MessageLevel.INFO, new ColorPalette(
                ColorARGB.pack(255, 255, 255),
                ColorARGB.pack(15, 15, 15),
                ColorARGB.pack(15, 15, 15)
        ));

        COLORS.put(MessageLevel.WARN, new ColorPalette(
                ColorARGB.pack(224, 187, 0),
                ColorARGB.pack(25, 21, 0),
                ColorARGB.pack(180, 150, 0)
        ));

        COLORS.put(MessageLevel.SEVERE, new ColorPalette(
                ColorARGB.pack(220, 0, 0),
                ColorARGB.pack(25, 0, 0),
                ColorARGB.pack(160, 0, 0)
        ));
    }

    private record ColorPalette(int text, int background, int foreground) {}
    private record MessageRender(int x, int y, int width, int height, MessageLevel level, List<OrderedText> lines, double opacity) {}
}