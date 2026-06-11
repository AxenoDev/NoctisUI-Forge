package me.axeno.noctisui.client.component;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import me.axeno.noctisui.client.render.Render2DEngine;
import me.axeno.noctisui.client.render.font.FontAtlas;
import me.axeno.noctisui.client.QuickImports;
import me.axeno.noctisui.client.utils.Color;
import me.axeno.noctisui.client.utils.MathUtils;
import me.axeno.noctisui.client.utils.TextPosition;
import net.minecraft.client.gui.GuiGraphics;

public class Switch extends ClickableComponent<Switch> implements QuickImports
{

    @Setter
    public int fontSize = 9;
    @Getter
    @Setter
    private boolean enabled;
    @Getter
    @Setter
    private String label;
    @Getter
    @Setter
    private Color labelColor;
    @Setter
    private FontAtlas font = fonts.getInterMedium();
    @Setter
    private boolean shadow = false;

    @Setter
    private float textSpacing = 5f;

    @Setter
    private TextPosition textPosition = TextPosition.RIGHT;

    @Setter
    private float textOffsetX = 0f;

    @Setter
    private float textOffsetY = 0f;

    @Setter
    private Color trackOffColor;

    @Setter
    private Color trackOnColor;

    @Setter
    private int trackRadius = -1;

    @Setter
    private Color thumbColor;

    @Setter
    private float thumbPadding = 2f;

    private Color outlineColor = null;
    private float outlineWidth = 0f;

    private boolean hasHover = false;
    private long hoverAnimationDuration;
    private Color hoverTrackOffColor;
    private Color hoverTrackOnColor;
    private Color hoverThumbColor;

    private long hoverStartTime = -1;
    private boolean isHovered = false;

    @Setter
    private long toggleAnimationDuration = 150L;

    private long toggleStartTime = -1;

    private float thumbProgress = 0f;

    public Switch(float x, float y, float width, float height, Color trackOffColor, Color trackOnColor, Color thumbColor)
    {
        super(x, y, width, height);
        this.trackOffColor = trackOffColor;
        this.trackOnColor = trackOnColor;
        this.thumbColor = thumbColor;
    }

    public Switch(float x, float y, float width, float height, String label, Color trackOffColor, Color trackOnColor, Color thumbColor)
    {
        super(x, y, width, height);
        this.label = label;
        this.trackOffColor = trackOffColor;
        this.trackOnColor = trackOnColor;
        this.thumbColor = thumbColor;
    }

    public Switch(float x, float y, float width, float height, String label, TextPosition textPosition, Color trackOffColor, Color trackOnColor, Color thumbColor)
    {
        super(x, y, width, height);
        this.label = label;
        this.textPosition = textPosition;
        this.trackOffColor = trackOffColor;
        this.trackOnColor = trackOnColor;
        this.thumbColor = thumbColor;
    }

    public Switch(float x, float y, float width, float height, String label, Color trackOffColor, Color trackOnColor, Color thumbColor, Color labelColor)
    {
        super(x, y, width, height);
        this.label = label;
        this.trackOffColor = trackOffColor;
        this.trackOnColor = trackOnColor;
        this.thumbColor = thumbColor;
        this.labelColor = labelColor;
    }

    public Switch(float x, float y, float width, float height, String label, float textSpacing, Color trackOffColor, Color trackOnColor, Color thumbColor, Color labelColor)
    {
        super(x, y, width, height);
        this.label = label;
        this.textSpacing = textSpacing;
        this.trackOffColor = trackOffColor;
        this.trackOnColor = trackOnColor;
        this.thumbColor = thumbColor;
        this.labelColor = labelColor;
    }

    public void setOutline(Color outlineColor, float outlineWidth)
    {
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
    }

    public void hover(long animationDuration, Color hoverTrackOffColor, Color hoverTrackOnColor, Color hoverThumbColor)
    {
        this.hasHover = true;
        this.hoverAnimationDuration = animationDuration;
        this.hoverTrackOffColor = hoverTrackOffColor;
        this.hoverTrackOnColor = hoverTrackOnColor;
        this.hoverThumbColor = hoverThumbColor;
    }

    private int resolvedTrackRadius()
    {
        return trackRadius < 0 ? (int) (height / 2f) : trackRadius;
    }

