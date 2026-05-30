package me.axeno.noctisui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class GuiUtils {

    public static void drawImage(GuiGraphics guiGraphics, int x, int y, int width, int height, ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(texture, x, y, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.disableBlend();
    }

    public static void drawImage(GuiGraphics guiGraphics, int x, int y, int width, int height, float percentage, ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, percentage);
        guiGraphics.blit(texture, x, y, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    public static int scaleWidth(int width, float percentage) {
        return (int) ((float) width * (percentage / 80.0F));
    }

    public static int scaleHeight(int height, float percentage) {
        return (int) ((float) height * (percentage / 80.0F));
    }

    public static float getScaleFactor(int height) {
        return Math.min(height / 720.0F, 1.0F);
    }

    public static Color3f colorLerp(Color3f start, Color3f end, float percentage) {
        return new Color3f(MathsUtils.lerp(start.getRed() * 255.0F, end.getRed() * 255.0F, percentage), MathsUtils.lerp(start.getGreen() * 255.0F, end.getGreen() * 255.0F, percentage), MathsUtils.lerp(start.getBlue() * 255.0F, end.getBlue() * 255.0F, percentage));
    }

}
