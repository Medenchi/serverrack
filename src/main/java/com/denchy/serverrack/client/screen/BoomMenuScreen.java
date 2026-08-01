package com.denchy.serverrack.client.screen;

import com.denchy.serverrack.det.BoomType;
import com.denchy.serverrack.network.payload.BoomActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Director menu (RMB in air with the detonator).
 * Sends absolute values so sync state never drifts between opens.
 * P still fires instantly without opening this.
 */
public class BoomMenuScreen extends Screen {

    private BoomType type = BoomType.MELTDOWN;
    private float speed = 1.0f;

    private ButtonWidget typeBtn;
    private ButtonWidget speedBtn;

    public BoomMenuScreen() {
        super(Text.translatable("screen.serverrack.boom_menu"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 82;

        typeBtn = addDrawableChild(ButtonWidget.builder(
                Text.literal(typeLabel()), b -> {
                    type = type.next();
                    ClientPlayNetworking.send(new BoomActionPayload("type_" + type.ordinal()));
                    b.setMessage(Text.literal(typeLabel()));
                }).dimensions(cx - 110, y, 220, 20).build());

        speedBtn = addDrawableChild(ButtonWidget.builder(
                Text.literal(speedLabel()), b -> {
                    speed = speed >= 1.0f ? 0.5f : (speed >= 0.5f ? 0.25f : 1.0f);
                    ClientPlayNetworking.send(new BoomActionPayload("speed_" + speed));
                    b.setMessage(Text.literal(speedLabel()));
                }).dimensions(cx - 110, y + 24, 220, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§c§lБАБАХ §r§7(клавиша P)"), b -> {
                    ClientPlayNetworking.send(new BoomActionPayload("boom"));
                    close();
                }).dimensions(cx - 110, y + 58, 220, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§aВосстановить сцену (дубль 2)"), b -> {
                    ClientPlayNetworking.send(new BoomActionPayload("restore"));
                    close();
                }).dimensions(cx - 110, y + 82, 220, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§eБэкап выделения заново"), b ->
                    ClientPlayNetworking.send(new BoomActionPayload("rebackup"))
                ).dimensions(cx - 110, y + 110, 107, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Закрыть"), b -> close()
                ).dimensions(cx + 3, y + 110, 107, 20).build());
    }

    private String typeLabel() {
        return "§eВзрыв: §f" + type.ruName();
    }

    private String speedLabel() {
        return "§eТемп: §fx" + speed + (speed < 1.0f ? "  §7(cinemaslow-mo)" : "");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                this.width / 2, this.height / 2 - 98, 0xFF5555);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7" + type.ruHint()), this.width / 2, this.height / 2 - 22, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§8ЛКМ/ПКМ детонатором - точки · восстановление из бэкапа на диске"),
                this.width / 2, this.height / 2 + 54, 0x666666);
    }
}