    private float[] resolvedLabelPosition()
    {
        if (label == null || label.isEmpty() || font == null || labelColor == null)
            return null;

        float textWidth = font.getWidth(label, fontSize);
        float textHeight = font.getLineHeight(fontSize);

        return switch (textPosition)
        {
            case CUSTOM -> new float[]{x + textOffsetX, y + textOffsetY};
            case LEFT -> new float[]{x - textWidth - textSpacing, y + (height - textHeight) / 2f};
            case TOP -> new float[]{x + (width - textWidth) / 2f, y - textHeight - textSpacing};
            case BOTTOM -> new float[]{x + (width - textWidth) / 2f, y + height + textSpacing};
            default -> new float[]{x + width + textSpacing, y + (height - textHeight) / 2f};
        };
    }

    private boolean isClickable(double mouseX, double mouseY)
    {
        // Box hit-test
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
            return true;

        float[] labelPos = resolvedLabelPosition();
        if (labelPos == null) return false;

        float textWidth = font.getWidth(label, fontSize);
        float textHeight = font.getLineHeight(fontSize);

        return mouseX >= labelPos[0] && mouseX <= labelPos[0] + textWidth
                && mouseY >= labelPos[1] && mouseY <= labelPos[1] + textHeight;
    }

    @Override
    public void render(GuiGraphics context, double mouseX, double mouseY, float delta)
    {
        PoseStack matrices = context.pose();

        Color currentTrackOffColor = trackOffColor;
        Color currentTrackOnColor = trackOnColor;
        Color currentThumbColor = thumbColor;

        boolean isMouseOver = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        if (hasHover)
        {
            if (isMouseOver && !isHovered)
            {
                isHovered = true;
                hoverStartTime = System.currentTimeMillis();
            } else if (!isMouseOver && isHovered)
            {
                isHovered = false;
                hoverStartTime = System.currentTimeMillis();
            }

            if (hoverStartTime != -1)
            {
                long elapsed = System.currentTimeMillis() - hoverStartTime;
                float progress = Math.min(1f, (float) elapsed / hoverAnimationDuration);

                if (isHovered)
                {
                    currentTrackOffColor = Color.interpolateColor(trackOffColor, hoverTrackOffColor, progress);
                    currentTrackOnColor = Color.interpolateColor(trackOnColor, hoverTrackOnColor, progress);
                    currentThumbColor = Color.interpolateColor(thumbColor, hoverThumbColor, progress);
                } else
                {
                    currentTrackOffColor = Color.interpolateColor(hoverTrackOffColor, trackOffColor, progress);
                    currentTrackOnColor = Color.interpolateColor(hoverTrackOnColor, trackOnColor, progress);
                    currentThumbColor = Color.interpolateColor(hoverThumbColor, thumbColor, progress);
                    if (progress == 1f) hoverStartTime = -1;
                }
            }
        }

        if (toggleStartTime != -1)
        {
            long elapsed = System.currentTimeMillis() - toggleStartTime;
            float raw = Math.min(1f, (float) elapsed / toggleAnimationDuration);
            float eased = MathUtils.ease(raw);

            thumbProgress = enabled ? eased : (1f - eased);

            if (raw == 1f)
                toggleStartTime = -1;
        } else
        {
            thumbProgress = enabled ? 1f : 0f;
        }

        Color trackColor = Color.interpolateColor(currentTrackOffColor, currentTrackOnColor, thumbProgress);

        int radius = resolvedTrackRadius();

        Render2DEngine.drawRoundedRect(matrices, x, y, width, height, radius, trackColor);

        if (outlineWidth > 0 && outlineColor != null)
            Render2DEngine.drawRoundedOutline(matrices, x, y, width, height, radius, outlineWidth, outlineColor);

        float thumbDiameter = height - thumbPadding * 2f;
        float travel = width - thumbDiameter - thumbPadding * 2f;
        float thumbX = x + thumbPadding + travel * thumbProgress;
        float thumbY = y + thumbPadding;

        Render2DEngine.drawRoundedRect(
                matrices,
                thumbX, thumbY,
                thumbDiameter, thumbDiameter,
                thumbDiameter / 2f,
                currentThumbColor
        );

        float[] labelPos = resolvedLabelPosition();
        if (labelPos != null)
        {
            if (!shadow) font.render(matrices, label, labelPos[0], labelPos[1], fontSize, labelColor.getValue());
            else font.renderWithShadow(matrices, label, labelPos[0], labelPos[1], fontSize, labelColor.getValue());
        }
    }

    @Override
    protected void onClicked()
    {
        this.enabled = !enabled;
        this.toggleStartTime = System.currentTimeMillis();
    }
}
