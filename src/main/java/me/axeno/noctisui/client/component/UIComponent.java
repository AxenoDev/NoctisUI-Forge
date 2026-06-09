package me.axeno.noctisui.client.component;

import net.minecraft.client.gui.GuiGraphics;

public interface UIComponent
{

    void render(GuiGraphics context, double mouseX, double mouseY, float delta);

    default boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return false;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        return false;
    }

    default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        return false;
    }

}
