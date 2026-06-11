package me.axeno.noctisui.client.render;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import me.axeno.noctisui.NoctisUI;
import me.axeno.noctisui.client.QuickImports;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
public class Shaders implements QuickImports {

    public static ShaderInstance ROUNDED_RECT;
    public static ShaderInstance ROUNDED_OUTLINE;
    public static ShaderInstance CIRCLE;
    public static ShaderInstance MSDF;
    public static ShaderInstance COLOR_PICKER;
    public static ShaderInstance BLUR;

    public static Uniform msdfPxrange;

    public static Uniform colorPickerResolution;
    public static Uniform colorPickerPosition;
    public static Uniform colorPickerHue;
    public static Uniform colorPickerAlpha;

    @Getter
    private static boolean initialized = false;

    public static void reload(ResourceManager manager) {
        log.info("Reloading shaders...");
        load();
        log.info("Shaders reloaded successfully.");
    }

    public static void load() {
        initialized = ROUNDED_RECT != null && ROUNDED_OUTLINE != null;
    }

    @Mod.EventBusSubscriber(modid = NoctisUI.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Registration {

        @SubscribeEvent
        public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
            register(event, "rounded_rect", DefaultVertexFormat.POSITION_COLOR, shader -> ROUNDED_RECT = shader);
            register(event, "rounded_outline", DefaultVertexFormat.POSITION_COLOR, shader -> ROUNDED_OUTLINE = shader);
            register(event, "circle", DefaultVertexFormat.POSITION_COLOR, shader -> CIRCLE = shader);
            register(event, "msdf", DefaultVertexFormat.POSITION_TEX_COLOR, shader -> {
                MSDF = shader;
                msdfPxrange = shader.getUniform("pxRange");
            });
            register(event, "color_picker", DefaultVertexFormat.POSITION_COLOR, shader -> {
                COLOR_PICKER = shader;
                colorPickerResolution = shader.getUniform("Resolution");
                colorPickerPosition = shader.getUniform("Position");
                colorPickerHue = shader.getUniform("Hue");
                colorPickerAlpha = shader.getUniform("Alpha");
            });
            register(event, "blur", DefaultVertexFormat.POSITION, shader -> BLUR = shader);
            initialized = true;
        }

        private static void register(RegisterShadersEvent event, String name,
                                     com.mojang.blaze3d.vertex.VertexFormat format,
                                     Consumer<ShaderInstance> onLoaded) throws IOException {
            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            ResourceLocation.fromNamespaceAndPath(NoctisUI.MODID, name),
                            format
                    ),
                    onLoaded
            );
        }
    }
}
