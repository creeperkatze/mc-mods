package dev.creeperkatze.exchange;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public enum LodMod {
	DISTANT_HORIZONS("distanthorizons", "distant_horizons"),
	VOXY("voxy", "voxy");

	private final String modId;
	private final String key;

	LodMod(String modId, String key) {
		this.modId = modId;
		this.key = key;
	}

	public String key() {
		return this.key;
	}

	public boolean isLoaded() {
		return FabricLoader.getInstance().isModLoaded(this.modId);
	}

	public LodMod other() {
		return this == VOXY ? DISTANT_HORIZONS : VOXY;
	}

	public Component displayName() {
		return Component.translatable("exchange.mod." + this.key);
	}

	public static @Nullable LodMod byKey(String key) {
		for (LodMod mod : values()) {
			if (mod.key.equals(key)) {
				return mod;
			}
		}
		return null;
	}
}
