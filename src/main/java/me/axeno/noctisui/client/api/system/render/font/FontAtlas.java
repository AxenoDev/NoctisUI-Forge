package me.axeno.noctisui.client.api.system.render.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Setter;
import me.axeno.noctisui.NoctisUI;
import me.axeno.noctisui.client.api.system.Shaders;
import me.axeno.noctisui.client.common.QuickImports;
import me.axeno.noctisui.client.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class FontAtlas implements QuickImports
{
    private final static String FORMATTING_PALETTE = "0123456789abcdefklmnor";
    private final static int[][] FORMATTING_COLOR_PALETTE = new int[32][3];

    static
    {
        for (int i = 0; i < 32; ++i)
        {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;

            if (i == 6)
            {
                k += 85;
            }


            if (i >= 16)
            {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            FORMATTING_COLOR_PALETTE[i][0] = k;
            FORMATTING_COLOR_PALETTE[i][1] = l;
            FORMATTING_COLOR_PALETTE[i][2] = i1;
        }
    }

    private final int[] textColor = new int[3];
    private final int distanceRange;
    private final int width;
    private final int height;
    private final Glyph[] glyphs = new Glyph[2048 * 2048];
    private final FontMetrics fontMetrics;
    private final DynamicTexture tex;
    private final ResourceLocation textureId;
    private volatile float textX;
    @Setter
    private float size = 9;

    public FontAtlas(final ResourceManager manager, final String name, String modid) throws IOException
    {
        this(name, new InputStreamReader(manager.open(ResourceLocation.fromNamespaceAndPath(modid, "fonts/" + name + ".json"))),
                manager.open(ResourceLocation.fromNamespaceAndPath(modid, "fonts/" + name + ".png")));
    }

    public FontAtlas(final ResourceManager manager, final String name) throws IOException
    {
        this(name, new InputStreamReader(manager.open(ResourceLocation.fromNamespaceAndPath(NoctisUI.MODID, "fonts/" + name + ".json"))),
                manager.open(ResourceLocation.fromNamespaceAndPath(NoctisUI.MODID, "fonts/" + name + ".png")));
    }

    public FontAtlas(final String name, final Reader meta, final InputStream texture) throws IOException
    {
        this.textureId = ResourceLocation.fromNamespaceAndPath(NoctisUI.MODID, "font/" + name);
        this.tex = new DynamicTexture(NativeImage.read(texture));
        Minecraft.getInstance().getTextureManager().register(textureId, tex);

        final JsonObject atlasJson = JsonParser.parseReader(meta).getAsJsonObject();

        if (!"msdf".equals(atlasJson.getAsJsonObject("atlas").get("type").getAsString()))
        {
            throw new RuntimeException("Unsupported atlas-type");
        }

        this.width = atlasJson.getAsJsonObject("atlas").get("width").getAsInt();
        this.height = atlasJson.getAsJsonObject("atlas").get("height").getAsInt();
        this.distanceRange = atlasJson.getAsJsonObject("atlas").get("distanceRange").getAsInt();
        this.fontMetrics = FontMetrics.parse(atlasJson.getAsJsonObject("metrics"));

        for (final JsonElement glyphElement : atlasJson.getAsJsonArray("glyphs"))
        {
            final JsonObject glyphObject = glyphElement.getAsJsonObject();
            final Glyph glyph = Glyph.parse(glyphObject);

            this.glyphs[glyph.getUnicode()] = glyph;
        }
    }

    /**
     * Interpolates between two colors based on time and a speed factor.
     *
     * @param color1 The first color to interpolate from.
     * @param color2 The second color to interpolate to.
     * @param speed  The speed at which the interpolation occurs in seconds.
     * @param index  An index value to offset the interpolation.
     * @return The interpolated color.
     */
    public static Color interpolateColor(Color color1, Color color2, int speed, int index)
    {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return interpolateColorSimple(color1, color2, angle / 360f);
    }

    /**
     * Simple linear interpolation between two colors.
     *
     * @param color1 The first color.
     * @param color2 The second color.
     * @param ratio  The interpolation ratio (0.0 to 1.0).
     * @return The interpolated color.
     */
    public static Color interpolateColorSimple(Color color1, Color color2, float ratio)
    {
        int red = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * ratio);
        int green = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * ratio);
        int blue = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * ratio);
        return new Color(red, green, blue);
    }

    public String truncate(final String text, final float width, final float size)
    {
        if (getWidth(text, size) <= width) return text;

        StringBuilder truncated = new StringBuilder();

        for (int i = 0; i < text.length(); i++)
        {
            if (getWidth(truncated.toString(), size) < width)
            {
                truncated.append(text.charAt(i));
            } else
            {
                truncated.append("...");
                break;
            }
        }
        return truncated.toString();
    }

    public void release()
    {
        Minecraft.getInstance().getTextureManager().release(textureId);
    }

    private void bindMsdfShader()
    {
        if (Shaders.MSDF == null)
        {
            return;
        }
        if (Shaders.msdfPxrange != null)
        {
            Shaders.msdfPxrange.set((float) distanceRange);
        }
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.setShader(() -> Shaders.MSDF);
    }

    public void render(final PoseStack PoseStack, final String text, final float x, final float y, final int color)
    {
        this.render(PoseStack, text, x, y, size, color);
    }

    public void renderRightString(final PoseStack PoseStack, final String text, final float x, final float y,
                                  final float size, final int color)
    {
        this.render(PoseStack, text, x - getWidth(text), y, size, color);
    }

    public void renderRightString(final PoseStack PoseStack, final String text, final float x, final float y,
                                  final int color)
    {
        this.render(PoseStack, text, x - getWidth(text), y, size, color);
    }

    public void renderCenteredString(final PoseStack PoseStack, final String text, final float x, final float y,
                                     final int color)
    {
        this.render(PoseStack, text, x - getWidth(text) / 2f, y, size, color);
    }

    public void renderCenteredString(final PoseStack PoseStack, final String text, final float x, final float y,
                                     final float size, final int color)
    {
        this.render(PoseStack, text, x - getWidth(text) / 2f, y, size, color);
    }

    public void renderHorizontalGradient(PoseStack matrices, String text, float x, float y, float size,
                                         Color primaryColor, Color secondaryColor, int speed)
    {
        if (Shaders.MSDF == null)
        {
            Shaders.load();
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderInstance lastShader = RenderSystem.getShader();
        bindMsdfShader();

        final Matrix4f model = matrices.last().pose();

        int alpha = primaryColor.getAlpha();

        final BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        boolean hasContent = false;

        float currentX = x;

        for (int i = 0; i < text.length(); i++)
        {
            int unicode = text.codePointAt(i);

            if (unicode == '§' && i + 1 < text.length())
            {
                i++;
            } else
            {
                final Glyph glyph = this.glyphs[unicode];

                if (glyph == null) continue;

                if (glyph.getPlaneRight() - glyph.getPlaneLeft() != 0)
                {
                    int index = (int) (currentX - x);

                    Color charColor = interpolateColor(primaryColor, secondaryColor, speed, index);

                    float x0 = currentX + glyph.getPlaneLeft() * size;
                    float x1 = currentX + glyph.getPlaneRight() * size;
                    float y0 = y + fontMetrics.getAscender() * size - glyph.getPlaneTop() * size;
                    float y1 = y + fontMetrics.getAscender() * size - glyph.getPlaneBottom() * size;
                    float u0 = glyph.getAtlasLeft() / width;
                    float u1 = glyph.getAtlasRight() / width;
                    float v0 = glyph.getAtlasTop() / height;
                    float v1 = glyph.getAtlasBottom() / height;

                    bufferBuilder.vertex(model, x0, y0, 0).uv(u0, 1 - v0).color(charColor.getRed() / 255f, charColor.getGreen() / 255f, charColor.getBlue() / 255f, alpha / 255f).endVertex();
                    bufferBuilder.vertex(model, x0, y1, 0).uv(u0, 1 - v1).color(charColor.getRed() / 255f, charColor.getGreen() / 255f, charColor.getBlue() / 255f, alpha / 255f).endVertex();
                    bufferBuilder.vertex(model, x1, y1, 0).uv(u1, 1 - v1).color(charColor.getRed() / 255f, charColor.getGreen() / 255f, charColor.getBlue() / 255f, alpha / 255f).endVertex();
                    bufferBuilder.vertex(model, x1, y0, 0).uv(u1, 1 - v0).color(charColor.getRed() / 255f, charColor.getGreen() / 255f, charColor.getBlue() / 255f, alpha / 255f).endVertex();
                    hasContent = true;
                }
                currentX += size * glyph.getAdvance();
            }
        }

        if (hasContent)
        {
            BufferUploader.drawWithShader(bufferBuilder.end());
        }

        RenderSystem.setShader(() -> lastShader);
        RenderSystem.disableBlend();
    }

    public void renderDiagonalGradient(PoseStack matrices, String text, float x, float y, float size,
                                       Color primaryColor, Color secondaryColor, int speed, float verticalStrength)
    {
        if (Shaders.MSDF == null)
        {
            Shaders.load();
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderInstance lastShader = RenderSystem.getShader();
        bindMsdfShader();

        final Matrix4f model = matrices.last().pose();
        int alpha = primaryColor.getAlpha();

        final BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        boolean hasContent = false;

        float currentX = x;

        int yOffset = (int) (y * verticalStrength * 5);

        for (int i = 0; i < text.length(); i++)
        {
            int unicode = text.codePointAt(i);

            if (unicode == '§' && i + 1 < text.length())
            {
                i++;
            } else
            {
                final Glyph glyph = this.glyphs[unicode];

                if (glyph == null) continue;

                if (glyph.getPlaneRight() - glyph.getPlaneLeft() != 0)
                {
                    int xIndex = (int) (currentX - x);

                    int combinedIndex = xIndex + yOffset;

                    Color charColor = interpolateColor(primaryColor, secondaryColor, speed, -combinedIndex);

                    float x0 = currentX + glyph.getPlaneLeft() * size;
                    float x1 = currentX + glyph.getPlaneRight() * size;
                    float y0 = y + fontMetrics.getAscender() * size - glyph.getPlaneTop() * size;
                    float y1 = y + fontMetrics.getAscender() * size - glyph.getPlaneBottom() * size;
                    float u0 = glyph.getAtlasLeft() / width;
                    float u1 = glyph.getAtlasRight() / width;
                    float v0 = glyph.getAtlasTop() / height;
                    float v1 = glyph.getAtlasBottom() / height;

                    bufferBuilder.vertex(model, x0, y0, 0).uv(u0, 1 - v0).color(charColor.getRed() / 255f, charColor.getGreen() / 255f, charColor.getBlue() / 255f, alpha / 255f).endVertex();
                    bufferBuilder.vertex(model, x0, y1, 0).uv(u0, 1 - v1).color(charColor.getRed() / 255f, charColor.getGreen() / 255f, charColor.getBlue() / 255f, alpha / 255f).endVertex();
                    bufferBuilder.vertex(model, x1, y1, 0).uv(u1, 1 - v1).color(charColor.getRed() / 255f, charColor.getGreen() / 255f, charColor.getBlue() / 255f, alpha / 255f).endVertex();
                    bufferBuilder.vertex(model, x1, y0, 0).uv(u1, 1 - v0).color(charColor.getRed() / 255f, charColor.getGreen() / 255f, charColor.getBlue() / 255f, alpha / 255f).endVertex();
                    hasContent = true;
                }
                currentX += size * glyph.getAdvance();
            }
        }

        if (hasContent)
        {
            BufferUploader.drawWithShader(bufferBuilder.end());
        }

        RenderSystem.setShader(() -> lastShader);
        RenderSystem.disableBlend();
    }

    public void render(final PoseStack matrices, final FormattedCharSequence text, final float x, final float y,
                       final float size, final int color)
    {
        if (Shaders.MSDF == null) Shaders.load();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderInstance lastShader = RenderSystem.getShader();
        bindMsdfShader();

        this.textX = x;

        final Matrix4f model = matrices.last().pose();
        final int alpha = FastColor.ARGB32.alpha(color);
        final int red = FastColor.ARGB32.red(color);
        final int green = FastColor.ARGB32.green(color);
        final int blue = FastColor.ARGB32.blue(color);

        this.textColor[0] = red;
        this.textColor[1] = green;
        this.textColor[2] = blue;

        final BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        text.accept((index, style, codePoint) ->
        {
            final Glyph glyph = this.glyphs[codePoint];

            if (glyph == null) return true;
            if (style.getColor() == null)
            {
                this.textColor[0] = red;
                this.textColor[1] = green;
                this.textColor[2] = blue;
            } else
            {
                final int rgb = style.getColor().getValue();
                this.textColor[0] = FastColor.ARGB32.red(rgb);
                this.textColor[1] = FastColor.ARGB32.green(rgb);
                this.textColor[2] = FastColor.ARGB32.blue(rgb);
            }
            this.textX += this.visit(model, bufferBuilder, glyph, textX, y, size, alpha);
            return true;
        });

        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.setShader(() -> lastShader);
        RenderSystem.disableBlend();
    }

    public void render(PoseStack matrices, String text, float x, float y, float size, int color)
    {
        if (Shaders.MSDF == null) Shaders.load();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderInstance lastShader = RenderSystem.getShader();
        bindMsdfShader();

        final Matrix4f model = matrices.last().pose();
        int alpha = FastColor.ARGB32.alpha(color);
        int red = FastColor.ARGB32.red(color);
        int green = FastColor.ARGB32.green(color);
        int blue = FastColor.ARGB32.blue(color);

        this.textColor[0] = red;
        this.textColor[1] = green;
        this.textColor[2] = blue;

        final BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (int i = 0; i < text.length(); i++)
        {
            int unicode = text.codePointAt(i);

            if (unicode == '§' && i + 1 < text.length())
            {
                final int colorIndex = FORMATTING_PALETTE.indexOf(Character.toLowerCase(text.charAt(i + 1)));
                if (colorIndex >= 0 && colorIndex < 16)
                {
                    System.arraycopy(FORMATTING_COLOR_PALETTE[colorIndex], 0, textColor, 0, 3);
                } else if (colorIndex == 21)
                {
                    textColor[0] = red;
                    textColor[1] = green;
                    textColor[2] = blue;
                }
                i++;
            } else
            {
                final Glyph glyph = this.glyphs[unicode];

                if (glyph == null) continue;
                if (glyph.getPlaneRight() - glyph.getPlaneLeft() != 0)
                {
                    float x0 = x + glyph.getPlaneLeft() * size;
                    float x1 = x + glyph.getPlaneRight() * size;
                    float y0 = y + fontMetrics.getAscender() * size - glyph.getPlaneTop() * size;
                    float y1 = y + fontMetrics.getAscender() * size - glyph.getPlaneBottom() * size;
                    float u0 = glyph.getAtlasLeft() / width;
                    float u1 = glyph.getAtlasRight() / width;
                    float v0 = glyph.getAtlasTop() / height;
                    float v1 = glyph.getAtlasBottom() / height;

                    bufferBuilder.vertex(model, x0, y0, 0).uv(u0, 1 - v0).color(textColor[0] / 255f, textColor[1] / 255f, textColor[2] / 255f, alpha / 255f).endVertex();
                    bufferBuilder.vertex(model, x0, y1, 0).uv(u0, 1 - v1).color(textColor[0] / 255f, textColor[1] / 255f, textColor[2] / 255f, alpha / 255f).endVertex();
                    bufferBuilder.vertex(model, x1, y1, 0).uv(u1, 1 - v1).color(textColor[0] / 255f, textColor[1] / 255f, textColor[2] / 255f, alpha / 255f).endVertex();
                    bufferBuilder.vertex(model, x1, y0, 0).uv(u1, 1 - v0).color(textColor[0] / 255f, textColor[1] / 255f, textColor[2] / 255f, alpha / 255f).endVertex();
                }
                x += size * glyph.getAdvance();
            }
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.setShader(() -> lastShader);
        RenderSystem.disableBlend();
    }

    public void renderWithShadow(final PoseStack matrices, final String text, final float x, final float y,
                                 final float size, final int color)
    {
        RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
        this.render(matrices, text, x + 0.75F, y + 0.75F, size, color);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        this.render(matrices, text, x, y, size, color);
    }

    public void renderWithShadow(final PoseStack matrices, final String text, final float x, final float y,
                                 final int color)
    {
        RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
        this.render(matrices, text, x + 0.75F, y + 0.75F, size, color);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        this.render(matrices, text, x, y, size, color);
    }

    private float visit(final Matrix4f model, final BufferBuilder bufferBuilder, final Glyph glyph, final float x,
                        final float y, final float size, final int alpha)
    {
        if (glyph.getPlaneRight() - glyph.getPlaneLeft() != 0)
        {
            float x0 = x + glyph.getPlaneLeft() * size;
            float x1 = x + glyph.getPlaneRight() * size;
            float y0 = y + fontMetrics.getAscender() * size - glyph.getPlaneTop() * size;
            float y1 = y + fontMetrics.getAscender() * size - glyph.getPlaneBottom() * size;
            float u0 = glyph.getAtlasLeft() / width;
            float u1 = glyph.getAtlasRight() / width;
            float v0 = glyph.getAtlasTop() / height;
            float v1 = glyph.getAtlasBottom() / height;

            bufferBuilder.vertex(model, x0, y0, 0).uv(u0, 1 - v0).color(textColor[0] / 255f, textColor[1] / 255f, textColor[2] / 255f, alpha / 255f).endVertex();
            bufferBuilder.vertex(model, x0, y1, 0).uv(u0, 1 - v1).color(textColor[0] / 255f, textColor[1] / 255f, textColor[2] / 255f, alpha / 255f).endVertex();
            bufferBuilder.vertex(model, x1, y1, 0).uv(u1, 1 - v1).color(textColor[0] / 255f, textColor[1] / 255f, textColor[2] / 255f, alpha / 255f).endVertex();
            bufferBuilder.vertex(model, x1, y0, 0).uv(u1, 1 - v0).color(textColor[0] / 255f, textColor[1] / 255f, textColor[2] / 255f, alpha / 255f).endVertex();
        }
        return size * glyph.getAdvance();
    }

    public final float getSize()
    {
        return this.size;
    }

    public final float getWidth(final Component text, final float size)
    {
        return this.getWidth(text.getVisualOrderText(), size);
    }

    public final float getWidth(final FormattedCharSequence text, final float size)
    {
        final float[] sum = new float[1];

        text.accept((index, style, codePoint) ->
        {
            final Glyph glyph = this.glyphs[codePoint];

            if (glyph == null) return true;
            if (glyph.getPlaneRight() - glyph.getPlaneLeft() != 0)
            {
                sum[0] += size * glyph.getAdvance();
            }
            return true;
        });
        return sum[0];
    }

    public final float getWidth(final String text)
    {
        return this.getWidth(text, size);
    }

    public float getWidth(String text, float size)
    {
        float sum = 0;
        for (int i = 0; i < text.length(); i++)
        {
            final int unicode = text.codePointAt(i);

            if (unicode == '§' && i + 1 < text.length())
            {
                i++;
            } else
            {
                final Glyph glyph = glyphs[unicode];
                if (glyph != null)
                {
                    sum += size * glyph.getAdvance();
                }
            }
        }
        return sum;
    }

    public final float getLineHeight()
    {
        return this.getLineHeight(size);
    }

    public final float getLineHeight(final float size)
    {
        return this.fontMetrics.getLineHeight() * size;
    }

    public Vector2f getStringSize(final String text)
    {
        return new Vector2f(getWidth(text), getLineHeight());
    }

    public void drawStringWithShadow(PoseStack matrices, String text, float x, float y, int argbColor)
    {
        renderWithShadow(matrices, text, x, y, this.size, argbColor);
    }

}
