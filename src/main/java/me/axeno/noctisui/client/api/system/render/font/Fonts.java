package me.axeno.noctisui.client.api.system.render.font;

import me.axeno.noctisui.client.component.system.NotificationManager;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Fonts {

    private FontAtlas interBold;
    private FontAtlas interSemiBold;
    private FontAtlas interMedium;
    private FontAtlas proggyClean;
    private FontAtlas poppins;
    private FontAtlas icons;
    private FontAtlas lucide;

    private final List<FontAtlas> loadedAtlases = new ArrayList<>();

    public void reload(net.minecraft.server.packs.resources.ResourceManager manager) {
        release();
        try {
            this.interBold = load(manager, "inter-bold");
            this.interSemiBold = load(manager, "inter-semibold");
            this.interMedium = load(manager, "inter-medium");
            this.proggyClean = load(manager, "proggy-clean");
            this.poppins = load(manager, "poppins");
            this.icons = load(manager, "icons");
            this.lucide = load(manager, "lucide");

            NotificationManager.initFont(this);
        } catch (final IOException e) {
            throw new RuntimeException("Couldn't load fonts", e);
        }
    }

    private FontAtlas load(net.minecraft.server.packs.resources.ResourceManager manager, String name) throws IOException {
        FontAtlas atlas = new FontAtlas(manager, name);
        loadedAtlases.add(atlas);
        return atlas;
    }

    private void release() {
        for (FontAtlas atlas : loadedAtlases) {
            atlas.release();
        }
        loadedAtlases.clear();
    }
}
