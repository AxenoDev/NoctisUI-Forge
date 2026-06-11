package me.axeno.noctisui.client.render.font;

import lombok.Getter;
import me.axeno.noctisui.client.component.notification.NotificationManager;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Fonts
{

    private final List<FontAtlas> loadedAtlases = new ArrayList<>();
    private FontAtlas interBold;
    private FontAtlas interSemiBold;
    private FontAtlas interMedium;
    private FontAtlas proggyClean;
    private FontAtlas poppins;
    private FontAtlas icons;
    private FontAtlas lucide;

    public void reload(ResourceManager manager)
    {
        release();
        try
        {
            this.interBold = load(manager, "inter-bold");
            this.interSemiBold = load(manager, "inter-semibold");
            this.interMedium = load(manager, "inter-medium");
            this.proggyClean = load(manager, "proggy-clean");
            this.poppins = load(manager, "poppins");
            this.icons = load(manager, "icons");
            this.lucide = load(manager, "lucide");

            NotificationManager.initFont(this);
        } catch (final IOException e)
        {
            throw new RuntimeException("Couldn't load fonts", e);
        }
    }

    private FontAtlas load(net.minecraft.server.packs.resources.ResourceManager manager, String name) throws IOException
    {
        FontAtlas atlas = new FontAtlas(manager, name);
        loadedAtlases.add(atlas);
        return atlas;
    }

    private void release()
    {
        for (FontAtlas atlas : loadedAtlases)
        {
            atlas.release();
        }
        loadedAtlases.clear();
    }
}
