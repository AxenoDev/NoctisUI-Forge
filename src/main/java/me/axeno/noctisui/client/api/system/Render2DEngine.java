package me.axeno.noctisui.client.api.system;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.axeno.noctisui.client.common.QuickImports;
import me.axeno.noctisui.client.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class Render2DEngine implements QuickImports
{

    private static RenderTarget blurCapture;
    private static RenderTarget blurTemp;

    public static void drawLine(PoseStack matrices, float x, float y, float x1, float y1, float width, Color color)
    {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrices.last().pose(), x, y, 0f).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrices.last().pose(), x1, y1, 0f).color(r, g, b, a).endVertex();

        RenderSystem.lineWidth(width);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    public static void drawOutline(PoseStack matrices, float x, float y, float width, float height, Color color)
    {
        drawLine(matrices, x, y, x + width, y, 1, color);
        drawLine(matrices, x + width, y, x + width, y + height, 1, color);
        drawLine(matrices, x - 0.5f, y + height, x + width, y + height, 1, color);
        drawLine(matrices, x, y, x, y + height + 0.5f, 1, color);
    }

    public static void drawRoundedOutline(PoseStack matrices, float x1, float y1, float x2, float y2, float radius,
                                          float width, Color color)
    {
        drawRoundedOutline(matrices, x1, y1, x2, y2, radius, width, color, color, color, color);
    }

    public static void drawOutlinedGradient(PoseStack matrices, float x1, float y1, float x2, float y2, float radius,
                                            float width, Color color1, Color color2)
    {
        drawRoundedOutline(matrices, x1, y1, x2, y2, radius, width, color1, color1, color2, color2);
    }

    public static void drawOutlinedVerticalGradient(PoseStack matrices, float x1, float y1, float x2, float y2,
                                                    float radius, float width, Color color1, Color color2)
    {
        drawRoundedOutline(matrices, x1, y1, x2, y2, radius, width, color2, color1, color2, color1);
    }

    public static void drawOutline(PoseStack matrices, float x1, float y1, float x2, float y2, float width,
                                   Color color)
    {
        drawLine(matrices, x1, y1, x1 + x2, y1, width, color);
        drawLine(matrices, x1 + x2, y1, x1 + x2, y1 + y2, width, color);
        drawLine(matrices, x1 - 0.5f, y1 + y2, x1 + x2, y1 + y2, width, color);
        drawLine(matrices, x1, y1, x1, y1 + y2 + 0.5f, width, color);
    }

    public static void drawRoundedOutline(PoseStack matrices, float x1, float y1, float x2, float y2, float radius,
                                          float width, Color color1, Color color2, Color color3, Color color4)
    {
        if (Shaders.ROUNDED_OUTLINE == null)
        {
            Shaders.load();
        }

        x2 = x1 + x2;
        y2 = y1 + y2;

        ShaderInstance shaderProg = Shaders.ROUNDED_OUTLINE;
        if (shaderProg == null) throw new IllegalStateException("Shader program is not available.");

        float scaleFactor = (float) mc.getWindow().getGuiScale();
        int windowHeight = mc.getWindow().getHeight();

        Vector3f start = transformPosition(matrices, x1, y1, 0f);
        Vector3f end = transformPosition(matrices, x2, y2, 0f);

        float[] actualCoords = getActualCoordinates(start, end, scaleFactor, windowHeight);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        prepareBuffer(buffer, matrices.last().pose(), x1, y1, x2, y2, 0f, color1);

        setUniform(shaderProg, "Radius", radius * scaleFactor);
        setUniform(shaderProg, "Bounds", actualCoords[0] + 1, actualCoords[3] + 1, actualCoords[2] - 1, actualCoords[1] - 1);
        setUniform(shaderProg, "Smoothness", 2f);
        setUniform(shaderProg, "StrokeWidth", width);
        setUniform(shaderProg, "color1", color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, color1.getAlpha() / 255f);
        setUniform(shaderProg, "color2", color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, color2.getAlpha() / 255f);
        setUniform(shaderProg, "color3", color3.getRed() / 255f, color3.getGreen() / 255f, color3.getBlue() / 255f, color3.getAlpha() / 255f);
        setUniform(shaderProg, "color4", color4.getRed() / 255f, color4.getGreen() / 255f, color4.getBlue() / 255f, color4.getAlpha() / 255f);

        renderShape(buffer, Shaders.ROUNDED_OUTLINE);
    }

    public static void drawRect(PoseStack matrices, float x, float y, float width, float height, Color color)
    {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrices.last().pose(), x, y + height, 0f).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrices.last().pose(), x + width, y + height, 0f).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrices.last().pose(), x + width, y, 0f).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrices.last().pose(), x, y, 0f).color(r, g, b, a).endVertex();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    public static void drawRoundedRect(PoseStack matrices, float x1, float y1, float x2, float y2, float radius,
                                       Color color)
    {
        drawRoundedRect(matrices, x1, y1, x2, y2, radius, color, color, color, color);
    }

    public static void drawRoundedRect(PoseStack matrices, float x1, float y1, float x2, float y2, float topLeft,
                                       float topRight, float bottomLeft, float bottomRight, Color color)
    {
        drawRoundedRect(matrices, x1, y1, x2, y2, topLeft, topRight, bottomLeft, bottomRight, color, color, color, color);
    }

    public static void drawRoundedRect(PoseStack matrices, float x1, float y1, float x2, float y2, float radius,
                                       Color color1, Color color2, Color color3, Color color4)
    {
        drawRoundedRect(matrices, x1, y1, x2, y2, radius, radius, radius, radius, color1, color2, color3, color4);
    }

    public static void drawRoundedRect(PoseStack matrices, float x1, float y1, float x2, float y2, float topLeft,
                                       float topRight, float bottomLeft, float bottomRight, Color color1, Color color2,
                                       Color color3, Color color4)
    {
        if (Shaders.ROUNDED_RECT == null)
        {
            Shaders.load();
        }

        x2 = x1 + x2;
        y2 = y1 + y2;

        float scaleFactor = (float) mc.getWindow().getGuiScale();
        int windowHeight = mc.getWindow().getHeight();

        ShaderInstance shaderProg = Shaders.ROUNDED_RECT;
        if (shaderProg == null) throw new IllegalStateException("Shader program is not available.");

        Vector3f start = transformPosition(matrices, x1, y1, 0f);
        Vector3f end = transformPosition(matrices, x2, y2, 0f);

        float[] actualCoords = getActualCoordinates(start, end, scaleFactor, windowHeight);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        prepareBuffer(buffer, matrices.last().pose(), x1, y1, x2, y2, 0f, color1);

        setUniform(shaderProg, "RadiusTopLeft", topLeft * scaleFactor);
        setUniform(shaderProg, "RadiusTopRight", topRight * scaleFactor);
        setUniform(shaderProg, "RadiusBottomLeft", bottomLeft * scaleFactor);
        setUniform(shaderProg, "RadiusBottomRight", bottomRight * scaleFactor);
        setUniform(shaderProg, "Bounds", actualCoords[0], actualCoords[3], actualCoords[2], actualCoords[1]);
        setUniform(shaderProg, "Smoothness", 2f);
        setUniform(shaderProg, "color1", color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, color1.getAlpha() / 255f);
        setUniform(shaderProg, "color2", color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, color2.getAlpha() / 255f);
        setUniform(shaderProg, "color3", color3.getRed() / 255f, color3.getGreen() / 255f, color3.getBlue() / 255f, color3.getAlpha() / 255f);
        setUniform(shaderProg, "color4", color4.getRed() / 255f, color4.getGreen() / 255f, color4.getBlue() / 255f, color4.getAlpha() / 255f);

        renderShape(buffer, shaderProg);
    }

    private static void setUniform(ShaderInstance shader, String name, float... values)
    {
        var uniform = shader.getUniform(name);
        if (uniform != null)
        {
            uniform.set(values);
        }
    }

    private static Vector3f transformPosition(PoseStack matrices, float x, float y, float z)
    {
        Matrix4f positionMatrix = matrices.last().pose();
        return positionMatrix.transformPosition(x, y, z, new Vector3f());
    }

    public static void prepareBuffer(BufferBuilder buffer, Matrix4f positionMatrix, float x1, float y1, float x2,
                                     float y2, float z, Color color)
    {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;
        buffer.vertex(positionMatrix, x1, y1, z).color(r, g, b, a).endVertex();
        buffer.vertex(positionMatrix, x1, y2, z).color(r, g, b, a).endVertex();
        buffer.vertex(positionMatrix, x2, y2, z).color(r, g, b, a).endVertex();
        buffer.vertex(positionMatrix, x2, y1, z).color(r, g, b, a).endVertex();
    }

    private static void renderShape(BufferBuilder buffer, ShaderInstance shaderInstance)
    {
        RenderSystem.disableDepthTest();

        ShaderInstance last = RenderSystem.getShader();
        RenderSystem.setShader(() -> shaderInstance);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.disableBlend();

        RenderSystem.setShader(() -> last);

        RenderSystem.enableDepthTest();
    }

    private static float[] getActualCoordinates(Vector3f start, Vector3f end, float scaleFactor, int windowHeight)
    {
        float actualX1 = start.x * scaleFactor;
        float actualX2 = end.x * scaleFactor;
        float actualY1 = windowHeight - start.y * scaleFactor;
        float actualY2 = windowHeight - end.y * scaleFactor;
        return new float[]{actualX1, actualY1, actualX2, actualY2};
    }

    private static void ensureBlurTargets(int width, int height)
    {
        if (blurCapture == null)
        {
            blurCapture = new TextureTarget(width, height, false, false);
            blurCapture.setFilterMode(9729);
        } else if (blurCapture.viewWidth != width || blurCapture.viewHeight != height)
        {
            blurCapture.resize(width, height, false);
            blurCapture.setFilterMode(9729);
        }

        if (blurTemp == null)
        {
            blurTemp = new TextureTarget(width, height, false, false);
            blurTemp.setFilterMode(9729);
        } else if (blurTemp.viewWidth != width || blurTemp.viewHeight != height)
        {
            blurTemp.resize(width, height, false);
            blurTemp.setFilterMode(9729);
        }
    }

    /**
     * Copies the current main framebuffer into an off-screen target so blur can sample it safely.
     */
    private static void captureMainFramebuffer(RenderTarget main)
    {
        int width = main.viewWidth;
        int height = main.viewHeight;
        ensureBlurTargets(width, height);

        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, blurCapture.frameBufferId);
        GlStateManager._glBlitFrameBuffer(
                0, 0, width, height,
                0, 0, width, height,
                GL11.GL_COLOR_BUFFER_BIT,
                GL11.GL_LINEAR
        );
        main.bindWrite(false);
    }

    private static void setupBlurShader(ShaderInstance shader, RenderTarget source, int fbW, int fbH,
                                        float dirX, float dirY, float brightness,
                                        float applyMask, float pixelX, float pixelY, float pixelW, float pixelH,
                                        float cornerRadius)
    {
        setUniform(shader, "InputResolution", (float) fbW, (float) fbH);
        setUniform(shader, "BlurDirection", dirX, dirY);
        setUniform(shader, "Brightness", brightness);
        setUniform(shader, "ApplyMask", applyMask);
        setUniform(shader, "uSize", pixelW, pixelH);
        setUniform(shader, "uLocation", pixelX, pixelY);
        setUniform(shader, "radius", cornerRadius);
        shader.setSampler("InputSampler", source.getColorTextureId());
    }

    private static void drawBlurQuad(Matrix4f matrix, float x, float y, float width, float height)
    {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.vertex(matrix, x, y + height, 0).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).endVertex();
        buffer.vertex(matrix, x + width, y, 0).endVertex();
        buffer.vertex(matrix, x, y, 0).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void runBlurPass(RenderTarget source, RenderTarget dest, RenderTarget restoreWrite,
                                    float dirX, float dirY, float brightness, float applyMask,
                                    Matrix4f matrix, float x, float y, float width, float height,
                                    float pixelX, float pixelY, float pixelW, float pixelH, float cornerRadius,
                                    int fbW, int fbH)
    {
        ShaderInstance shader = Shaders.BLUR;
        dest.bindWrite(true);

        setupBlurShader(shader, source, fbW, fbH, dirX, dirY, brightness, applyMask,
                pixelX, pixelY, pixelW, pixelH, cornerRadius);

        ShaderInstance last = RenderSystem.getShader();
        RenderSystem.setShader(() -> shader);
        RenderSystem.disableDepthTest();
        if (applyMask < 0.5f)
        {
            RenderSystem.disableBlend();
        } else
        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        drawBlurQuad(matrix, x, y, width, height);

        RenderSystem.setShader(() -> last);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        restoreWrite.bindWrite(false);
    }

    /**
     * Draws a frosted-glass blur over the current framebuffer content.
     *
     * @param quality    blur strength in pixels (internally converted to multiple small blur passes)
     * @param brightness output brightness multiplier
     */
    public static void drawBlur(PoseStack matrices, float x, float y, float width, float height,
                                float radius, float quality, float brightness)
    {
        if (Shaders.BLUR == null || width <= 0 || height <= 0)
        {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        var window = minecraft.getWindow();
        int framebufferWidth = mainTarget.viewWidth;
        int framebufferHeight = mainTarget.viewHeight;
        int guiScale = (int) window.getGuiScale();
        float scaledW = window.getGuiScaledWidth();
        float scaledH = window.getGuiScaledHeight();

        mainTarget.bindWrite(false);
        captureMainFramebuffer(mainTarget);

        Vector3f topLeft = new Vector3f();
        Vector3f bottomRight = new Vector3f();
        matrices.last().pose().transformPosition(x, y, 0, topLeft);
        matrices.last().pose().transformPosition(x + width, y + height, 0, bottomRight);

        float pixelX = topLeft.x * guiScale;
        float pixelY = framebufferHeight - bottomRight.y * guiScale;
        float pixelW = (bottomRight.x - topLeft.x) * guiScale;
        float pixelH = (bottomRight.y - topLeft.y) * guiScale;
        float cornerRadius = radius * guiScale;

        PoseStack identity = new PoseStack();
        Matrix4f fullscreenMatrix = identity.last().pose();

        // Each H+V pair uses a small fixed-radius kernel (~4 px). Stack passes for smooth blur.
        int iterations = Math.max(2, Math.min(10, Math.round(quality / 3f)));

        RenderTarget src = blurCapture;
        RenderTarget dst = blurTemp;

        for (int i = 0; i < iterations; i++)
        {
            runBlurPass(src, dst, mainTarget,
                    1f, 0f, 1f, 0f,
                    fullscreenMatrix, 0, 0, scaledW, scaledH,
                    0, 0, pixelW, pixelH, 0,
                    framebufferWidth, framebufferHeight);
            RenderTarget swap = src;
            src = dst;
            dst = swap;

            runBlurPass(src, dst, mainTarget,
                    0f, 1f, 1f, 0f,
                    fullscreenMatrix, 0, 0, scaledW, scaledH,
                    0, 0, pixelW, pixelH, 0,
                    framebufferWidth, framebufferHeight);
            swap = src;
            src = dst;
            dst = swap;
        }

        // Composite pre-blurred texture onto the panel (ApplyMask = 2)
        runBlurPass(src, mainTarget, mainTarget,
                0f, 0f, brightness, 2f,
                matrices.last().pose(), x, y, width, height,
                pixelX, pixelY, pixelW, pixelH, cornerRadius,
                framebufferWidth, framebufferHeight);
    }
}
