package me.axeno.noctisui.client;

import me.axeno.noctisui.client.NoctisUIClient;
import me.axeno.noctisui.client.render.font.Fonts;
import net.minecraft.client.Minecraft;

public interface QuickImports
{
    Minecraft mc = Minecraft.getInstance();
    NoctisUIClient noctisui = NoctisUIClient.getInstance();
    Fonts fonts = noctisui.getFonts();
}
