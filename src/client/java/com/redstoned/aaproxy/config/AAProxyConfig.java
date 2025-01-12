package com.redstoned.aaproxy.config;

import java.io.File;

import com.redstoned.aaproxy.AAProxy;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;

@Config(name = "aaproxy")
public class AAProxyConfig implements ConfigData {
	@ConfigEntry.Gui.Tooltip
	public boolean globalEnabled = true;
	@ConfigEntry.Gui.Tooltip
	public boolean proxySingleplayer = false;
	@ConfigEntry.Gui.Tooltip
	public String roProxyFolder = new File(MinecraftClient.getInstance().runDirectory, "aaproxy").getAbsolutePath();
	
	@ConfigEntry.Gui.Excluded
	private static ConfigHolder<AAProxyConfig> holder;

	public static void registerAndLoad() {
		holder = AutoConfig.register(AAProxyConfig.class, JanksonConfigSerializer::new);
		AAProxy.CONFIG = holder.getConfig();

		holder.registerSaveListener((manager, data) -> {
			// keep folder read only
			if (data.roProxyFolder != AAProxy.root.getParent()) {
				data.roProxyFolder = AAProxy.root.getParent();
			}

			// resave advancements on other changes
			AAProxy.UpdateAdvancements(MinecraftClient.getInstance());
			return ActionResult.SUCCESS;
		});
	}


	public void save() {
		holder.save();
	}
}