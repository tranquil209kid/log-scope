/*
 * Copyright (c) 2024 tranquil209kid
 * Licensed under the EUPL v1.2
 */

package io.logscope.util;

public record Dim2i(int x, int y, int width, int height) {
    public int getLimitX() {
        return this.x + this.width;
    }

    public int getLimitY() {
        return this.y + this.height;
    }

    public boolean containsCursor(double x, double y) {
        return x >= this.x && x < this.getLimitX() && y >= this.y && y < this.getLimitY();
    }

    public int getCenterX() {
        return this.x + (this.width / 2);
    }

    public int getCenterY() {
        return this.y + (this.height / 2);
    }
}
