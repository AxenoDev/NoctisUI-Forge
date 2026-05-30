package me.axeno.noctisui.client.component.system;

import me.axeno.noctisui.client.NoctisUIClient;
import me.axeno.noctisui.client.api.system.Render2DEngine;
import me.axeno.noctisui.client.api.system.render.font.FontAtlas;
import me.axeno.noctisui.client.api.system.render.font.Fonts;
import me.axeno.noctisui.client.common.QuickImports;
import me.axeno.noctisui.client.utils.Color;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A manager for displaying and handling notifications in the UI.
 *
 * <p>This class allows you to create, display, and manage notifications with different types, durations, and styles.</p>
 *
 * <pre>
 *     {@code
 *     NotificationManager notificationManager = new NotificationManager();
 *     notificationManager.success("save_success", "Success", "Your changes have been saved.");
 *     notificationManager.error("save_error", "Error", "Failed to save changes.");
 *     }
 * </pre>
 *
 * @author axeno
 *
 */
public class NotificationManager implements QuickImports
{

    private static final int NOTIFICATION_WIDTH = 188;
    private static final int NOTIFICATION_MIN_HEIGHT = 34;
    private static final int NOTIFICATION_MAX_HEIGHT = 68;
    private static final int LINE_HEIGHT = 10;
    private static final int NOTIFICATION_SPACING = 5;
    private static final int MARGIN_X = 10;
    private static final int MARGIN_Y = 10;
    private static final int PADDING = 8;
    private static final float TYPE_STRIP_WIDTH = 2f;
    private static final float ICON_SIZE = 9f;
    private static final float TEXT_TITLE_SIZE = 8.5f;
    private static final float TEXT_BODY_SIZE = 8f;
    private static final float CORNER_RADIUS = 6f;
    private static final float BLUR_QUALITY = 18f;
    private static final float BLUR_BRIGHTNESS = 0.97f;
    @Getter
    private static NotificationManager instance;
    @Setter
    @Getter
    private static FontAtlas font;
    @Setter
    @Getter
    private static FontAtlas fontBold;
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    /**
     * Creates a new NotificationManager instance and sets it as the singleton instance.
     */
    public NotificationManager()
    {
        instance = this;
    }

    /**
     * Initializes the fonts used for rendering notifications.
     *
     * @param fonts The Fonts instance containing the desired fonts.
     */
    public static void initFont(Fonts fonts)
    {
        font = fonts.getInterMedium();
        fontBold = fonts.getInterBold();
    }

    /**
     * Registers the notification rendering callback to the HUD render event.
     * This method should be called once during the client initialization.
     */
    public static void init()
    {
        // HUD rendering is handled by ClientEvents via RenderGuiEvent.Post
    }

    /**
     * Renders the notifications on the HUD.
     *
     * @param ctx       The GuiGraphics for rendering.
     * @param tickDelta The tick delta for smooth animations.
     */
    public static void renderNotifications(GuiGraphics ctx, float tickDelta)
    {
        NotificationManager manager = getInstance();
        manager.update();
        manager.render(ctx.pose());
    }

    /**
     * Adds a new notification to be displayed.
     *
     * @param id      A unique ResourceLocation for the notification.
     * @param title   The title of the notification.
     * @param message The message content of the notification.
     * @param type    The type of the notification (e.g., INFO, WARNING, ERROR).
     */
    public void addNotification(String id, String title, String message, NotificationType type)
    {
        addNotification(id, title, message, type, 3000);
    }

    /**
     * Adds a new notification to be displayed with a custom duration.
     *
     * @param id       A unique ResourceLocation for the notification.
     * @param title    The title of the notification.
     * @param message  The message content of the notification.
     * @param type     The type of the notification (e.g., INFO, WARNING, ERROR).
     * @param duration The duration (in milliseconds) for which the notification should be displayed.
     */
    public void addNotification(String id, String title, String message, NotificationType type, long duration)
    {
        Notification newNotification = new Notification(id, title, message, type, duration);

        for (Notification existing : notifications) {
            if (existing.isSimilarTo(newNotification)) {
                existing.incrementStack();
                return;
            }
        }

        notifications.add(newNotification);
    }

    /**
     * Adds a success notification.
     *
     * @param id      A unique ResourceLocation for the notification.
     * @param title   The title of the notification.
     * @param message The message content of the notification.
     */
    public void success(String id, String title, String message)
    {
        addNotification(id, title, message, NotificationType.SUCCESS);
    }

