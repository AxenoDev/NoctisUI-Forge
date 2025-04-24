package fr.libnaus.noctisui.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.libnaus.noctisui.utils.GuiUtils;
import fr.libnaus.noctisui.utils.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

public abstract class AbstractHUD {
    protected final Minecraft mc = Minecraft.getInstance();
    private final ResourceLocation texture;
    protected int containerWidth, containerHeight, containerX, containerY;
    protected float scale;

    public AbstractHUD(ResourceLocation texture) {
        this.texture = texture;
    }

    @SubscribeEvent
    public void onRender(RenderGuiOverlayEvent event) {
        if (this.mc != null && this.mc.player != null) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            this.setupContainer(guiGraphics);
            this.render(guiGraphics);
        }
    }

    protected void setupContainer(GuiGraphics guiGraphics) {
        int screenWidth = this.mc.getWindow().getGuiScaledWidth();
        int screenHeight = this.mc.getWindow().getGuiScaledHeight();
        this.containerHeight = GuiUtils.scaleWidth(screenWidth, this.getContainerWidthScale());
        this.containerWidth = GuiUtils.scaleHeight(screenHeight, this.getContainerHeightScale());
        this.containerX = this.getContainerX(screenWidth);
        this.containerY = this.getContainerY(screenHeight);
        this.scale = this.containerHeight / 20.0F * this.getScaleFactor();

        GuiUtils.drawImage(guiGraphics, this.containerX, this.containerY, this.containerWidth, this.containerHeight, this.texture);
    }

    protected abstract void render(GuiGraphics stack);

    protected abstract int getContainerWidthScale();

    protected abstract int getContainerHeightScale();

    protected abstract float getScaleFactor();

    protected abstract Position setContainerPositionX();

    protected abstract Position setContainerPositionY();

    protected int getContainerX(int screenWidth) {
        Position.X position = this.setContainerPositionX().getX();
        int x = this.setContainerPositionX().getOffsetX();
        int xpos;
        switch (position) {
            case LEFT -> xpos = x;
            case CENTER -> xpos = (screenWidth - this.containerWidth) / 2 + x;
            default -> xpos = screenWidth - this.containerWidth - x;
        }

        return xpos;
    };

    protected int getContainerY(int screenHeight) {
        Position.Y position = this.setContainerPositionY().getY();
        int y = this.setContainerPositionY().getOffsetY();
        int ypos;
        switch (position) {
            case TOP -> ypos = y;
            case CENTER -> ypos = (screenHeight - this.containerHeight) / 2 + y;
            default -> ypos = screenHeight - this.containerHeight - y;
        }

        return ypos;
    };

    protected void beginScale(PoseStack stack) {
        stack.pushPose();
        stack.scale(this.scale, this.scale, 1.0F);
    }

    protected void endScale(PoseStack stack) {
        stack.popPose();
    }

    protected float getScaleFactor(float value) {
        return value / this.scale;
    }

    protected int getCenteredTextX(int baseX, int width) {
        return (int)(baseX + width / 2.0F);
    }

    protected int getTextY() {
        float containerYF = (float)this.containerY;
        float containerHeightF = (float)this.containerHeight;
        Objects.requireNonNull(this.mc.getWindow());
        return (int)(containerYF + (containerHeightF - 9.0F * this.scale) / 2.0F);
    }
}
