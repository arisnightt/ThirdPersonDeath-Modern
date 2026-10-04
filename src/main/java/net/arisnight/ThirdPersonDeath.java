package net.arisnight;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;

public class ThirdPersonDeath implements ClientModInitializer {
	private static boolean deathscreen = false; private static CameraType playerperspective = null; private static long DeathTime = 0; private static boolean zoom = false;
	@Override
	public void onInitializeClient() {}
	public static void detectscreen() {Minecraft client = Minecraft.getInstance(); if (isDeathScreen(client.gui.screen())) {if (!deathscreen) {screenisdeath(client);}
		deathscreen = true;
	} else {if (deathscreen) {screenisnotdeath(client);}
		deathscreen = false;
	}
	}

	private static boolean isDeathScreen(Screen screen) {
		if (screen instanceof DeathScreen) return true;
		return screen instanceof BedrockDeathScreen;
	}
	private static void screenisdeath(Minecraft client) {playerperspective = client.options.getCameraType(); client.options.setCameraType(CameraType.THIRD_PERSON_BACK); DeathTime = System.currentTimeMillis();
		zoom = true;
	}

	private static void screenisnotdeath(Minecraft client) {if (playerperspective != null) {client.options.setCameraType(playerperspective); playerperspective = null;}
		zoom = false; DeathTime = 0;
	}

	public static boolean isZoom() {return zoom;}
	public static float zoomprogress() {if (!zoom || DeathTime == 0) {return 0.0f;}
		long time = System.currentTimeMillis(); long passed = time - DeathTime; float seconds = passed / 1000.0f;
		if (seconds >= 30.0f) {return 1.0f;}
		float progress = seconds / 30.0f; if (progress <= (8.57f / 30.0f)) {return progress * (30.0f / 8.57f) * 0.9f;}
		else {float smoothout = (progress - (8.57f / 30.0f)) / (1.43f / 30.0f); float end = 1.0f - (float) Math.pow(1.0f - smoothout, 2.0f); return 0.9f + (0.1f * end);}
	}

	public static float getTilt() {
		if (!zoom || DeathTime == 0) return 0.0f;
		float p = zoomprogress();
		return p * 30.0f;
	}

	public static float deathCamDist() {
		if (!zoom || DeathTime == 0) return 0.0f;
		float p = zoomprogress();
		float pullP = Math.min(p * (25f / 4f), 1.0f);
		return pullP * 4.0f;
	}
}