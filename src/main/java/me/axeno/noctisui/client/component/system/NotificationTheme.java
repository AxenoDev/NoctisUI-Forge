package me.axeno.noctisui.client.component.system;

import me.axeno.noctisui.client.utils.Color;

/**
 * Server brand palette for notification chrome (card, text, progress).
 * Semantic type colors (success, error, …) live on {@link NotificationType}.
 */
public final class NotificationTheme
{
    public static final Color SURFACE_LIGHT = hex(0xF0F4F8);
    public static final Color SURFACE = hex(0x1F2833);
    public static final Color BACKGROUND = hex(0x0B0C10);
    public static final Color ACCENT = hex(0x0055FF);

    private NotificationTheme()
    {
    }

    public static Color hex(int rgb)
    {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    public static Color withAlpha(Color color, float alpha)
    {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * alpha));
    }
}
