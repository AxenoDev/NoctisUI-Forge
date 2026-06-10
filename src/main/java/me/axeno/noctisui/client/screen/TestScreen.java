package me.axeno.noctisui.client.screen;

import me.axeno.noctisui.client.NoctisUIClient;
import me.axeno.noctisui.client.api.system.render.font.FontAtlas;
import me.axeno.noctisui.client.component.*;
import me.axeno.noctisui.client.component.input.TextInput;
import me.axeno.noctisui.client.component.system.NotificationManager;
import me.axeno.noctisui.client.utils.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class TestScreen extends Screen
{

    private static final float PANEL_WIDTH = 440;
    private static final float PANEL_HEIGHT = 420;

    private final List<TextInput> textInputs = new ArrayList<>();
    private DivComponent root;
    private TextComponent statusText;
    private TextInput nameInput;
    private TextInput emailInput;

    public TestScreen()
    {
        super(Component.literal("NoctisUI Test"));
    }

    @Override
    protected void init()
    {
        textInputs.clear();

        float panelX = (this.width - PANEL_WIDTH) / 2f;
        float panelY = (this.height - PANEL_HEIGHT) / 2f;

        FontAtlas interBold = NoctisUIClient.getInstance().getFonts().getInterBold();
        FontAtlas interMedium = NoctisUIClient.getInstance().getFonts().getInterMedium();

        root = new DivComponent(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        root.enableBlur(26f, 0.95f);
        root.setBackgroundColor(new Color(0, 0, 0, 90));
        root.setCornerRadius(14);
        root.setOutline(new Color(60, 60, 80, 180), 1.5f);

        // ── Header ───────────────────────────────────────────────────────────
        root.addChild(new TextComponent(24, 20, "NoctisUI", 18, new Color(220, 220, 255), interBold));
        root.addChild(new TextComponent(24, 44, "GUI de test — composants Forge 1.20.1", 10, new Color(140, 140, 160), interMedium));

        // ── Nom ──────────────────────────────────────────────────────────────
        root.addChild(new TextComponent(24, 78, "Nom", 9, new Color(180, 180, 200), interMedium));
        nameInput = createTextInput(24, 92, 392, 32, interMedium, TextInput.InputType.TEXT);
        nameInput.setPlaceholder("Entrez votre nom...");
        nameInput.setMaxLength(32);
        root.addChild(nameInput);

        // ── Email ─────────────────────────────────────────────────────────────
        root.addChild(new TextComponent(24, 138, "Email", 9, new Color(180, 180, 200), interMedium));
        emailInput = createTextInput(24, 152, 392, 32, interMedium, TextInput.InputType.EMAIL);
        emailInput.setPlaceholder("exemple@email.com");
        emailInput.setShowValidation(true);
        emailInput.setValidateOnType(true);
        root.addChild(emailInput);

        // ── Notifications ─────────────────────────────────────────────────────
        root.addChild(new TextComponent(24, 200, "Notifications", 9, new Color(180, 180, 200), interMedium));
        root.addChild(createNotifyButton(24, 214, 92, 28, "Succès", new Color(34, 120, 70), NotificationType.SUCCESS));
        root.addChild(createNotifyButton(124, 214, 92, 28, "Erreur", new Color(160, 45, 45), NotificationType.ERROR));
        root.addChild(createNotifyButton(224, 214, 92, 28, "Alerte", new Color(160, 110, 30), NotificationType.WARNING));
        root.addChild(createNotifyButton(324, 214, 92, 28, "Info", new Color(45, 90, 160), NotificationType.INFO));

        // ── Checkbox ──────────────────────────────────────────────────────────
        Checkbox rememberCheckbox = new Checkbox(24, 256, 14, 14, "Se souvenir de moi", new Color(40, 40, 55, 200), new Color(70, 110, 220), new Color(180, 180, 200));
        rememberCheckbox.setFont(interMedium);
        rememberCheckbox.setFontSize(9);
        rememberCheckbox.setRadius(3);
        rememberCheckbox.setOutline(new Color(80, 80, 110, 200), 1f);
        rememberCheckbox.hover(160, new Color(55, 55, 75, 220), new Color(90, 130, 240));
        rememberCheckbox.setOnToggle(cb -> setStatus(cb.isChecked() ? "Se souvenir activé." : "Se souvenir désactivé.", cb.isChecked() ? new Color(120, 200, 140) : new Color(180, 180, 200)));
        root.addChild(rememberCheckbox);

        // ── Switch ────────────────────────────────────────────────────────────
        Switch notifSwitch = new Switch(24, 278, 30, 16, "Activer les notifications", new Color(50, 50, 65, 220), // track OFF
                new Color(70, 110, 220), // track ON
                Color.WHITE, // thumb
                new Color(180, 180, 200) // label
        );
        notifSwitch.setFont(interMedium);
        notifSwitch.setFontSize(9);
        notifSwitch.setOutline(new Color(80, 80, 110, 200), 1f);
        notifSwitch.hover(160, new Color(65, 65, 80, 240), // hover track OFF
                new Color(90, 130, 240), // hover track ON
                Color.WHITE // hover thumb
        );
        notifSwitch.setOnToggle(sw -> setStatus(sw.isEnabled() ? "Notifications activées." : "Notifications désactivées.", sw.isEnabled() ? new Color(120, 200, 140) : new Color(180, 180, 200)));
        root.addChild(notifSwitch);

        // ── Slider ─────────────────────────────────────────────────────────────
        Slider volumeSlider = new Slider(24, 306, 392, 20, "Volume", new Color(180, 180, 200), 0f, 100f, 50f, new Color(70, 110, 220), new Color(80, 80, 100), Color.WHITE);
        volumeSlider.setFont(interMedium);
        volumeSlider.setFontSize(9);
        volumeSlider.setStep(1f);
        volumeSlider.setShowValueInLabel(true);
        volumeSlider.setValue(50f);
        volumeSlider.setOnChanged(s -> setStatus("Volume: " + Math.round(s.getValue()), new Color(140, 200, 220)));
        volumeSlider.setOnRelease(s -> NotificationManager.getInstance().info("slider_release", "Slider", "Valeur réglée: " + Math.round(s.getValue())));
        root.addChild(volumeSlider);

        // ── Boutons principaux ────────────────────────────────────────────────
        Button submitButton = new Button(24, 340, 190, 34, "Valider", new Color(70, 110, 220), Color.WHITE);
        submitButton.setRadius(8);
        submitButton.setFont(interBold);
        submitButton.setFontSize(11);
        submitButton.hover(180, new Color(90, 130, 240), new Color(255, 255, 200));
        submitButton.setOnClick(b -> onSubmit());
        root.addChild(submitButton);

        Button closeButton = new Button(226, 340, 190, 34, "Fermer (Esc)", new Color(55, 55, 65), new Color(200, 200, 210));
        closeButton.setRadius(8);
        closeButton.setFont(interMedium);
        closeButton.setFontSize(10);
        closeButton.hover(180, new Color(75, 75, 85), Color.WHITE);
        closeButton.setOnClick(b -> onClose());
        root.addChild(closeButton);

        // ── Status ────────────────────────────────────────────────────────────
        statusText = new TextComponent(24, 388, "Prêt.", 9, new Color(120, 200, 140), interMedium);
        root.addChild(statusText);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TextInput createTextInput(float x, float y, float w, float h, FontAtlas font, TextInput.InputType type)
    {
        TextInput input = new TextInput(x, y, w, h, font, type);
        textInputs.add(input);
        return input;
    }

    private Button createNotifyButton(float x, float y, float w, float h, String label, Color bg, NotificationType type)
    {
        Button button = new Button(x, y, w, h, label, bg, Color.WHITE);
        button.setRadius(6);
        button.setFontSize(9);
        button.hover(150, bg.brighter(), new Color(255, 255, 200));
        button.setOnClick(b -> showNotification(type));
        return button;
    }

    private void onSubmit()
    {
        String name = nameInput.getText().trim();
        String email = emailInput.getText().trim();

        if (name.isEmpty())
        {
            setStatus("Entrez un nom.", new Color(220, 120, 120));
            NotificationManager.getInstance().warning("test_validation", "Validation", "Le champ nom est requis.");
            return;
        }

        if (!emailInput.isValid())
        {
            setStatus("Email invalide.", new Color(220, 120, 120));
            NotificationManager.getInstance().error("test_validation", "Validation", "Adresse email invalide.");
            return;
        }

        setStatus("Envoyé : " + name + " <" + email + ">", new Color(120, 200, 140));
        NotificationManager.getInstance().success("test_submit", "Enregistré", "Bonjour " + name + " !");
    }

    private void showNotification(NotificationType type)
    {
        NotificationManager manager = NotificationManager.getInstance();
        switch (type)
        {
            case SUCCESS ->
            {
                manager.success("test_success", "Succès", "Notification de succès.");
                setStatus("Notification succès affichée.", new Color(120, 200, 140));
            }
            case ERROR ->
            {
                manager.error("test_error", "Erreur", "Notification d'erreur.");
                setStatus("Notification erreur affichée.", new Color(220, 120, 120));
            }
            case WARNING ->
            {
                manager.warning("test_warning", "Alerte", "Notification d'avertissement.");
                setStatus("Notification alerte affichée.", new Color(220, 180, 100));
            }
            case INFO ->
            {
                manager.info("test_info", "Info", "Notification informative.");
                setStatus("Notification info affichée.", new Color(120, 160, 220));
            }
        }
    }

    private void setStatus(String message, Color color)
    {
        statusText.setText(message);
        statusText.setColor(color);
    }

    // ── Screen overrides ──────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (root != null) root.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics)
    {
        // Keep the world visible so blur can sample it.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (root != null) root.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean mouseReleased(double p_94722_, double p_94723_, int p_94724_)
    {
        if (root != null) root.mouseReleased(p_94722_, p_94723_, p_94724_);
        return true;
    }

    @Override
    public boolean mouseDragged(double p_94699_, double p_94700_, int p_94701_, double p_94702_, double p_94703_)
    {
        if (root != null) root.mouseDragged(p_94699_, p_94700_, p_94701_, p_94702_, p_94703_);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        for (TextInput input : textInputs)
        {
            if (input.keyPressed(keyCode, scanCode, modifiers)) return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        for (TextInput input : textInputs)
        {
            if (input.charTyped(chr, modifiers)) return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    private enum NotificationType
    {
        SUCCESS, ERROR, WARNING, INFO
    }
}