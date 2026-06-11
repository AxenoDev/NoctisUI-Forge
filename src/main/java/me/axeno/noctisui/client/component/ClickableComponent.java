package me.axeno.noctisui.client.component;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public abstract class ClickableComponent<T extends ClickableComponent<T>> extends UIBaseComponent
{
    @Getter
    @Setter
    private Consumer<T> onClick;

    public ClickableComponent(float x, float y, float width, float height)
    {
        super(x, y, width, height);
    }

    protected boolean isMouseOver(double mouseX, double mouseY)
    {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (isMouseOver(mouseX, mouseY))
        {
            onClicked();

            if (onClick != null)
            {
                onClick.accept((T) this);
            }
            return true;
        }
        return false;
    }

    protected void onClicked()
    {

    }
}
