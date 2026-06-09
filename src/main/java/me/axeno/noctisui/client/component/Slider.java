package me.axeno.noctisui.client.component;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import me.axeno.noctisui.client.api.system.Render2DEngine;
import me.axeno.noctisui.client.api.system.render.font.FontAtlas;
import me.axeno.noctisui.client.common.QuickImports;
import me.axeno.noctisui.client.utils.Color;
import me.axeno.noctisui.client.utils.TextPosition;
import me.axeno.noctisui.utils.MathsUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public class Slider extends UIBaseComponent implements QuickImports
{
    @Getter
    @Setter
    private float min, max;

    @Getter
    @Setter
    private float step = 0f;

    @Getter
    @Setter
    private boolean snapOnRelease = false;

    @Getter
    @Setter
    private int decimalPlaces = 1;

    @Getter
    @Setter
    private float value;

    private float rawProgress;

    @Getter
    @Setter
    private long snapAnimationDuration = 120L;

    private float snapFromProgress = 0f;
    private float snapToProgress = 0f;
    private long snapStartTime = -1;

    @Setter
    private Color trackFillColor;

    @Setter
    private Color trackEmptyColor;

    @Setter
    private float trackHeight = 4f;

    @Setter
    private int trackRadius = -1;

    @Setter
    private Color thumbColor;

    @Getter
    @Setter
    private float thumbSize = -1f;

    @Setter
    private Color thumbDragColor;

    private boolean hasHover = false;
    private long hoverAnimationDuration;
    private Color hoverTrackFillColor;
    private Color hoverTrackEmptyColor;
    private Color hoverThumbColor;

    private long hoverStartTime = -1;
    private boolean isHovered = false;

    @Getter
    @Setter
    private boolean showTicks = false;

    @Setter
    private Color tickColor = new Color(200, 200, 200, 120);
    @Getter
    @Setter
    private float tickWidth = 1f;
    @Getter
    @Setter
    private float tickHeight = 4f;

    @Getter
    @Setter
    private boolean showTooltip = true;

    @Setter
    private Color tooltipBackground = new Color(30, 30, 40, 210);
    @Setter
    private Color tooltipTextColor = new Color(220, 220, 255);
    @Getter
    @Setter
    private int tooltipFontSize = 8;
    @Getter
    @Setter
    private float tooltipPaddingX = 4f;
    @Getter
    @Setter
    private float tooltipPaddingY = 2f;
    @Getter
    @Setter
    private int tooltipRadius = 4;

    @Getter
    @Setter
    private String label;
    @Getter
    @Setter
    private Color labelColor;

    @Setter
    private FontAtlas font = fonts.getInterMedium();
    @Setter
    private int fontSize = 9;
    @Setter
    private boolean shadow = false;

    @Setter
    private float textSpacing = 5f;

    @Setter
    private TextPosition textPosition = TextPosition.TOP;
    @Setter
    private float textOffsetX = 0f;
    @Setter
    private float textOffsetY = 0f;

    @Getter
    @Setter
    private boolean showValueInLabel = false;

    @Getter
    @Setter
    private String valueSeparator = "  ";

    private boolean isDragging = false;
    private float dragOffsetX = 0f;

    private Consumer<Slider> onChangedAction;

    private Consumer<Slider> onReleaseAction;

    public Slider(float x, float y, float width, float height, float min, float max, float initialValue, Color trackFillColor, Color trackEmptyColor, Color thumbColor)
    {
        super(x, y, width, height);
        this.min = min;
        this.max = max;
        this.trackFillColor = trackFillColor;
        this.trackEmptyColor = trackEmptyColor;
        this.thumbColor = thumbColor;
        this.thumbDragColor = thumbColor;
        setValue(initialValue);
    }

    public Slider(float x, float y, float width, float height, float min, float max, float initialValue, float step, Color trackFillColor, Color trackEmptyColor, Color thumbColor)
    {
        this(x, y, width, height, min, max, initialValue, trackFillColor, trackEmptyColor, thumbColor);
        this.step = step;
        setValue(initialValue);
    }

    public Slider(float x, float y, float width, float height, String label, Color labelColor, float min, float max, float initialValue, Color trackFillColor, Color trackEmptyColor, Color thumbColor)
    {
        this(x, y, width, height, min, max, initialValue, trackFillColor, trackEmptyColor, thumbColor);
        this.label = label;
        this.labelColor = labelColor;
    }

    public Slider(float x, float y, float width, float height, String label, Color labelColor, float min, float max, float initialValue, float step, Color trackFillColor, Color trackEmptyColor, Color thumbColor)
    {
        this(x, y, width, height, min, max, initialValue, trackFillColor, trackEmptyColor, thumbColor);
        this.label = label;
        this.labelColor = labelColor;
        this.step = step;
        setValue(initialValue);
    }

    public void setValue(float newValue)
    {
        this.value = snap(MathsUtils.clamp(newValue, min, max));
        this.rawProgress = normalise(this.value);
    }

    public void animateToValue(float targetValue)
    {
        snapFromProgress = rawProgress;
        snapToProgress = normalise(snap(clamp(targetValue)));
        snapStartTime = System.currentTimeMillis();
    }

    public void setOnChanged(Consumer<Slider> action)
    {
        this.onChangedAction = action;
    }

    public void setOnRelease(Consumer<Slider> action)
    {
        this.onReleaseAction = action;
    }

    public void hover(long animationDuration, Color hoverTrackFillColor, Color hoverTrackEmptyColor, Color hoverThumbColor)
    {
        this.hasHover = true;
        this.hoverAnimationDuration = animationDuration;
        this.hoverTrackFillColor = hoverTrackFillColor;
        this.hoverTrackEmptyColor = hoverTrackEmptyColor;
        this.hoverThumbColor = hoverThumbColor;
    }

    private float trackY()
    {
        return y + (height - trackHeight) / 2f;
    }

    private float trackX0()
    {
        return x + resolvedThumbSize() / 2f;
    }

    private float trackX1()
    {
        return x + width - resolvedThumbSize() / 2f;
    }

    private float trackW()
    {
        return trackX1() - trackX0();
    }

    private float thumbCentreX(float progress)
    {
        return trackX0() + progress * trackW();
    }

    private String formatValue(float v)
    {
        if (decimalPlaces <= 0) return String.valueOf(Math.round(v));
        String fmt = "%." + decimalPlaces + "f";
        return String.format(fmt, v);
    }

    private float[] resolveLabelPosition()
    {
        if (label == null || label.isEmpty() || font == null || labelColor == null) return null;

        String displayLabel = showValueInLabel ? label + valueSeparator + formatValue(value) : label;

        float textWidth = font.getWidth(displayLabel, fontSize);
        float textHeight = font.getLineHeight(fontSize);

        return switch (textPosition)
        {
            case CUSTOM -> new float[]{x + textOffsetX, y + textOffsetY, textWidth, textHeight};
            case LEFT ->
                    new float[]{x - textWidth - textSpacing, y + (height - textHeight) / 2f, textWidth, textHeight};
            case RIGHT -> new float[]{x + width + textSpacing, y + (height - textHeight) / 2f, textWidth, textHeight};
            case BOTTOM -> new float[]{x + (width - textWidth) / 2f, y + height + textSpacing, textWidth, textHeight};
            default -> // TOP
                    new float[]{x + (width - textWidth) / 2f, y - textHeight - textSpacing, textWidth, textHeight};
        };
    }

    private float ease(float t)
    {
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }

    private float clamp(float v)
    {
        return Math.max(min, Math.min(max, v));
    }

    private float normalise(float v)
    {
        return (max == min) ? 0f : (v - min) / (max - min);
    }

    private float denormalise(float p)
    {
        return min + p * (max - min);
    }

    private float snap(float v)
    {
        if (step <= 0f) return v;
        float snapped = Math.round((v - min) / step) * step + min;
        return clamp(snapped);
    }

    private float resolvedThumbSize()
    {
        return thumbSize < 0f ? height : thumbSize;
    }

    private float resolvedTrackRadius()
    {
        return trackRadius < 0 ? trackHeight / 2f : trackRadius;
    }

    @Override
    public void render(GuiGraphics context, double mouseX, double mouseY, float delta)
    {
        PoseStack matrices = context.pose();

        if (snapStartTime != -1)
        {
            long elapsed = System.currentTimeMillis() - snapStartTime;
            float raw = Math.min(1f, (float) elapsed / snapAnimationDuration);
            float eased = ease(raw);
            rawProgress = snapFromProgress + (snapToProgress - snapFromProgress) * eased;

            if (raw == 1f)
            {
                rawProgress = snapToProgress;
                value = snap(clamp(denormalise(rawProgress)));
                snapStartTime = -1;
            }
        }

        Color currentFill = trackFillColor;
        Color currentEmpty = trackEmptyColor;
        Color currentThumb = isDragging && thumbDragColor != null ? thumbDragColor : thumbColor;

        boolean isMouseOverTrack = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        if (hasHover)
        {
            if (isMouseOverTrack && !isHovered)
            {
                isHovered = true;
                hoverStartTime = System.currentTimeMillis();
            } else if (!isMouseOverTrack && isHovered && !isDragging)
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
                    currentFill = Color.interpolateColor(trackFillColor, hoverTrackFillColor, progress);
                    currentEmpty = Color.interpolateColor(trackEmptyColor, hoverTrackEmptyColor, progress);
                    if (!isDragging) currentThumb = Color.interpolateColor(thumbColor, hoverThumbColor, progress);
                } else
                {
                    currentFill = Color.interpolateColor(hoverTrackFillColor, trackFillColor, progress);
                    currentEmpty = Color.interpolateColor(hoverTrackEmptyColor, trackEmptyColor, progress);
                    if (!isDragging) currentThumb = Color.interpolateColor(hoverThumbColor, thumbColor, progress);
                    if (progress == 1f) hoverStartTime = -1;
                }
            }
        }

        float ty = trackY();
        float tx0 = trackX0();
        float tw = trackW();
        float tr = resolvedTrackRadius();
        float tCentreX = thumbCentreX(rawProgress);
        float tSize = resolvedThumbSize();

        Render2DEngine.drawRoundedRect(matrices, tx0, ty, tw, trackHeight, (int) tr, currentEmpty);

        float filledW = tCentreX - tx0;
        if (filledW > 0)
        {
            Render2DEngine.drawRoundedRect(matrices, tx0, ty, filledW, trackHeight, (int) tr, currentFill);
        }

        if (showTicks && step > 0f)
        {
            float tickY = ty + (trackHeight - tickHeight) / 2f;
            int count = Math.round((max - min) / step);
            for (int i = 0; i <= count; i++)
            {
                float tickProgress = (count == 0) ? 0f : (float) i / count;
                float tickX = tx0 + tickProgress * tw - tickWidth / 2f;
                Render2DEngine.drawRoundedRect(matrices, tickX, tickY, tickWidth, tickHeight, 0, tickColor);
            }
        }

        float thumbX = tCentreX - tSize / 2f;
        float thumbY = y + (height - tSize) / 2f;
        Render2DEngine.drawRoundedRect(matrices, thumbX, thumbY, tSize, tSize, (int) (tSize / 2f), currentThumb);

        if (showTooltip && isDragging && font != null)
        {
            String tooltipText = formatValue(value);
            float tw2 = font.getWidth(tooltipText, tooltipFontSize);
            float th2 = font.getLineHeight(tooltipFontSize);
            float boxW = tw2 + tooltipPaddingX * 2f;
            float boxH = th2 + tooltipPaddingY * 2f;
            float boxX = tCentreX - boxW / 2f;
            float boxY = thumbY - boxH - 4f;

            boxX = Math.max(x, Math.min(x + width - boxW, boxX));

            Render2DEngine.drawRoundedRect(matrices, boxX, boxY, boxW, boxH, tooltipRadius, tooltipBackground);
            font.render(matrices, tooltipText, boxX + tooltipPaddingX, boxY + tooltipPaddingY, tooltipFontSize, tooltipTextColor.getValue());
        }

        float[] labelPos = resolveLabelPosition();
        if (labelPos != null)
        {
            String displayLabel = showValueInLabel ? label + valueSeparator + formatValue(value) : label;
            if (!shadow) font.render(matrices, displayLabel, labelPos[0], labelPos[1], fontSize, labelColor.getValue());
            else
                font.renderWithShadow(matrices, displayLabel, labelPos[0], labelPos[1], fontSize, labelColor.getValue());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (button != 0) return false;

        float tSize = resolvedThumbSize();
        float centreX = thumbCentreX(rawProgress);
        float centreY = y + height / 2f;

        boolean onThumb = Math.abs((float) mouseX - centreX) <= tSize / 2f && Math.abs((float) mouseY - centreY) <= tSize / 2f;

        boolean onTrack = (float) mouseX >= trackX0() && (float) mouseX <= trackX1() && (float) mouseY >= y && (float) mouseY <= y + height;

        if (onThumb)
        {
            isDragging = true;
            dragOffsetX = (float) mouseX - centreX;
            return true;
        }

        if (onTrack)
        {
            isDragging = true;
            dragOffsetX = 0f;
            applyMouseX((float) mouseX);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (!isDragging || button != 0) return false;

        isDragging = false;

        if (step > 0f && snapOnRelease)
        {
            float snappedValue = snap(clamp(denormalise(rawProgress)));
            float snappedProgress = normalise(snappedValue);

            snapFromProgress = rawProgress;
            snapToProgress = snappedProgress;
            snapStartTime = System.currentTimeMillis();

            value = snappedValue;
        }

        if (onReleaseAction != null) onReleaseAction.accept(this);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (!isDragging || button != 0) return false;
        applyMouseX((float) mouseX - dragOffsetX);
        return true;
    }

    private void applyMouseX(float mx)
    {
        float tw = trackW();
        float rawProg = tw <= 0f ? 0f : (mx - trackX0()) / tw;
        rawProgress = Math.max(0f, Math.min(1f, rawProg));

        float rawValue = denormalise(rawProgress);

        if (step > 0f && !snapOnRelease)
        {
            value = snap(clamp(rawValue));
            rawProgress = normalise(value);
        } else
        {
            value = clamp(rawValue);
        }

        if (onChangedAction != null) onChangedAction.accept(this);
    }
}
