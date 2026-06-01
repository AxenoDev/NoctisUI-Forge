package me.axeno.noctisui.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.axeno.noctisui.NoctisUI;
import me.axeno.noctisui.client.api.system.Shaders;
import me.axeno.noctisui.client.component.system.NotificationManager;
import me.axeno.noctisui.client.screen.TestScreen;
import me.axeno.noctisui.config.NoctisUIConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = NoctisUI.MODID, value = Dist.CLIENT)
public class ClientEvents {

    public static final KeyMapping OPEN_TEST_GUI = new KeyMapping(
            "key.noctisui.open_test_gui",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            "key.categories.noctisui"
    );

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (NotificationManager.getInstance() != null) {
            NotificationManager.renderNotifications(event.getGuiGraphics(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (OPEN_TEST_GUI.consumeClick() && NoctisUIConfig.CLIENT.isDebugEnabled()) {
            Minecraft.getInstance().setScreen(new TestScreen());
        }
    }

    @Mod.EventBusSubscriber(modid = NoctisUI.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_TEST_GUI);
        }

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new NoctisUIAssetsReloadListener());
        }

    }
}
