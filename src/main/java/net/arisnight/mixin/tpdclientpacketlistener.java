package net.arisnight.mixin;

import net.arisnight.BedrockDeathScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class tpdclientpacketlistener {

    @Inject(method = "handlePlayerCombatKill", at = @At("HEAD"), cancellable = true)
    private void onHandlePlayerCombatKill(ClientboundPlayerCombatKillPacket packet, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            boolean hardcore = false;
            if (client.level != null) {
                hardcore = client.level.getLevelData().isHardcore();
            }
            if (client.player != null && !client.player.shouldShowDeathScreen()) {
                if (client.getConnection() != null) {
                    client.getConnection().send(
                            new net.minecraft.network.protocol.game.ServerboundClientCommandPacket(
                                    net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action.PERFORM_RESPAWN
                            )
                    );
                } else {
                    client.execute(() -> {
                        if (client.player != null) client.player.respawn();
                    });
                }
                ci.cancel();
                return;
            }
            final var deathMessage = packet.message();
            final boolean isHardcore = hardcore;
            client.execute(() -> client.setScreenAndShow(new BedrockDeathScreen(deathMessage, isHardcore)));
            ci.cancel();
        }
    }
}