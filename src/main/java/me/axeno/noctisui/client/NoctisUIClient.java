package me.axeno.noctisui.client;

import me.axeno.noctisui.client.api.system.Shaders;
import me.axeno.noctisui.client.api.system.render.font.Fonts;
import me.axeno.noctisui.client.component.system.NotificationManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;

public class NoctisUIClient {

    @Getter
    private static NoctisUIClient instance;

    @Getter
    private Fonts fonts;

    public static void init() {
        instance = new NoctisUIClient();
        instance.fonts = new Fonts();
        instance.fonts.reload(Minecraft.getInstance().getResourceManager());
        Shaders.load();
        new NotificationManager();
        NotificationManager.init();
    }
}
