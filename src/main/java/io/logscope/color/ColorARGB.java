package io.logscope.color;

public class ColorARGB implements ColorU8 {
    private static final int ALPHA_COMPONENT_OFFSET = 24;
    private static final int RED_COMPONENT_OFFSET = 16;
    private static final int GREEN_COMPONENT_OFFSET = 8;
    private static final int BLUE_COMPONENT_OFFSET = 0;

    public static int pack(int r, int g, int b, int a) {
        return (a & COMPONENT_MASK) << ALPHA_COMPONENT_OFFSET |
                (r & COMPONENT_MASK) << RED_COMPONENT_OFFSET |
                (g & COMPONENT_MASK) << GREEN_COMPONENT_OFFSET |
                (b & COMPONENT_MASK) << BLUE_COMPONENT_OFFSET;
    }

    public static int pack(int r, int g, int b) {
        return pack(r, g, b, (1 << ColorU8.COMPONENT_BITS) - 1);
    }

    public static int unpackAlpha(int color) {
        return color >> ALPHA_COMPONENT_OFFSET & COMPONENT_MASK;
    }

    public static int unpackRed(int color) {
        return color >> RED_COMPONENT_OFFSET & COMPONENT_MASK;
    }

    public static int unpackGreen(int color) {
        return color >> GREEN_COMPONENT_OFFSET & COMPONENT_MASK;
    }

    public static int unpackBlue(int color) {
        return color >> BLUE_COMPONENT_OFFSET & COMPONENT_MASK;
    }

    public static int toABGR(int color, int alpha) {
        return Integer.reverseBytes(color << 8 | alpha);
    }

    public static int toABGR(int color) {
        return Integer.reverseBytes(Integer.rotateLeft(color, 8));
    }

    public static int withAlpha(int rgb, int alpha) {
        return (alpha << ALPHA_COMPONENT_OFFSET) | (rgb & ~(COMPONENT_MASK << ALPHA_COMPONENT_OFFSET));
    }
}