package com.denchy.serverrack.client.screen;

import com.denchy.serverrack.det.NukeConfig;
import com.denchy.serverrack.network.payload.NukeConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Yellow mushroom tuning. Opens by HOLDING the director's detonator in air
 * (quick tap - boom menu, hold ~1.5s - this screen).
 */
public class NukeConfigScreen extends Screen {

    private TextFieldWidget stemField;
    private TextFieldWidget capField;
    private TextFieldWidget ringField;
    private TextFieldWidget densityField;

    public NukeConfigScreen() {
        super(Text.translatable("screen.serverrack.nuke_config"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 60;
        int fieldWidth = 60;
        int fieldHeight = 18;
        int gap = 26;

        stemField = new TextFieldWidget(this.textRenderer, centerX + 50, startY - 2, fieldWidth, fieldHeight, Text.literal("stem"));
        stemField.setText(String.valueOf(NukeConfig.stemHeight));
        this.addDrawableChild(stemField);

        startY += gap;
        capField = new TextFieldWidget(this.textRenderer, centerX + 50, startY - 2, fieldWidth, fieldHeight, Text.literal("cap"));
        capField.setText(String.valueOf(NukeConfig.capRadius));
        this.addDrawableChild(capField);

        startY += gap;
        ringField = new TextFieldWidget(this.textRenderer, centerX + 50, startY - 2, fieldWidth, fieldHeight, Text.literal("ring"));
        ringField.setText(String.valueOf(NukeConfig.ringRadius));
        this.addDrawableChild(ringField);

        startY += gap;
        densityField = new TextFieldWidget(this.textRenderer, centerX + 50, startY - 2, fieldWidth, fieldHeight, Text.literal("density"));
        densityField.setText(String.valueOf(NukeConfig.density));
        this.addDrawableChild(densityField);

        startY += gap + 10;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.serverrack.save"), btn -> saveAndClose())
                .dimensions(centerX - 100, startY, 95, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.serverrack.cancel"), btn -> this.close())
                .dimensions(centerX + 5, startY, 95, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFDD55);

        int centerX = this.width / 2;
        int startY = 60;
        int gap = 26;
        context.drawTextWithShadow(textRenderer, "Высота стебля: 4-40", centerX - 160, startY + 3, 0xCCCCCC);
        context.drawTextWithShadow(textRenderer, "Радиус шляпки: 3-20", centerX - 160, startY + gap + 3, 0xCCCCCC);
        context.drawTextWithShadow(textRenderer, "Радиус кольца: 4-28", centerX - 160, startY + gap * 2 + 3, 0xCCCCCC);
        context.drawTextWithShadow(textRenderer, "Плотность частиц: 5-100", centerX - 160, startY + gap * 3 + 3, 0xCCCCCC);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Долгий ПКМ детонатором в воздухе - это меню · тип взрыва: «Ядерный гриб»"),
                centerX, startY + gap * 4 + 42, 0x888888);
    }

    private void saveAndClose() {
        try {
            int stem = parseClamped(stemField.getText(), NukeConfig.STEM_MIN, NukeConfig.STEM_MAX, NukeConfig.stemHeight);
            int cap = parseClamped(capField.getText(), NukeConfig.CAP_MIN, NukeConfig.CAP_MAX, NukeConfig.capRadius);
            int ring = parseClamped(ringField.getText(), NukeConfig.RING_MIN, NukeConfig.RING_MAX, NukeConfig.ringRadius);
            int dens = parseClamped(densityField.getText(), NukeConfig.DENSITY_MIN, NukeConfig.DENSITY_MAX, NukeConfig.density);

            NukeConfig.set(stem, cap, ring, dens);
            ClientPlayNetworking.send(new NukeConfigPayload(stem, cap, ring, dens));
            this.close();
        } catch (Exception e) {
            // ignore, keep old values
        }
    }

    private int parseClamped(String s, int min, int max, int fallback) {
        try {
            int v = Integer.parseInt(s.trim());
            return Math.max(min, Math.min(max, v));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
