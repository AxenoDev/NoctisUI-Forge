package me.axeno.noctisui.client.component.notification;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import me.axeno.noctisui.client.NoctisUIClient;
import me.axeno.noctisui.client.render.Render2DEngine;
import me.axeno.noctisui.client.render.font.FontAtlas;
import me.axeno.noctisui.client.render.font.Fonts;
import me.axeno.noctisui.client.QuickImports;
import me.axeno.noctisui.client.utils.Color;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager implements QuickImports
{
    private static final int CARD_WIDTH = 180;
    private static final int CARD_MIN_HEIGHT = 38;
    private static final int CARD_MAX_HEIGHT = 60;
    private static final int LINE_HEIGHT = 9;
    private static final int CARD_SPACING = 5;
    private static final int MARGIN_X = 10;
    private static final int MARGIN_Y = 10;
    private static final int PADDING = 8;
    private static final float ACCENT_WIDTH = 2.5f;
    private static final float ICON_BADGE_SIZE = 20f;
    private static final float ICON_BADGE_RADIUS = 6f;
    private static final float ICON_SIZE = 10f;
    private static final float TITLE_SIZE = 7.9f;
    private static final float BODY_SIZE = 6.9f;
    private static final float STACK_SIZE = 6.1f;
    private static final float CORNER_RADIUS = 6f;

    @Getter
    private static NotificationManager instance;
    @Setter
    @Getter
    private static FontAtlas font;
    @Setter
    @Getter
    private static FontAtlas fontBold;

    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public NotificationManager()
    {
        instance = this;
    }

    public static void initFont(Fonts fonts)
    {
        font = fonts.getInterMedium();
        fontBold = fonts.getInterBold();
    }

    public static void init()
    {
        // HUD rendering is handled by ClientEvents via RenderGuiEvent.Post
    }

    public static void renderNotifications(GuiGraphics ctx, float tickDelta)
    {
        NotificationManager manager = getInstance();
        if (manager == null) return;
        manager.update();
        manager.render(ctx.pose());
    }

    public void addNotification(String id, String title, String message, NotificationType type)
    {
        addNotification(id, title, message, type, 4000);
    }

    public void addNotification(String id, String title, String message, NotificationType type, long duration)
    {
        Notification incoming = new Notification(id, title, message, type, duration);
        for (Notification existing : notifications) {
            if (existing.isSimilarTo(incoming)) {
                existing.incrementStack();
                return;
            }
        }
        notifications.add(incoming);
    }

    public void success(String id, String title, String message)
    {
        addNotification(id, title, message, NotificationType.SUCCESS);
    }

    public void error(String id, String title, String message)
    {
        addNotification(id, title, message, NotificationType.ERROR);
    }

    public void warning(String id, String title, String message)
    {
        addNotification(id, title, message, NotificationType.WARNING);
    }

    public void info(String id, String title, String message)
    {
        addNotification(id, title, message, NotificationType.INFO);
    }

    private void update()
    {
        List<Notification> expired = new ArrayList<>();
        List<Notification> visible = new ArrayList<>();

        for (Notification notification : notifications) {
            notification.update();
            if (notification.shouldRemove()) {
                expired.add(notification);
            }
            else {
                visible.add(notification);
            }
        }
        notifications.removeAll(expired);

        int y = 0;
        for (Notification notification : visible) {
            notification.setTargetY(y);
            y += cardHeight(notification) + CARD_SPACING;
        }
    }

    private void render(PoseStack matrices)
    {
        if (notifications.isEmpty()) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        for (Notification notification : new ArrayList<>(notifications)) {
            float alpha = notification.getAlpha();
            if (alpha <= 0.01f) continue;

            float slide = notification.getSlideOffset();
            float scale = notification.getScale();
            int height = cardHeight(notification);
            int x = (int) (screenWidth - CARD_WIDTH - MARGIN_X + slide);
            int y = (int) (MARGIN_Y + notification.getCurrentY());

            matrices.pushPose();
            float pivotX = x + CARD_WIDTH;
            float pivotY = y;
            matrices.translate(pivotX, pivotY, 0);
            matrices.scale(scale, scale, 1f);
            matrices.translate(-pivotX, -pivotY, 0);

            renderCard(matrices, notification, x, y, height, alpha);
            matrices.popPose();
        }
    }

    private void renderCard(PoseStack matrices, Notification notification, int x, int y, int height, float alpha)
    {
        NotificationType type = notification.getType();
        Color accent = withAlpha(type.getAccent(), alpha);
        Color accentBright = withAlpha(type.getAccentBright(), alpha);

        Color background = NotificationTheme.withAlpha(NotificationTheme.CARD_BACKGROUND, alpha);
        Render2DEngine.drawRoundedRect(matrices, x, y, CARD_WIDTH, height, CORNER_RADIUS, background);

        // Left accent rail
        float railX = x + PADDING * 0.55f;
        float railY = y + 9;
        float railH = height - 18;
        Render2DEngine.drawRoundedRect(matrices, railX, railY, ACCENT_WIDTH, railH, 1.5f, accentBright);

        // Border: single soft dark outline (remove bright white edge)
        Color border = NotificationTheme.withAlpha(NotificationTheme.CARD_BORDER_DARK, alpha * 0.9f);
        Render2DEngine.drawRoundedOutline(matrices, x, y, CARD_WIDTH, height, CORNER_RADIUS, 0.9f, border);

        float contentTop = y + PADDING;
        float badgeX = x + PADDING + ACCENT_WIDTH + 2;
        // center icon badge vertically inside the card
        float badgeY = y + (height - ICON_BADGE_SIZE) / 2f;

        renderIconBadge(matrices, type, badgeX, badgeY, alpha);

        int textX = (int) (badgeX + ICON_BADGE_SIZE + 6);
        int maxTextWidth = CARD_WIDTH - (textX - x) - PADDING - (notification.hasStack() ? 30 : 0);
        int textY = (int) contentTop + 1;

        if (hasText(notification.getTitle())) {
            Color titleColor = NotificationTheme.withAlpha(NotificationTheme.TEXT_PRIMARY, alpha);
            textY = renderWrappedText(matrices, notification.getTitle(), textX, textY, maxTextWidth, titleColor, true,
                    TITLE_SIZE);
        }

        if (hasText(notification.getMessage())) {
            Color bodyColor = NotificationTheme.withAlpha(NotificationTheme.TEXT_SECONDARY, alpha * 0.92f);
            renderWrappedText(matrices, notification.getMessage(), textX, textY, maxTextWidth, bodyColor, false,
                    BODY_SIZE);
        }

        if (notification.hasStack()) {
            renderStackBadge(matrices, notification, x, y, alpha, accent);
        }
    }

    private void renderIconBadge(PoseStack matrices, NotificationType type, float x, float y, float alpha)
    {
        Color badgeBg = NotificationTheme.tint(NotificationTheme.CARD_BACKGROUND, type.getBadgeBackground(), 0.22f);
        badgeBg = NotificationTheme.withAlpha(badgeBg, alpha);
        Color badgeBorder = NotificationTheme.withAlpha(type.getAccent(), alpha * 0.22f);

        Render2DEngine.drawRoundedRect(matrices, x, y, ICON_BADGE_SIZE, ICON_BADGE_SIZE, ICON_BADGE_RADIUS, badgeBg);
        Render2DEngine.drawRoundedOutline(matrices, x, y, ICON_BADGE_SIZE, ICON_BADGE_SIZE, ICON_BADGE_RADIUS, 0.7f,
                badgeBorder);

        FontAtlas lucide = NoctisUIClient.getInstance().getFonts().getLucide();
        Color iconColor = NotificationTheme.withAlpha(type.getAccentBright(), alpha);
        float iconW = lucide.getWidth(type.getIcon(), ICON_SIZE);
        // render icon centered inside the badge (no extra offset)
        lucide.render(matrices, type.getIcon(), x + (ICON_BADGE_SIZE - iconW) / 2f,
                y + (ICON_BADGE_SIZE - ICON_SIZE) / 2f, ICON_SIZE, iconColor.getValue());
    }

    private void renderStackBadge(PoseStack matrices, Notification notification, int x, int y, float alpha, Color accent)
    {
        String stackText = "×" + notification.getStackCount();
        float textW = fontBold.getWidth(stackText, STACK_SIZE);
        float pillW = textW + 10;
        float pillH = 14;
        float pillX = x + CARD_WIDTH - PADDING - pillW;
        float pillY = y + PADDING;

        // Render stack count as outlined pill without background (transparent)
        Color pillBorder = NotificationTheme.withAlpha(accent, alpha * 0.6f);
        Render2DEngine.drawRoundedOutline(matrices, pillX, pillY, pillW, pillH, 6f, 0.9f, pillBorder);
        // draw text centered vertically inside the pill
        int textDrawY = (int) (pillY + (pillH - STACK_SIZE) / 2f + 1f);
        drawText(matrices, stackText, (int) (pillX + 5), textDrawY, STACK_SIZE,
                NotificationTheme.withAlpha(NotificationTheme.TEXT_PRIMARY, alpha), true);
    }

    private int cardHeight(Notification notification)
    {
        int height = CARD_MIN_HEIGHT;
        int textX = (int) (PADDING + ACCENT_WIDTH + 4 + ICON_BADGE_SIZE + 8);
        int maxTextWidth = CARD_WIDTH - textX - PADDING;

        if (hasText(notification.getTitle())) {
            height += (wrapText(notification.getTitle(), maxTextWidth, true, TITLE_SIZE).size() - 1) * LINE_HEIGHT;
        }

        if (hasText(notification.getMessage())) {
            height += (wrapText(notification.getMessage(), maxTextWidth, false, BODY_SIZE).size() - 1) * LINE_HEIGHT;
        }

        return Math.min(Math.max(height, CARD_MIN_HEIGHT), CARD_MAX_HEIGHT);
    }

    private List<String> wrapText(String text, int maxWidth, boolean bold, float size)
    {
        FontAtlas typeface = bold ? fontBold : font;
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (typeface.getWidth(candidate, size) <= maxWidth) {
                current = new StringBuilder(candidate);
            }
            else {
                if (!current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                }
                else {
                    lines.add(truncateText(word, maxWidth, bold, size));
                }
            }
        }

        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines.isEmpty() ? List.of("") : lines;
    }

    private int renderWrappedText(PoseStack matrices, String text, int x, int y, int maxWidth, Color color,
                                    boolean bold, float size)
    {
        int currentY = y;
        for (String line : wrapText(text, maxWidth, bold, size)) {
            drawText(matrices, line, x, currentY, size, color, bold);
            currentY += LINE_HEIGHT;
        }
        return currentY;
    }

    private int drawText(PoseStack matrices, String text, int x, int y, float size, Color color, boolean bold)
    {
        FontAtlas typeface = bold ? fontBold : font;
        typeface.render(matrices, text, x, y, size, color.getValue());
        return y + LINE_HEIGHT;
    }

    private void drawText(PoseStack matrices, String text, int x, int y, Color color, boolean bold)
    {
        FontAtlas typeface = bold ? fontBold : font;
        typeface.render(matrices, text, x, y, color.getValue());
    }

    private String truncateText(String text, int maxWidth, boolean bold, float size)
    {
        FontAtlas typeface = bold ? fontBold : font;
        if (typeface.getWidth(text, size) <= maxWidth) return text;

        String ellipsis = "…";
        float ellipsisWidth = typeface.getWidth(ellipsis, size);
        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String candidate = truncated.toString() + text.charAt(i);
            if (typeface.getWidth(candidate, size) + ellipsisWidth > maxWidth) break;
            truncated.append(text.charAt(i));
        }
        return truncated + ellipsis;
    }

    private static boolean hasText(String value)
    {
        return value != null && !value.isEmpty();
    }

    private static Color withAlpha(Color color, float alpha)
    {
        return NotificationTheme.withAlpha(color, alpha);
    }
}
