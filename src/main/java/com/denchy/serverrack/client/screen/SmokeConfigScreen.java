package com.denchy.serverrack.client.screen;

import com.denchy.serverrack.network.payload.ClearSmokePayload;
import com.denchy.serverrack.network.payload.SmokeConfigPayload;
import com.denchy.serverrack.smoke.SmokeConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class SmokeConfigScreen extends Screen {

    private TextFieldWidget freqField;
    private TextFieldWidget countField;
    private TextFieldWidget radiusField;
    private TextFieldWidget riseField;
    private TextFieldWidget densityField;

    public SmokeConfigScreen() {
        super(Text.translatable("screen.serverrack.smoke_config"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 60;
        int fieldWidth = 60;
        int fieldHeight = 18;
        int gap = 26;
        int labelWidth = 180;

        // Frequency
        this.addDrawableChild(createLabel(centerX - labelWidth/2 - 70, startY, "Частота (тиков): 1-40"));
        freqField = new TextFieldWidget(this.textRenderer, centerX + 50, startY - 2, fieldWidth, fieldHeight, Text.literal("freq"));
        freqField.setText(String.valueOf(SmokeConfig.frequencyTicks));
        this.addDrawableChild(freqField);

        // Count
        startY += gap;
        this.addDrawableChild(createLabel(centerX - labelWidth/2 - 70, startY, "Кол-во частиц: 1-32"));
        countField = new TextFieldWidget(this.textRenderer, centerX + 50, startY - 2, fieldWidth, fieldHeight, Text.literal("count"));
        countField.setText(String.valueOf(SmokeConfig.particlesPerEmit));
        this.addDrawableChild(countField);

        // Radius
        startY += gap;
        this.addDrawableChild(createLabel(centerX - labelWidth/2 - 70, startY, "Радиус по потолку: 1-24"));
        radiusField = new TextFieldWidget(this.textRenderer, centerX + 50, startY - 2, fieldWidth, fieldHeight, Text.literal("radius"));
        radiusField.setText(String.valueOf(SmokeConfig.ceilingRadius));
        this.addDrawableChild(radiusField);

        // Rise
        startY += gap;
        this.addDrawableChild(createLabel(centerX - labelWidth/2 - 70, startY, "Высота подъема: 2-64"));
        riseField = new TextFieldWidget(this.textRenderer, centerX + 50, startY - 2, fieldWidth, fieldHeight, Text.literal("rise"));
        riseField.setText(String.valueOf(SmokeConfig.maxRiseHeight));
        this.addDrawableChild(riseField);

        // Density
        startY += gap;
        this.addDrawableChild(createLabel(centerX - labelWidth/2 - 70, startY, "Макс плотность: 1-200"));
        densityField = new TextFieldWidget(this.textRenderer, centerX + 50, startY - 2, fieldWidth, fieldHeight, Text.literal("density"));
        densityField.setText(String.valueOf(SmokeConfig.maxDensity));
        this.addDrawableChild(densityField);

        startY += gap + 10;

        // Buttons
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.serverrack.save"), btn -> saveAndClose())
                .dimensions(centerX - 100, startY, 95, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.serverrack.cancel"), btn -> this.close())
                .dimensions(centerX + 5, startY, 95, 20).build());

        startY += 26;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Удалить весь дым"), btn -> clearSmoke())
                .dimensions(centerX - 100, startY, 200, 20).build());

        startY += 26;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Сбросить по умолчанию"), btn -> resetDefaults())
                .dimensions(centerX - 100, startY, 200, 20).build());
    }

    private ButtonWidget createLabel(int x, int y, String text) {
        // Using ButtonWidget as label is hack, but we will draw text manually in render
        // Instead return invisible button that draws nothing, and we handle draw in render method
        // For simplicity, use ButtonWidget with message but disabled look? We'll just not add label widget and draw in render.
        // So create dummy that does nothing - we will override render to draw text separately
        return ButtonWidget.builder(Text.literal(text), b -> {})
                .dimensions(x, y, 0, 0).build();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        int centerX = this.width / 2;
        int startY = 60;
        int gap = 26;
        context.drawTextWithShadow(textRenderer, "Частота (тиков): 1-40", centerX - 160, startY + 3, 0xCCCCCC);
        context.drawTextWithShadow(textRenderer, "Кол-во частиц: 1-32", centerX - 160, startY + gap + 3, 0xCCCCCC);
        context.drawTextWithShadow(textRenderer, "Радиус потолка: 1-24", centerX - 160, startY + gap*2 + 3, 0xCCCCCC);
        context.drawTextWithShadow(textRenderer, "Высота подъема: 2-64", centerX - 160, startY + gap*3 + 3, 0xCCCCCC);
        context.drawTextWithShadow(textRenderer, "Макс плотность: 1-200", centerX - 160, startY + gap*4 + 3, 0xCCCCCC);

        context.drawTextWithShadow(textRenderer, "J (Ж) - вкл/выкл стойку под прицелом", centerX - 100, startY + gap*5 + 70, 0xAAAAAA);
        context.drawTextWithShadow(textRenderer, "U (Г) - это меню", centerX - 100, startY + gap*5 + 82, 0xAAAAAA);
    }

    private void saveAndClose() {
        try {
            int freq = parseClamped(freqField.getText(), SmokeConfig.FREQ_MIN, SmokeConfig.FREQ_MAX, SmokeConfig.frequencyTicks);
            int count = parseClamped(countField.getText(), SmokeConfig.COUNT_MIN, SmokeConfig.COUNT_MAX, SmokeConfig.particlesPerEmit);
            int radius = parseClamped(radiusField.getText(), SmokeConfig.RADIUS_MIN, SmokeConfig.RADIUS_MAX, SmokeConfig.ceilingRadius);
            int rise = parseClamped(riseField.getText(), SmokeConfig.RISE_MIN, SmokeConfig.RISE_MAX, SmokeConfig.maxRiseHeight);
            int density = parseClamped(densityField.getText(), SmokeConfig.DENSITY_MIN, SmokeConfig.DENSITY_MAX, SmokeConfig.maxDensity);

            SmokeConfig.set(freq, count, radius, rise, density);

            // send to server
            var payload = new SmokeConfigPayload(freq, count, radius, rise, density);
            ClientPlayNetworking.send(payload);

            this.close();
        } catch (Exception e) {
            // ignore
        }
    }

    private void clearSmoke() {
        var payload = new ClearSmokePayload();
        ClientPlayNetworking.send(payload);
        // Also clear locally immediately
        com.denchy.serverrack.smoke.CeilingSmokeManager.clearAll();
    }

    private void resetDefaults() {
        freqField.setText("4");
        countField.setText("3");
        radiusField.setText("6");
        riseField.setText("24");
        densityField.setText("40");
    }

    private int parseClamped(String s, int min, int max, int fallback) {
        try {
            int v = Integer.parseInt(s.trim());
            if (v < min) return min;
            if (v > max) return max;
            return v;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
