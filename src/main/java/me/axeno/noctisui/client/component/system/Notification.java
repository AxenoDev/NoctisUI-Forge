package me.axeno.noctisui.client.component.system;

import me.axeno.noctisui.client.common.QuickImports;
import me.axeno.noctisui.client.utils.Color;
import lombok.Getter;
import lombok.Setter;

/**
 * A single toast notification with slide, fade, and stack behaviour.
 */
@Getter
public class Notification implements QuickImports
{
    private static final long ENTER_MS = 240;
    private static final long EXIT_MS = 220;
    private static final float SLIDE_DISTANCE = 34f;
    private static final float Y_SPRING = 0.22f;

    private final NotificationType type;
    private final String id;
    private final String title;
    private final String message;
    private final Color color;
    private final long duration;
    private float animationProgress = 0f;
    private final long creationTime;
    @Setter
    private float targetY;
    private float currentY;
    @Setter
    private int stackCount = 1;
    @Getter
    private long lastStackTime;

    public Notification(String id, String title, String message, NotificationType type, long duration)
    {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.color = type.getAccent();
        this.duration = duration;
        this.creationTime = System.currentTimeMillis();
        this.lastStackTime = this.creationTime;
    }

    public void update()
    {
        long elapsedSinceCreation = System.currentTimeMillis() - creationTime;
        long elapsedSinceLastStack = System.currentTimeMillis() - lastStackTime;

        if (elapsedSinceCreation < ENTER_MS) {
            animationProgress = easeOutCubic(elapsedSinceCreation / (float) ENTER_MS);
        }
        else if (elapsedSinceLastStack > duration - EXIT_MS) {
            float fadeT = (elapsedSinceLastStack - (duration - EXIT_MS)) / (float) EXIT_MS;
            animationProgress = 1f - easeInCubic(fadeT);
        }
        else {
            animationProgress = 1f;
        }

        float deltaY = targetY - currentY;
        if (Math.abs(deltaY) > 0.05f) {
            currentY += deltaY * Y_SPRING;
            if (Math.abs(deltaY) < 0.4f) {
                currentY = targetY;
            }
        }
        else {
            currentY = targetY;
        }
    }

    public boolean shouldRemove()
    {
        return System.currentTimeMillis() - lastStackTime > duration;
    }

    public float getSlideOffset()
    {
        long elapsedSinceCreation = System.currentTimeMillis() - creationTime;
        long elapsedSinceLastStack = System.currentTimeMillis() - lastStackTime;

        if (elapsedSinceCreation < ENTER_MS) {
            float t = easeOutCubic(elapsedSinceCreation / (float) ENTER_MS);
            return (1f - t) * SLIDE_DISTANCE;
        }

        if (elapsedSinceLastStack > duration - EXIT_MS) {
            float fadeT = (elapsedSinceLastStack - (duration - EXIT_MS)) / (float) EXIT_MS;
            return easeInCubic(fadeT) * SLIDE_DISTANCE;
        }

        return 0f;
    }

    public float getScale()
    {
        long elapsedSinceCreation = System.currentTimeMillis() - creationTime;
        if (elapsedSinceCreation < ENTER_MS) {
            float t = easeOutCubic(elapsedSinceCreation / (float) ENTER_MS);
            return 0.96f + 0.04f * t;
        }
        return 1f;
    }

    public float getAlpha()
    {
        return Math.max(0f, Math.min(1f, animationProgress));
    }

    public boolean isSimilarTo(Notification other)
    {
        return other != null && this.id.equals(other.id);
    }

    public void incrementStack()
    {
        this.stackCount++;
        this.lastStackTime = System.currentTimeMillis();
    }

    public boolean hasStack()
    {
        return stackCount > 1;
    }

    public float getProgress()
    {
        long elapsed = System.currentTimeMillis() - lastStackTime;
        return Math.min(elapsed / (float) duration, 1f);
    }

    private static float easeOutCubic(float t)
    {
        float c = 1f - t;
        return 1f - c * c * c;
    }

    private static float easeInCubic(float t)
    {
        return t * t * t;
    }
}
