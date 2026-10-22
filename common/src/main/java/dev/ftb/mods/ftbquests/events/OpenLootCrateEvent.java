package dev.ftb.mods.ftbquests.events;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

@FunctionalInterface
public interface OpenLootCrateEvent {
    Event<OpenLootCrateEvent> EVENT = EventFactory.createEventResult();

    /**
     * Fired when a player or Loot Crate Opener is about to open a loot crate. This is fired before
     * {@link ClaimRewardEvent.GrantItem#EVENT}. This is only fired server-side by the Loot Crate Opener, but is fired
     * on both client and server when a player manually opens a crate.
     * <p>
     * If canceled, the loot crate is not opened by the player, and rejected for insertion by a Loot Crate Opener.
     *
     * @param player   the player opening the loot crate; may be null if opened by a Loot Crate Opener and the owning player is offline
     * @param playerId the player's UUID
     * @param level    the player or Loot Crate Opener's level
     * @param crate    the loot crate about to be opened
     * @param opener   the Loot Crate Opener or other block entity automating the process; null if this crate is being opened manually
     * @param simulate true if this is a simulated opening by a Loot Crate Opener; always false if being opened manually
     * @return an event result indicating if the crate should be opened, with an optional failure reason to be displayed to the player
     */
    CompoundEventResult<Optional<Component>> onOpenLootCrate(@Nullable Player player, UUID playerId, Level level, LootCrate crate, @Nullable BlockEntity opener, boolean simulate);

    static CompoundEventResult<Optional<Component>> fail() {
        return CompoundEventResult.interruptFalse(Optional.empty());
    }

    static CompoundEventResult<Optional<Component>> fail(Component reason) {
        return CompoundEventResult.interruptFalse(Optional.of(reason));
    }
}
