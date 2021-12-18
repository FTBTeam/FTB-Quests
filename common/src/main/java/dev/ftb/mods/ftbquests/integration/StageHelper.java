package dev.ftb.mods.ftbquests.integration;

import dev.architectury.platform.Platform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.player.Player;

public abstract class StageHelper {
	public static LazyLoadedValue<StageHelper> instance = new LazyLoadedValue<>(() -> {
		if (Platform.isModLoaded("kubejs")) {
			return new KubeJSStageHelper();
		} else {
			return new EntityTagStageHelper();
		}
	});

	public abstract boolean has(Player player, String stage);

	public abstract void add(ServerPlayer player, String stage);

	public abstract void remove(ServerPlayer player, String stage);
}