    /**
     * Adds an error notification.
     *
     * @param id      A unique ResourceLocation for the notification.
     * @param title   The title of the notification.
     * @param message The message content of the notification.
     */
    public void error(String id, String title, String message)
    {
        addNotification(id, title, message, NotificationType.ERROR);
    }

    /**
     * Adds a warning notification.
     *
     * @param id      A unique ResourceLocation for the notification.
     * @param title   The title of the notification.
     * @param message The message content of the notification.
     */
    public void warning(String id, String title, String message)
    {
        addNotification(id, title, message, NotificationType.WARNING);
    }

    /**
     * Adds an info notification.
     *
     * @param id      A unique ResourceLocation for the notification.
     * @param title   The title of the notification.
     * @param message The message content of the notification.
     */
    public void info(String id, String title, String message)
    {
        addNotification(id, title, message, NotificationType.INFO);
    }

    /**
     * Updates the state of all notifications, including their positions and removal status.
     */
    private void update()
    {
        List<Notification> toRemove = new ArrayList<>();
        List<Notification> visibleNotif = new ArrayList<>();

        for (Notification notification : notifications) {
            notification.update();
            if (notification.shouldRemove()) toRemove.add(notification);
            else visibleNotif.add(notification);
        }

        notifications.removeAll(toRemove);

        int currentY = 0;
        for (Notification notification : visibleNotif) {
            notification.setTargetY(currentY);
            currentY += calculateNotificationHeight(notification) + NOTIFICATION_SPACING;
        }
    }

    /**
     * Renders all active notifications on the screen.
     *
     * @param matrices The PoseStack used for rendering transformations.
     */
    private void render(PoseStack matrices)
    {
        if (notifications.isEmpty()) return;
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        List<Notification> notificationsCopy = new ArrayList<>(notifications);

        for (Notification notification : notificationsCopy) {
            float offsetX = notification.getSlideOffset();
            float alpha = notification.getAlpha();
            float animatedY = notification.getCurrentY();
            int x = (int) (screenWidth - NOTIFICATION_WIDTH - MARGIN_X + offsetX);
            int y = (int) (MARGIN_Y + animatedY);
            renderNotification(matrices, notification, x, y, alpha);
        }
    }

    /**
     * Renders a single notification at the specified position with the given alpha transparency.
     *
     * @param matrices     The PoseStack used for rendering transformations.
     * @param notification The Notification to be rendered.
     * @param x            The X-coordinate for rendering the notification.
     * @param y            The Y-coordinate for rendering the notification.
     * @param alpha        The alpha transparency value (0.0 to 1.0) for the notification.
     */
    private void renderNotification(PoseStack matrices, Notification notification, int x, int y, float alpha)
    {
        alpha = Math.max(0f, Math.min(1f, alpha));

        int notificationHeight = calculateNotificationHeight(notification);
        Color typeColor = typeColor(notification, alpha);

        Render2DEngine.drawBlur(matrices, x, y, NOTIFICATION_WIDTH, notificationHeight, CORNER_RADIUS, BLUR_QUALITY,
                BLUR_BRIGHTNESS);

        Color depth = NotificationTheme.withAlpha(NotificationTheme.BACKGROUND, alpha * 0.72f);
        Render2DEngine.drawRoundedRect(matrices, x, y, NOTIFICATION_WIDTH, notificationHeight, CORNER_RADIUS, depth);

        Color surface = NotificationTheme.withAlpha(NotificationTheme.SURFACE, alpha * 0.84f);
        Render2DEngine.drawRoundedRect(matrices, x, y, NOTIFICATION_WIDTH, notificationHeight, CORNER_RADIUS, surface);

        Color border = NotificationTheme.withAlpha(NotificationTheme.SURFACE_LIGHT, alpha * 0.1f);
        Render2DEngine.drawRoundedOutline(matrices, x, y, NOTIFICATION_WIDTH, notificationHeight, CORNER_RADIUS, 0.8f,
                border);

        float stripX = x + PADDING;
        float stripY = y + 6;
        float stripH = notificationHeight - 12;
        Render2DEngine.drawRoundedRect(matrices, stripX, stripY, TYPE_STRIP_WIDTH, stripH, 1f, typeColor);

        float iconCenterX = stripX + TYPE_STRIP_WIDTH + 10;
        float iconCenterY = y + notificationHeight / 2f;
        renderIcon(matrices, notification.getType(), (int) iconCenterX, (int) iconCenterY, typeColor);

        int textStartX = (int) (iconCenterX + 10);
        int maxTextWidth = NOTIFICATION_WIDTH - (textStartX - x) - PADDING;
        int currentTextY = y + 7;

        if (notification.getTitle() != null && !notification.getTitle().isEmpty()) {
            Color titleColor = NotificationTheme.withAlpha(NotificationTheme.SURFACE_LIGHT, alpha);
            currentTextY = renderWrappedText(matrices, notification.getTitle(), textStartX, currentTextY, maxTextWidth,
                    titleColor, true, TEXT_TITLE_SIZE);
            currentTextY += 1;
        }

        if (notification.getMessage() != null && !notification.getMessage().isEmpty()) {
            Color messageColor = NotificationTheme.withAlpha(NotificationTheme.SURFACE_LIGHT, alpha * 0.55f);
            renderWrappedText(matrices, notification.getMessage(), textStartX, currentTextY, maxTextWidth, messageColor,
                    false, TEXT_BODY_SIZE);
        }

        if (notification.hasStack()) {
            renderStackCounter(matrices, notification, x, y, alpha);
        }

        renderProgressBar(matrices, notification, x, y + notificationHeight - 3, alpha);
    }

