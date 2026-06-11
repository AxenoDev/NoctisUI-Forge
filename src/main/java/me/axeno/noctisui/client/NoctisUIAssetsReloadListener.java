package me.axeno.noctisui.client;

import me.axeno.noctisui.client.render.Shaders;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class NoctisUIAssetsReloadListener extends SimplePreparableReloadListener<Void> {

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void ignored, ResourceManager resourceManager, ProfilerFiller profiler) {
        if (NoctisUIClient.getInstance() != null) {
            NoctisUIClient.getInstance().getFonts().reload(resourceManager);
        }
        Shaders.reload(resourceManager);
    }
}
