package me.axeno.noctisui.client.component.notification;

import me.axeno.noctisui.client.utils.Color;
import lombok.Getter;

/**
 * Notification categories with accent, icon, and badge styling.
 */
@Getter
public enum NotificationType
{
    SUCCESS(
            new Color(52, 211, 153),
            new Color(16, 185, 129),
            new Color(34, 80, 62),
            "\uE951",
            "Succès"
    ),
    ERROR(
            new Color(248, 113, 113),
            new Color(239, 68, 68),
            new Color(90, 38, 38),
            "\uEB15",
            "Erreur"
    ),
    WARNING(
            new Color(251, 191, 36),
            new Color(245, 158, 11),
            new Color(90, 62, 24),
            "\uE90A",
            "Alerte"
    ),
    INFO(
            new Color(96, 165, 250),
            new Color(59, 130, 246),
            new Color(28, 52, 96),
            "\uEA0C",
            "Info"
    );

    private final Color accent;
    private final Color accentBright;
    private final Color badgeBackground;
    private final String icon;
    private final String label;

    NotificationType(Color accent, Color accentBright, Color badgeBackground, String icon, String label)
    {
        this.accent = accent;
        this.accentBright = accentBright;
        this.badgeBackground = badgeBackground;
        this.icon = icon;
        this.label = label;
    }

    /** @deprecated use {@link #getAccent()} */
    @Deprecated
    public Color getDefaultColor()
    {
        return accent;
    }
}
