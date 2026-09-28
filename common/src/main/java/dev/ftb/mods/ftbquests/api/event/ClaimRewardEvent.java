package dev.ftb.mods.ftbquests.api.event;

import dev.ftb.mods.ftblibrary.platform.event.TypedEvent;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Predicate;

public abstract class ClaimRewardEvent {
    /// Fired when a reward is about to be claimed via the quest book GUI. When this event is fired, it is confirmed
    /// that the reward is ready to be claimed, and not already claimed. The event can be canceled, which also
    /// prevents it being marked completed in the team's progression data.
    @FunctionalInterface
    public interface Pre extends Predicate<Pre.Data> {
        TypedEvent<Pre.Data, Boolean> TYPE = TypedEvent.ofBoolean(Pre.Data.class);

        /// @param reward the reward about to be claimed
        /// @param player the player receiving the award
        record Data(Reward reward, ServerPlayer player) {
        }
    }

    /// Fired when an item reward is about to award the item to the player, after [ClaimRewardEvent.Pre].
    /// The event can be canceled, which also prevents it being marked completed in the team's progression data.
    @FunctionalInterface
    public interface GrantItem extends Predicate<GrantItem.Data> {
        TypedEvent<GrantItem.Data, Boolean> TYPE = TypedEvent.ofBoolean(GrantItem.Data.class);

        /// @param player the player receiving the award, may be null if this is received via a Loot Crate Opener and the owning player is offline
        /// @param playerId the player's UUID
        /// @param level the player or Loot Crate Opener's level
        /// @param opener the Loot Crate Opener or other block entity automating the process; null if this crate is being opened manually
        /// @param stack the item being given to the player; note that the stack size may be greater than the item's usual max stack size
        record Data(@Nullable ServerPlayer player, UUID playerId, ServerLevel level, @Nullable BlockEntity opener, ItemStack stack) {
        }
    }
}
