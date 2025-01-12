package com.redstoned.aaproxy.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;

import com.redstoned.aaproxy.AAProxy;

@Environment(EnvType.CLIENT)
@Mixin(ClientAdvancementManager.class)
public class AdvancementManagerMixin {
	@Inject(at = @At("TAIL"), method = "onAdvancements")
	private void advancementChanged(AdvancementUpdateS2CPacket packet, CallbackInfo ci) {
		AAProxy.UpdateAdvancements(MinecraftClient.getInstance());
	}
}
