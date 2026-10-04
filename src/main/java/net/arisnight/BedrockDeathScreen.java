package net.arisnight;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class BedrockDeathScreen extends Screen {
    private static final int TITLE_SCALE = 2;

    private static final long BG_FADE_MS = 700;
    private static final long UI_DELAY_MS = 1000;
    private static final long UI_FADE_MS = 700;

    private final Component causeOfDeath;
    private final boolean hardcore;
    private final long openTime = System.currentTimeMillis();

    private Button respawnButton;
    private Button menuButton;

    public BedrockDeathScreen(Component causeOfDeath, boolean hardcore) {
        super(Component.translatable(hardcore ? "deathScreen.title.hardcore" : "deathScreen.title"));
        this.causeOfDeath = causeOfDeath;
        this.hardcore = hardcore;
    }

    private static float easeOutCubic(float t) {
        t = Math.max(0f, Math.min(1f, t));
        float inv = 1f - t;
        return 1f - inv * inv * inv;
    }

    private float bgAlpha() {
        long e = System.currentTimeMillis() - openTime;
        return easeOutCubic(e / (float) BG_FADE_MS);
    }

    private float uiAlpha() {
        long e = System.currentTimeMillis() - openTime - UI_DELAY_MS;
        if (e <= 0) return 0f;
        return easeOutCubic(e / (float) UI_FADE_MS);
    }

    private static int scaleAlpha(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int na = Math.max(0, Math.min(255, Math.round(a * factor)));
        return (na << 24) | (argb & 0x00FFFFFF);
    }

    @Override
    protected void init() {
        super.init();

        int bw = 200;
        int bh = 20;
        int cx = this.width / 2;
        int baseY = this.height - 85;

        Component respawnText = this.hardcore
                ? Component.translatable("deathScreen.spectate")
                : Component.translatable("thirdpersondeath.button.respawn");

        this.respawnButton = this.addRenderableWidget(Button.builder(respawnText, b -> doRespawn())
                .bounds(cx - bw / 2, baseY, bw, bh).build());
        this.menuButton = this.addRenderableWidget(
                Button.builder(Component.translatable("thirdpersondeath.button.game_menu"), b -> doExitToTitle())
                        .bounds(cx - bw / 2, baseY + 25, bw, bh).build());

        updateButtons();
    }

    private void updateButtons() {
        float a = uiAlpha();
        boolean clickable = a >= 1f;
        if (respawnButton != null) {
            respawnButton.setAlpha(a);
            respawnButton.active = clickable;
            respawnButton.visible = a > 0f;
        }
        if (menuButton != null) {
            menuButton.setAlpha(a);
            menuButton.active = clickable;
            menuButton.visible = a > 0f;
        }
    }

    private void doRespawn() {
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection().send(
                    new net.minecraft.network.protocol.game.ServerboundClientCommandPacket(
                            net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action.PERFORM_RESPAWN
                    )
            );
        } else if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.respawn();
        }
    }

    private void doExitToTitle() {
        if (this.minecraft == null) return;
        if (this.minecraft.level != null) {
            this.minecraft.level.disconnect(ClientLevel.DEFAULT_QUIT_MESSAGE);
        }
        this.minecraft.disconnectWithSavingScreen();
        this.minecraft.setScreenAndShow(new TitleScreen());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        extractDeathBackground(gfx, this.width, this.height, bgAlpha());
    }

    private static void extractDeathBackground(GuiGraphicsExtractor gfx, int w, int h, float alpha) {
        gfx.fillGradient(0, 0, w, h, scaleAlpha(0xCC0A0A0A, alpha), scaleAlpha(0xDD1A0505, alpha));
        int red = scaleAlpha(0x44220000, alpha);
        gfx.fillGradient(0, 0, w, 70, red, 0);
        gfx.fillGradient(0, h - 70, w, h, 0, red);
        int edge = 55;
        for (int i = 0; i < edge; i++) {
            float k = 1f - i / (float) edge;
            int c = scaleAlpha(0x44220000, alpha * k * k);
            gfx.fill(i, 0, i + 1, h, c);
            gfx.fill(w - i - 1, 0, w - i, h, c);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        updateButtons();
        super.extractRenderState(gfx, mouseX, mouseY, partialTick);

        ActiveTextCollector collector = gfx.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
        this.visitText(collector);
    }

    private void visitText(ActiveTextCollector collector) {
        float a = uiAlpha();
        if (a <= 0f) return;

        ActiveTextCollector.Parameters base = collector.defaultParameters();
        ActiveTextCollector.Parameters faded = base.withOpacity(a);
        int cx = this.width / 2;

        Component titleComp = Component.translatable(
                        this.hardcore ? "deathScreen.title.hardcore" : "thirdpersondeath.title.death")
                .setStyle(Style.EMPTY.withFont(new net.minecraft.network.chat.FontDescription.Resource(
                        Identifier.fromNamespaceAndPath("thirdpersondeath", "death_title"))));
        collector.defaultParameters(faded.withScale(TITLE_SCALE));
        collector.accept(TextAlignment.CENTER, cx / 2, 30, titleComp);

        collector.defaultParameters(faded);
        if (this.causeOfDeath != null) {
            collector.accept(TextAlignment.CENTER, cx, 82, this.causeOfDeath);
        }

        collector.defaultParameters(base);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}