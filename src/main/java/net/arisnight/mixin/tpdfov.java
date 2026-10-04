package net.arisnight.mixin;
import net.arisnight.ThirdPersonDeath;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class tpdfov {
	@Shadow private boolean detached;
	@Shadow private float yRot;
	@Shadow private float xRot;
	@Shadow private Vec3 position;
	@Shadow private net.minecraft.world.entity.Entity entity;

	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void scalefovondeathscreen(CallbackInfoReturnable<Float> cir) {
		if (ThirdPersonDeath.isZoom()) {
			float progress = ThirdPersonDeath.zoomprogress();
			float basefov = cir.getReturnValue();
			float fove = 115.0f;
			cir.setReturnValue(Mth.lerp(progress, basefov, fove));
		}
	}

	@Inject(method = "setRotation", at = @At("RETURN"))
	private void applyCinematicDeathTilt(float originalYRot, float originalXRot, CallbackInfo ci) {
		if (ThirdPersonDeath.isZoom() && detached) {
			float tilt = ThirdPersonDeath.getTilt();
			this.yRot += tilt * 0.85f;
			this.xRot += tilt * 0.2f;
		}
	}

	@Inject(method = "update", at = @At("RETURN"))
	private void applySmoothDeathCamPullback(net.minecraft.client.DeltaTracker delta, CallbackInfo ci) {
		if (ThirdPersonDeath.isZoom()) {
			float dist = ThirdPersonDeath.deathCamDist();
			if (dist > 0.01f && this.entity != null) {
				this.detached = true;
				float pt = delta.getGameTimeDeltaPartialTick(true);
				float t = Mth.clamp(dist / 4.0f, 0.0f, 1.0f);
				float eased = t * t * (3.0f - 2.0f * t) * 4.0f;
				Vec3 eye = this.entity.getEyePosition(pt);
				Vec3 look = this.entity.getViewVector(pt);
				this.position = eye.add(look.scale(-eased));
			}
		}
	}
}