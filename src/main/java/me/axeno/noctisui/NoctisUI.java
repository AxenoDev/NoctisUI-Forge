package me.axeno.noctisui;

import lombok.Getter;
import me.axeno.noctisui.client.NoctisUIClient;
import me.axeno.noctisui.config.NoctisUIConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NoctisUI.MODID)
public class NoctisUI {

    public static final String MODID = "noctisui";
    @Getter private static NoctisUI instance;

    public NoctisUI() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        NoctisUIConfig.register();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        NoctisUIClient.init();
    }
}
