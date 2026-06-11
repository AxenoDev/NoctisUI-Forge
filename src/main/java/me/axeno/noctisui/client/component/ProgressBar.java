package me.axeno.noctisui.client.component;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import me.axeno.noctisui.client.render.Render2DEngine;
import me.axeno.noctisui.client.render.font.FontAtlas;
import me.axeno.noctisui.client.QuickImports;
import me.axeno.noctisui.client.utils.Color;
import me.axeno.noctisui.client.utils.MathUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProgressBar extends UIBaseComponent implements QuickImports
{

    private final List<Step> steps = new ArrayList<>();
    private float displayProgress = 0f;
    private float targetProgress = 0f;
    private float animFromProgress = 0f;
    private long animStartTime = -1L;
    @Getter
    @Setter
    private long animationDuration = 400L;
    @Getter
    private int currentStepIndex = -1;
    @Setter
    private Color fillColor;
    @Setter
    private Color trackColor;
    @Getter
    @Setter
    private float trackHeight = 6f;
    @Getter
    @Setter
    private int trackRadius = -1;
    @Getter
    @Setter
    private boolean showStepMarkers = true;
    @Getter
    @Setter
    private float stepMarkerSize = 10f;
    @Setter
    private Color stepDoneColor;
    @Setter
    private Color stepTodoColor;
    @Setter
    private Color stepActiveColor;
    @Getter
    @Setter
    private float stepBorderWidth = 1.5f;
    @Setter
    private Color stepBorderColor = new Color(255, 255, 255, 50);
    @Getter
    @Setter
    private boolean showStepLabels = true;
    @Setter
    private FontAtlas stepLabelFont;
    @Getter
    @Setter
    private int stepLabelFontSize = 8;
    @Setter
    private Color stepLabelColor = new Color(180, 180, 200);
    @Setter
    private Color stepLabelActiveColor = new Color(220, 220, 255);
    @Getter
    @Setter
    private float stepLabelOffsetY = 6f;
    @Getter
    @Setter
    private boolean showTooltip = false;
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
    @Setter
    private FontAtlas tooltipFont;
    private boolean hasHover = false;
    private long hoverDuration = 200L;
    private Color hoverFillColor;
    private Color hoverTrackColor;
    private boolean isHovered = false;
    private long hoverStartTime = -1L;
    @Getter
    @Setter
    private boolean glowOnProgress = false;
    @Setter
    private Consumer<ProgressBar> onStepReached;
    @Setter
    private Consumer<ProgressBar> onComplete;

    public ProgressBar(float x, float y, float width, float height, Color fillColor, Color trackColor)
    {
        super(x, y, width, height);
        this.fillColor = fillColor;
        this.trackColor = trackColor;
        this.stepDoneColor = fillColor;
        this.stepTodoColor = trackColor;
        this.stepActiveColor = fillColor.brighter();
        this.stepLabelFont = fonts.getInterMedium();
        this.tooltipFont = fonts.getInterMedium();
    }

    public ProgressBar(float x, float y, float width, float height, Color fillColor, Color trackColor, List<Step> steps)
    {
        this(x, y, width, height, fillColor, trackColor);
        setSteps(steps);
    }

    public void setSteps(List<Step> steps)
    {
        this.steps.clear();
        this.steps.addAll(steps);
    }

    public void goToStep(int index)
    {
        if (steps.isEmpty() || index < 0 || index >= steps.size())
        {
            return;
        }
        currentStepIndex = index;
        animateTo(steps.get(index).progress());
    }

    public void nextStep()
    {
        goToStep(currentStepIndex + 1);
    }

    public void previousStep()
    {
        goToStep(currentStepIndex - 1);
    }

    public void setProgress(float progress)
    {
        targetProgress = MathUtils.clamp(progress, 0f, 1f);
        displayProgress = targetProgress;
        animStartTime = -1L;
    }

    public void animateTo(float target)
    {
        animFromProgress = displayProgress;
        targetProgress = MathUtils.clamp(target, 0f, 1f);
        animStartTime = System.currentTimeMillis();
    }

    public void hover(long duration, Color hoverFill, Color hoverTrack)
    {
        this.hasHover = true;
        this.hoverDuration = duration;
        this.hoverFillColor = hoverFill;
        this.hoverTrackColor = hoverTrack;
    }

    private float resolvedTrackRadius()
    {
        return trackRadius < 0 ? trackHeight / 2f : trackRadius;
    }

    private float trackY()
    {
        return y + (height - trackHeight) / 2f;
    }

    @Override
    public void render(GuiGraphics context, double mouseX, double mouseY, float delta)
    {
        if (!isVisible())
        {
            return;
        }
        PoseStack matrices = context.pose();

        boolean animating = false;
        if (animStartTime != -1L)
        {
            long elapsed = System.currentTimeMillis() - animStartTime;
            float raw = Math.min(1f, (float) elapsed / animationDuration);
            float eased = MathUtils.ease(raw);
            displayProgress = animFromProgress + (targetProgress - animFromProgress) * eased;
            animating = true;

            if (raw >= 1f)
            {
                displayProgress = targetProgress;
                animStartTime = -1L;

                if (targetProgress >= 1f && onComplete != null) onComplete.accept(this);
                if (!steps.isEmpty() && onStepReached != null)
                {
                    for (Step step : steps)
                    {
                        if (Math.abs(step.progress() - targetProgress) < 0.001f)
                        {
                            onStepReached.accept(this);
                            break;
                        }
                    }
                }
            }
        }

        Color currentFill = fillColor;
        Color currentTrack = trackColor;

        if (hasHover)
        {
            boolean over = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
            if (over && !isHovered)
            {
                isHovered = true;
                hoverStartTime = System.currentTimeMillis();
            }
            if (!over && isHovered)
            {
                isHovered = false;
                hoverStartTime = System.currentTimeMillis();
            }

            if (hoverStartTime != -1L)
            {
                long elapsed = System.currentTimeMillis() - hoverStartTime;
                float p = Math.min(1f, (float) elapsed / hoverDuration);
                if (isHovered)
                {
                    currentFill = Color.interpolateColor(fillColor, hoverFillColor, p);
                    currentTrack = Color.interpolateColor(trackColor, hoverTrackColor, p);
                } else
                {
                    currentFill = Color.interpolateColor(hoverFillColor, fillColor, p);
                    currentTrack = Color.interpolateColor(hoverTrackColor, trackColor, p);
                    if (p >= 1f) hoverStartTime = -1L;
                }
            }
        }

        float ty = trackY();
        float tr = resolvedTrackRadius();

        Render2DEngine.drawRoundedRect(matrices, x, ty, width, trackHeight, (int) tr, currentTrack);

        float fillW = width * displayProgress;
        if (fillW > 0f)
        {
            Render2DEngine.drawRoundedRect(matrices, x, ty, fillW, trackHeight, (int) tr, currentFill);
        }

        if (showStepMarkers && !steps.isEmpty())
        {
            for (Step step : steps)
            {
                float markerCX = x + step.progress() * width;
                float markerCY = ty + trackHeight / 2f;
                float ms = stepMarkerSize;
                float mHalf = ms / 2f;

                boolean isDone = displayProgress >= step.progress() - 0.001f; // 0.001f tolerance
                boolean isActive = (steps.indexOf(step) == currentStepIndex);

                Color markerColor = isActive ? stepActiveColor : (isDone ? stepDoneColor : stepTodoColor);

                Render2DEngine.drawRoundedRect(matrices, markerCX - mHalf, markerCY - mHalf, ms, ms, (int) mHalf, markerColor);

                if (stepBorderWidth > 0f)
                {
                    Color borderCol = isActive
                            ? new Color(255, 255, 255, 160)
                            : stepBorderColor;
                    Render2DEngine.drawRoundedOutline(matrices, markerCX - mHalf,
                            markerCY - mHalf,
                            ms, ms, (int) mHalf,
                            stepBorderWidth, borderCol);
                }

                if (isDone && !isActive)
                {
                    float dotS = ms * 0.35f;
                    float dotH = dotS / 2f;
                    Render2DEngine.drawRoundedRect(matrices,
                            markerCX - dotH,
                            markerCY - dotH,
                            dotS, dotS, (int) dotH,
                            new Color(255, 255, 255, 200));
                }

                if (showStepLabels && step.label() != null && !step.label().isEmpty() && stepLabelFont != null)
                {
                    String lbl = step.label();
                    float lblW = stepLabelFont.getWidth(lbl, stepLabelFontSize);
                    float lblX = markerCX - lblW / 2f;
                    float lblY = markerCY + mHalf + stepLabelOffsetY;
                    Color lblC = isActive ? stepLabelActiveColor : stepLabelColor;
                    stepLabelFont.render(matrices, lbl, lblX, lblY, stepLabelFontSize, lblC.getValue());
                }

            }
        }

        if (showTooltip && tooltipFont != null && (animating || isHovered))
        {
            String text = String.format("%.0f%%", displayProgress * 100f);
            float tw = tooltipFont.getWidth(text, tooltipFontSize);
            float th = tooltipFont.getLineHeight(tooltipFontSize);
            float boxW = tw + tooltipPaddingX * 2f;
            float boxH = th + tooltipPaddingY * 2f;
            float headX = x + fillW;
            float boxX = Math.max(x, Math.min(x + width - boxW, headX - boxW / 2f));
            float boxY = ty - boxH - 5f;

            Render2DEngine.drawRoundedRect(matrices, boxX, boxY, boxW, boxH, tooltipRadius, tooltipBackground);
            tooltipFont.render(matrices, text,
                    boxX + tooltipPaddingX,
                    boxY + tooltipPaddingY,
                    tooltipFontSize, tooltipTextColor.getValue());
        }

    }

    public record Step(String label, float progress)
    {
    }
}
