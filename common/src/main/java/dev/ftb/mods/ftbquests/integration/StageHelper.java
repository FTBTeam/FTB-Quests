package dev.ftb.mods.ftbquests.integration;

import com.google.common.base.Suppliers;
import dev.architectury.platform.Platform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public abstract class StageHelper {
	public static Supplier<? extends StageHelper> instance = Suppliers.memoize(() -> Platform.isModLoaded("kubejs") ? new KubeJSStageHelper() : new EntityTagStageHelper());

	public abstract boolean has(Player player, String stage);

	public abstract void add(ServerPlayer player, String stage);

	public abstract void remove(ServerPlayer player, String stage);
}
