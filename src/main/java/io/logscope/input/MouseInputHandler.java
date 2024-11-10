package io.logscope.input;

import net.minecraft.util.math.MathHelper;

public class MouseInputHandler {
    private boolean isDragging = false;
    private double dragOffset = 0;
    private double scrollProgress = 0.0;
    private static final int SCROLLBAR_TOP_MARGIN = 4;
    private static final int SCROLLABLE_HEIGHT = 120;

    private int totalMessages = 0;
    private int visibleMessages = 0;
    private int currentOffset = 0;

    public void startDrag(double mouseY) {
        isDragging = true;

        double totalHeight = SCROLLABLE_HEIGHT;
        double handleHeight = Math.max(30, totalHeight * (visibleMessages / (double)Math.max(1, totalMessages)));
        double handlePosition = scrollProgress * (totalHeight - handleHeight);

        dragOffset = mouseY - (SCROLLBAR_TOP_MARGIN + handlePosition);
    }

    public void endDrag() {
        isDragging = false;
    }

    public void updateDrag(double mouseY, int totalMessages, int maxVisible) {
        if (!isDragging || totalMessages <= maxVisible) return;

        double adjustedMouseY = mouseY - dragOffset;

        double totalHeight = SCROLLABLE_HEIGHT;
        double handleHeight = Math.max(30, totalHeight * (maxVisible / (double)Math.max(1, totalMessages)));
        double scrollableArea = totalHeight - handleHeight;

        double normalizedY = (adjustedMouseY - SCROLLBAR_TOP_MARGIN) / scrollableArea;
        normalizedY = MathHelper.clamp(normalizedY, 0.0, 1.0);

        scrollProgress = normalizedY;
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