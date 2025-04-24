package fr.libnaus.noctisui.utils;

public class MathsUtils {

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }


    public static float lerp(float start, float end, float percentage) {
        return start * (1.0F - percentage) + end * percentage;
    }

}
