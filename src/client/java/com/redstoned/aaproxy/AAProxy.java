package com.redstoned.aaproxy;

import java.io.File;
import java.io.FileWriter;

import org.lwjgl.glfw.GLFW;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.mojang.serialization.JsonOps;
import com.redstoned.aaproxy.config.AAProxyConfig;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Util;

public class AAProxy implements ClientModInitializer {
	private static final KeyBinding reload_kb = new KeyBinding("key.aaproxy.reload", GLFW.GLFW_KEY_UNKNOWN, "key.categories.aaproxy");
	private static final KeyBinding toggle_kb = new KeyBinding("key.aaproxy.toggle", GLFW.GLFW_KEY_F4, "key.categories.aaproxy");
	public static File root = new File(new File(MinecraftClient.getInstance().runDirectory, "aaproxy"), "advancements");;
	public static AAProxyConfig CONFIG = new AAProxyConfig();

	@Override
	public void onInitializeClient() {
		AAProxyConfig.registerAndLoad();

		KeyBindingHelper.registerKeyBinding(reload_kb);
		KeyBindingHelper.registerKeyBinding(toggle_kb);
		
		root.mkdirs();
		if (CONFIG.roProxyFolder != root.getParent()) {
			CONFIG.roProxyFolder = root.getParent();
			CONFIG.save();
		}

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (IsEnabled(client)) {
				client.player.sendMessage(enabledMsg(), false);
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// force reload, skips checks
			if (reload_kb.wasPressed()) {
				SerialiseAdvancements(client);
			}

			if (toggle_kb.wasPressed()) {
				CONFIG.globalEnabled = !CONFIG.globalEnabled;
				CONFIG.save();

				if (CONFIG.globalEnabled) {
					SerialiseAdvancements(client);
					client.player.sendMessage(enabledMsg(), false);
				} else {
					client.player.sendMessage(msg("msg.aaproxy.disabled"), false);
				}
			}
		});
	}

	private static void SerialiseAdvancements(MinecraftClient c) {
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

	public static boolean IsEnabled(MinecraftClient c) {
		return AAProxy.CONFIG.globalEnabled && (AAProxy.CONFIG.proxySingleplayer || !c.isInSingleplayer());
	}

	public static void UpdateAdvancements(MinecraftClient c) {
		if (c.player == null || !IsEnabled(c)) return;
		SerialiseAdvancements(c);
	}

	public static Text msg(String key, Object... args) {
		return Text.translatable(key, args)
			.setStyle(Style.EMPTY.withColor(0x00cc99));
	}

	public static final Text enabledMsg() {
		MutableText m1 = Text.literal("folder");
		m1.setStyle(
			Style.EMPTY
			.withClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, root.getParent()))
			.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy path")))
			.withUnderline(true)
		);
		return msg("msg.aaproxy.enabled", m1);
	}
}