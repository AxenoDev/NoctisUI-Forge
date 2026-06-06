package me.axeno.noctisui.client.component.system;

import me.axeno.noctisui.client.utils.Color;

/**
 * Visual tokens for notification cards in a simple dark-mode palette.
 * Semantic accent colors live on {@link NotificationType}.
 */
public final class NotificationTheme
{
    public static final Color TEXT_PRIMARY = hex(0xF4F7FB);
    public static final Color TEXT_SECONDARY = hex(0x9AA8BC);
    public static final Color TEXT_MUTED = hex(0x6B7A8F);

    public static final Color CARD_BACKGROUND = rgba(16, 18, 26, 242);
    public static final Color CARD_BORDER = rgba(255, 255, 255, 18);

    public static final Color CARD_BORDER_LIGHT = rgba(255, 255, 255, 12);
    public static final Color CARD_BORDER_DARK = rgba(0, 0, 0, 60);
    public static final Color PROGRESS_TRACK = rgba(255, 255, 255, 14);
    public static final Color STACK_BADGE_BG = rgba(255, 255, 255, 10);
    public static final Color STACK_BADGE_TEXT = hex(0xC8D4E4);

    private NotificationTheme()
    {
    }

    public static Color hex(int rgb)
    {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    public static Color rgba(int r, int g, int b, int a)
    {
        return new Color(r, g, b, a);
    }

    public static Color withAlpha(Color color, float alpha)
    {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * alpha));
    }

    public static Color mix(Color a, Color b, float t)
    {
        return Color.interpolateColor(a, b, Math.max(0f, Math.min(1f, t)));
    }

    public static Color tint(Color base, Color tint, float strength)
    {
        return mix(base, tint, strength);
    }
}
