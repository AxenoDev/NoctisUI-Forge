package fr.libnaus.noctisui.utils;

public class Color3f {
    private final float r, g, b;

    public Color3f(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public float getRed() {
        return this.r / 255;
    }

    public float getGreen() {
        return this.g / 255;
    }

    public float getBlue() {
        return this.b / 255;
    }

    public int getRGB() {
        return -16777216 | (int) (this.r * 255) << 16 | (int) (this.g * 255) << 8 | (int) (this.b * 255);
    }
}
