package dev.ftb.mods.ftbquests.integration;

import com.google.common.base.Suppliers;
import dev.architectury.platform.Platform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public abstract class StageHelper {
	public static final Supplier<StageHelper> INSTANCE = Suppliers.memoize(() -> {
				if (Platform.isModLoaded("kubejs")) {
					return new KubeJSStageHelper();
				} else if (Platform.isModLoaded("gamestages")) {
					return new GameStagesStageHelper();
				} else {
					return new EntityTagStageHelper();
				}
			}
	);

	public abstract boolean has(Player player, String stage);

	public abstract void add(ServerPlayer player, String stage);

	public abstract void remove(ServerPlayer player, String stage);
}
