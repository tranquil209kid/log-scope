package io.logscope.input;

import net.minecraft.util.math.MathHelper;

public class MouseInputHandler {
    private boolean isDragging = false;
    private double lastY = 0;
    private double lastDragY = 0;
    private double scrollProgress = 0.0;
    private static final int SCROLLBAR_TOP_MARGIN = 4;
    private static final int SCROLLABLE_HEIGHT = 120;

    private int totalMessages = 0;
    private int visibleMessages = 0;
    private int currentOffset = 0;

    public void startDrag(double mouseY) {
        isDragging = true;
        lastDragY = mouseY;
    }

    public void endDrag() {
        isDragging = false;
    }

    public void updateDrag(double mouseY, int totalMessages, int maxVisible) {
        if (!isDragging || totalMessages <= maxVisible) return;

        double normalizedY = (mouseY - SCROLLBAR_TOP_MARGIN) / SCROLLABLE_HEIGHT;
        normalizedY = MathHelper.clamp(normalizedY, 0.0, 1.0);

        scrollProgress = normalizedY;
        lastDragY = mouseY;
    }

    public void updateState(int totalMessages, int visibleMessages) {
        this.totalMessages = totalMessages;
        this.visibleMessages = visibleMessages;
    }

    public int getScrollOffset(int totalMessages, int maxVisible) {
        if (totalMessages <= maxVisible) return 0;
        return (int)(scrollProgress * (totalMessages - maxVisible));
    }

    public boolean isDragging() {
        return isDragging;
    }
}