package net.arisnight.mixin;
import net.arisnight.ThirdPersonDeath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class tpdminecraft {

    @Inject(method = "setScreenAndShow", at = @At("TAIL"))
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        ThirdPersonDeath.detectscreen();
    }
}