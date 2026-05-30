package me.axeno.noctisui.config;

import fr.libnaus.noctisui.client.NoctisUIClient;
import lombok.Getter;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class NoctisUIConfig {

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }

    private NoctisUIConfig() {}

    public static void register()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue debug_enabled;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("NoctisUI client configuration").push("noctisui");
            builder.push("debug");
            debug_enabled = builder
                    .comment("Enable debug mode for NoctisUI (toggles debug rendering and features like a test menu)")
                    .define("enabled", false);
            builder.pop();
            builder.pop();
        }

        public boolean isDebugEnabled() {
            return debug_enabled.get();
        }
    }
}
