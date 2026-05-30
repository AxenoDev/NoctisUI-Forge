package me.axeno.noctisui;

import lombok.Getter;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NoctisUI.MODID)
public class NoctisUI {

    public static final String MODID = "noctisui";
    @Getter private static NoctisUI instance;
//    private final TestHUD testHUD;
//    private boolean hudVisible = false;

    public NoctisUI() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
//        testHUD = new TestHUD("Mon HUD personnalisé");
    }

    private void clientSetup(FMLClientSetupEvent event) {

    }
}
