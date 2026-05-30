package me.axeno.noctisui.client.component;

import net.minecraft.client.gui.GuiGraphics;

public interface UIComponent
{

    void render(GuiGraphics context, double mouseX, double mouseY, float delta);

    boolean mouseClicked(double mouseX, double mouseY, int button);

}