    private static Color typeColor(Notification notification, float alpha)
    {
        Color c = notification.getColor();
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255 * alpha));
    }

    /**
     * Calculates the height of a notification based on its content.
     *
     * @param notification The Notification for which to calculate the height.
     *
     * @return The calculated height of the notification.
     */
    private int calculateNotificationHeight(Notification notification)
    {
        int height = NOTIFICATION_MIN_HEIGHT;
        int maxTextWidth = NOTIFICATION_WIDTH - PADDING - (int) TYPE_STRIP_WIDTH - 20 - PADDING;

        if (notification.getTitle() != null && !notification.getTitle().isEmpty()) {
            List<String> titleLines = wrapText(notification.getTitle(), maxTextWidth, true, TEXT_TITLE_SIZE);
            height += (titleLines.size() - 1) * LINE_HEIGHT;
        }

        if (notification.getMessage() != null && !notification.getMessage().isEmpty()) {
            List<String> messageLines = wrapText(notification.getMessage(), maxTextWidth, false, TEXT_BODY_SIZE);
            height += (messageLines.size() - 1) * LINE_HEIGHT;
        }

        return Math.min(height, NOTIFICATION_MAX_HEIGHT);
    }

    /**
     * Wraps the given text into multiple lines based on the specified maximum width.
     *
     * @param text     The text to be wrapped.
     * @param maxWidth The maximum width (in pixels) for each line.
     * @param bold     Whether to use the bold font for width calculations.
     *
     * @return A list of strings, each representing a line of wrapped text.
     */
    private List<String> wrapText(String text, int maxWidth, boolean bold, float size)
    {
        FontAtlas police = bold ? fontBold : font;
        List<String> lines = new ArrayList<>();

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;

            if (police.getWidth(testLine, size) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            }
            else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
                else {
                    lines.add(truncateText(word, maxWidth, bold, size));
                }
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Renders wrapped text at the specified position, handling line breaks as needed.
     *
     * @param matrices The PoseStack used for rendering transformations.
     * @param text     The text to be rendered.
     * @param x        The X-coordinate for rendering the text.
     * @param y        The starting Y-coordinate for rendering the text.
     * @param maxWidth The maximum width (in pixels) for each line of text.
     * @param color    The color of the text.
     * @param bold     Whether to use the bold font for rendering.
     *
     * @return The Y-coordinate after rendering the text (useful for further rendering).
     */
    private int renderWrappedText(PoseStack matrices, String text, int x, int y, int maxWidth, Color color,
                                  boolean bold, float size)
    {
        List<String> lines = wrapText(text, maxWidth, bold, size);
        int currentY = y;

        for (String line : lines) {
            drawText(matrices, line, x, currentY, size, color, bold);
            currentY += LINE_HEIGHT;
        }

        return currentY;
    }

    /**
     * Renders the stack counter for a notification if it has multiple stacked instances.
     *
     * @param matrices     The PoseStack used for rendering transformations.
     * @param notification The Notification for which to render the stack counter.
     * @param x            The X-coordinate for rendering the stack counter.
     * @param y            The Y-coordinate for rendering the stack counter.
     * @param alpha        The alpha transparency value (0.0 to 1.0) for the stack counter.
     */
    private void renderStackCounter(PoseStack matrices, Notification notification, int x, int y, float alpha)
    {
        String stackText = "×" + notification.getStackCount();
        float textSize = 7.5f;
        Color stackTextColor = NotificationTheme.withAlpha(NotificationTheme.SURFACE_LIGHT, alpha * 0.45f);
        float textWidth = fontBold.getWidth(stackText, textSize);
        drawText(matrices, stackText, (int) (x + NOTIFICATION_WIDTH - textWidth - PADDING), y + 6, textSize,
                stackTextColor, true);
    }

    /**
     * Renders the icon for a notification type at the specified position.
     *
     * @param matrices The PoseStack used for rendering transformations.
     * @param type     The NotificationType whose icon is to be rendered.
     * @param x        The X-coordinate for rendering the icon.
     * @param y        The Y-coordinate for rendering the icon.
     * @param color    The color to apply to the icon.
     */
    private void renderIcon(PoseStack matrices, NotificationType type, int centerX, int centerY, Color color)
    {
        FontAtlas lucide = NoctisUIClient.getInstance().getFonts().getLucide();
        float w = lucide.getWidth(type.getIcon(), ICON_SIZE);
        lucide.render(matrices, type.getIcon(), centerX - w / 2f, centerY - ICON_SIZE / 2f - 0.5f, ICON_SIZE,
                color.getValue());
    }

    /**
     * Renders the progress bar for a notification at the specified position.
     *
     * @param matrices     The PoseStack used for rendering transformations.
     * @param notification The Notification for which to render the progress bar.
     * @param x            The X-coordinate for rendering the progress bar.
     * @param y            The Y-coordinate for rendering the progress bar.
     * @param alpha        The alpha transparency value (0.0 to 1.0) for the progress bar.
     */
    private void renderProgressBar(PoseStack matrices, Notification notification, int x, int y, float alpha)
    {
        long elapsed = System.currentTimeMillis() - notification.getLastStackTime();
        float progress = Math.min(elapsed / (float) notification.getDuration(), 1f);

        float barInset = 5f;
        float barWidth = NOTIFICATION_WIDTH - barInset * 2;
        float barHeight = 1.5f;

        Color trackColor = NotificationTheme.withAlpha(NotificationTheme.SURFACE_LIGHT, alpha * 0.1f);
        Render2DEngine.drawRoundedRect(matrices, x + barInset, y, barWidth, barHeight, 0.5f, trackColor);

        if (progress < 1f) {
            float fillWidth = barWidth * (1f - progress);
            Color progressColor = NotificationTheme.withAlpha(NotificationTheme.ACCENT, alpha);
            Render2DEngine.drawRoundedRect(matrices, x + barInset, y, fillWidth, barHeight, 0.5f, progressColor);
        }
    }

    /**
     * Draws text at the specified position with the given color and font style.
     *
     * @param matrices The PoseStack used for rendering transformations.
     * @param text     The text to be drawn.
     * @param x        The X-coordinate for rendering the text.
     * @param y        The Y-coordinate for rendering the text.
     * @param color    The color of the text.
     * @param bold     Whether to use the bold font for rendering.
     */
    private void drawText(PoseStack matrices, String text, int x, int y, Color color, boolean bold)
    {
        FontAtlas police = bold ? fontBold : font;

        police.render(matrices, text, x, y, color.getValue());
    }

    /**
     * Draws text at the specified position with the given size, color, and font style.
     *
     * @param matrices The PoseStack used for rendering transformations.
     * @param text     The text to be drawn.
     * @param x        The X-coordinate for rendering the text.
     * @param y        The Y-coordinate for rendering the text.
     * @param size     The size of the font.
     * @param color    The color of the text.
     * @param bold     Whether to use the bold font for rendering.
     */
    private void drawText(PoseStack matrices, String text, int x, int y, float size, Color color, boolean bold)
    {
        FontAtlas police = bold ? fontBold : font;

        police.render(matrices, text, x, y, size, color.getValue());
    }

    /**
     * Truncates the given text to fit within the specified maximum width, adding an ellipsis if necessary.
     *
     * @param text     The text to be truncated.
     * @param maxWidth The maximum width (in pixels) for the text.
     * @param bold     Whether to use the bold font for width calculations.
     *
     * @return The truncated text with an ellipsis if it exceeds the maximum width.
     */
    private String truncateText(String text, int maxWidth, boolean bold, float size)
    {
        FontAtlas police = bold ? fontBold : font;

        if (police.getWidth(text, size) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        float ellipsisWidth = police.getWidth(ellipsis, size);

        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String candidate = truncated.toString() + text.charAt(i);
            if (police.getWidth(candidate, size) + ellipsisWidth > maxWidth) {
                break;
            }
            truncated.append(text.charAt(i));
        }

        return truncated + ellipsis;
    }
}
