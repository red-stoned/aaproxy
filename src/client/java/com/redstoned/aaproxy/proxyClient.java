package com.redstoned.aaproxy;

import java.io.File;
import java.io.FileWriter;

import org.lwjgl.glfw.GLFW;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.mojang.serialization.JsonOps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Util;

public class proxyClient implements ClientModInitializer {
	public static final KeyBinding kb = new KeyBinding("aaproxy.reload", GLFW.GLFW_KEY_F7, "key.categories.aaproxy");
	private static File root;
	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(kb);
		root = new File(new File(MinecraftClient.getInstance().runDirectory, "aaproxy"), "advancements");
		root.mkdirs();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (kb.wasPressed()) {
				UpdateAdvancementJson(client);
			}
		});
	}

	public static void UpdateAdvancementJson(MinecraftClient c) {
		Util.getIoWorkerExecutor().execute(() -> {
			File adv = new File(root, c.player.getUuidAsString() + ".json");
			try (FileWriter writer = new FileWriter(adv)) {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonObject adv_root = new JsonObject();
				c.getNetworkHandler().getAdvancementHandler().advancementProgresses.forEach((k, v) -> {
					if (v.getProgressBarPercentage() == 0.0f) return;
					JsonElement je = AdvancementProgress.CODEC.encodeStart(JsonOps.INSTANCE, v).getOrThrow();
					adv_root.add(k.id().toString(), je);
				});
				gson.toJson(adv_root, writer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}