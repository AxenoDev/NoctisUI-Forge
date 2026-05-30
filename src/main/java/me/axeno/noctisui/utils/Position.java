package me.axeno.noctisui.utils;

public class Position {

    Y y;
    X x;
    int offsetX;
    int offsetY;

    public Position(X position, int x) {
        this.x = position;
        this.offsetX = x;
    }

    public Position(Y position, int y) {
        this.y = position;
        this.offsetY = y;
    }

    public X getX() {
        return this.x;
    }

    public Y getY() {
        return this.y;
    }

    public int getOffsetX() {
        return this.offsetX;
    }

    public int getOffsetY() {
        return this.offsetY;
    }

    public static enum X {
        LEFT, CENTER, RIGHT;
        private X() {}
    }

    public static enum Y {
        TOP, CENTER, BOTTOM;
        private Y() {}
    }

}
