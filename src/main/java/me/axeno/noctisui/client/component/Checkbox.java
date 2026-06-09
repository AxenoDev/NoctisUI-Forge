package me.axeno.noctisui.client.component;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import me.axeno.noctisui.client.api.system.Render2DEngine;
import me.axeno.noctisui.client.api.system.render.font.FontAtlas;
import me.axeno.noctisui.client.common.QuickImports;
import me.axeno.noctisui.client.utils.Color;
import me.axeno.noctisui.client.utils.TextPosition;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

/**
 *
 */
public class Checkbox extends UIBaseComponent implements QuickImports
{

    private static final String LUCIDE_CHECK = "\uE954";

    @Setter
    public int fontSize = 9;

    @Getter
    @Setter
    private boolean checked;

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
    private Color backgroundColor;

    @Setter
    private Color checkColor;

    private Color outlineColor = null;
    private float outlineWidth = 0f;

    @Setter
    private int radius = 3;

    @Setter
    private float checkIconScale = 0.65f;

    private boolean hasHover = false;
    private long hoverAnimationDuration;
    private Color hoverBackgroundColor;
    private Color hoverCheckColor;

    private long hoverStartTime = -1;
    private boolean isHovered = false;

    private Consumer<Checkbox> onToggleAction;

    public Checkbox(float x, float y, float width, float height, Color backgroundColor, Color checkColor)
    {
        super(x, y, width, height);
        this.backgroundColor = backgroundColor;
        this.checkColor = checkColor;
    }

    public Checkbox(float x, float y, float width, float height, String label, Color backgroundColor, Color checkColor)
    {
        super(x, y, width, height);
        this.label = label;
        this.backgroundColor = backgroundColor;
        this.checkColor = checkColor;
    }

    public Checkbox(float x, float y, float width, float height, String label, TextPosition textPosition, Color backgroundColor, Color checkColor)
    {
        super(x, y, width, height);
        this.label = label;
        this.textPosition = textPosition;
        this.backgroundColor = backgroundColor;
        this.checkColor = checkColor;
    }

    public Checkbox(float x, float y, float width, float height,
                    String label, Color backgroundColor, Color checkColor, Color labelColor)
    {
        super(x, y, width, height);
        this.label = label;
        this.backgroundColor = backgroundColor;
        this.checkColor = checkColor;
        this.labelColor = labelColor;
    }

    public Checkbox(float x, float y, float width, float height,
                    String label, TextPosition textPosition,
                    Color backgroundColor, Color checkColor, Color labelColor)
    {
        super(x, y, width, height);
        this.label = label;
        this.textPosition = textPosition;
        this.backgroundColor = backgroundColor;
        this.checkColor = checkColor;
        this.labelColor = labelColor;
    }

    public Checkbox(float x, float y, float width, float height,
                    String label, float textOffsetX, float textOffsetY,
                    Color backgroundColor, Color checkColor, Color labelColor)
    {
        super(x, y, width, height);
        this.label = label;
        this.textPosition = TextPosition.CUSTOM;
        this.textOffsetX = textOffsetX;
        this.textOffsetY = textOffsetY;
        this.backgroundColor = backgroundColor;
        this.checkColor = checkColor;
        this.labelColor = labelColor;
    }

    public void setOutline(Color outlineColor, float outlineWidth)
    {
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
    }

    public void hover(long animationDuration, Color hoverBackgroundColor, Color hoverCheckColor)
    {
        this.hasHover = true;
        this.hoverAnimationDuration = animationDuration;
        this.hoverBackgroundColor = hoverBackgroundColor;
        this.hoverCheckColor = hoverCheckColor;
    }

    public void setOnToggle(Consumer<Checkbox> action)
    {
        this.onToggleAction = action;
    }

    private float[] resolveLabelPosition()
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
            default -> new float[]{x + width + textSpacing, y + (height - textHeight) / 2f}; // RIGHT
        };
    }

    private boolean isClickable(double mouseX, double mouseY)
    {
        // Box hit-test
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
            return true;

        float[] labelPos = resolveLabelPosition();
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

        Color originalBackgroundColor = backgroundColor;

        Color currentBackgroundColor = backgroundColor;
        Color currentCheckColor = checkColor;

        boolean isMouseOver = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

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
                    currentBackgroundColor = Color.interpolateColor(backgroundColor, hoverBackgroundColor, progress);
                    currentCheckColor = Color.interpolateColor(checkColor, hoverCheckColor, progress);
                } else
                {
                    currentBackgroundColor = Color.interpolateColor(hoverBackgroundColor, backgroundColor, progress);
                    currentCheckColor = Color.interpolateColor(hoverCheckColor, checkColor, progress);
                    if (progress == 1f) hoverStartTime = -1;
                }
            }
        }

        Render2DEngine.drawRoundedRect(matrices, x, y, width, height, radius, currentBackgroundColor);

        if (outlineWidth > 0 && outlineColor != null)
        {
            Render2DEngine.drawRoundedOutline(matrices, x, y, width, height, radius, outlineWidth, outlineColor);
        }

        if (checked)
        {
            FontAtlas lucide = fonts.getLucide();

            if (lucide != null)
            {
                Render2DEngine.drawRoundedRect(matrices, x, y, width, height, radius, currentCheckColor);

                float iconSize = height * checkIconScale;
                float iconWidth = lucide.getWidth(LUCIDE_CHECK, iconSize);
                float iconHeight = lucide.getLineHeight(iconSize);

                float iconX = x + (width - iconWidth) / 2f;
                float iconY = y + (height - iconHeight) / 2f;

                lucide.render(matrices, LUCIDE_CHECK, iconX, iconY, iconSize, originalBackgroundColor.getValue());
            }
        }

        float[] labelPos = resolveLabelPosition();
        if (labelPos != null)
        {
            if (!shadow) font.render(matrices, label, labelPos[0], labelPos[1], fontSize, labelColor.getValue());
            else font.renderWithShadow(matrices, label, labelPos[0], labelPos[1], fontSize, labelColor.getValue());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (isClickable(mouseX, mouseY))
        {
            checked = !checked;
            if (onToggleAction != null)
            {
                onToggleAction.accept(this);
            }
            return true;
        }
        return false;
    }

}
