package me.axeno.noctisui.client.utils;

public class MathUtils
{

    public static float lerp(float start, float end, float factor)
    {
        return start + (end - start) * Math.min(factor, 1f);
    }

    public static float clamp(float value, float min, float max)
    {
        return Math.max(min, Math.min(max, value));
    }

    public static float ease(float t)
    {
        return t < 0.5f ? 4f * t * t * t : (float) (1f - Math.pow(-2f * t + 2f, 3f) / 2f);
    }

}
