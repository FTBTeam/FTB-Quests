package dev.ftb.mods.ftbquests.api.event;

import dev.ftb.mods.ftblibrary.platform.event.TypedEvent;
import dev.ftb.mods.ftblibrary.util.result.DataOutcome;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;

/// Fired when a player or Loot Crate Opener is about to open a loot crate. This is fired before
/// [ClaimRewardEvent.GrantItem]. This is only fired server-side by the Loot Crate Opener, but is fired
/// on both client and server when a player manually opens a crate.
///
/// If canceled, the loot crate is not opened by the player, and rejected for insertion by a Loot Crate Opener.
///
@FunctionalInterface
public interface OpenLootCrateEvent extends Function<OpenLootCrateEvent.Data, DataOutcome<Component>> {
    TypedEvent<OpenLootCrateEvent.Data, DataOutcome<Component>> TYPE = TypedEvent.of(OpenLootCrateEvent.Data.class);

    /// @param player   the player opening the loot crate; may be null if opened by a Loot Crate Opener and the owning player is offline
    /// @param playerId the player's UUID ({@code Util.NIL_UUID} if this is a Loot Crate Opener missing the owner ID, i.e. not placed by a player)
    /// @param level    the player or Loot Crate Opener's level
    /// @param crate    the loot crate about to be opened
    /// @param opener   the Loot Crate Opener or other block entity automating the process; null if this crate is being opened manually
    /// @param simulate true if this is a simulated opening by a Loot Crate Opener; always false if being opened manually
    record Data(@Nullable Player player, UUID playerId, Level level, LootCrate crate, @Nullable BlockEntity opener, boolean simulate) {
    }
}
