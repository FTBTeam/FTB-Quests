package dev.ftb.mods.ftbquests.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class ClaimRewardEvent {
    @FunctionalInterface
    public interface Pre {
        Event<Pre> EVENT = EventFactory.createEventResult();

        /**
         * Fired server-side when a reward is about to be claimed via the quest book GUI. When this event is fired,
         * it has been confirmed that the reward is ready to be claimed, and has not already been claimed.
         * <p>
         * This event can be canceled, which also prevents the reward being marked as completed in the team's
         * progression data.
         *
         * @param reward the reward about to be claimed
         * @param player the player receiving the award
         */
        EventResult onClaim(Reward reward, ServerPlayer player);
    }

    @FunctionalInterface
    public interface GrantItem {
        Event<GrantItem> EVENT = EventFactory.createEventResult();

        /**
         * Fired server-side when an item reward is about to award the item to the player, after
         * {@link ClaimRewardEvent.Pre#EVENT}. This event is fired for both claiming rewards via the quest book GUI,
         * and when a Loot Crate Opener has opened a crate to add the item to its internal store.
         * <p>
         * This event can be canceled, which also prevents the reward being marked as completed in the team's
         * progression data.
         *
         * @param player the player receiving the award, may be null if this is received via a Loot Crate Opener and the owning player is offline
         * @param playerId the player's UUID
         * @param level the player or Loot Crate Opener's level
         * @param opener the Loot Crate Opener or other block entity automating the process; null if this crate is being opened manually
         * @param stack the item being given to the player; note that the stack size may be greater than the item's usual max stack size
         * @return an event result indicating if this item should be given
         */
        EventResult onClaimItem(@Nullable ServerPlayer player, UUID playerId, ServerLevel level, @Nullable BlockEntity opener, ItemStack stack);
    }
}
