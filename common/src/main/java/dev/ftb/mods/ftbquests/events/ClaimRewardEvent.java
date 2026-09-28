package dev.ftb.mods.ftbquests.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbquests.block.entity.LootCrateOpenerBlockEntity;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class ClaimRewardEvent {
    @FunctionalInterface
    public interface Pre {
        Event<Pre> EVENT = EventFactory.createEventResult();

        /**
         * Fired when a reward is about to be claimed via the quest book GUI. When this event is fired, it is confirmed
         * that the reward is ready to be claimed, and not already claimed. The event can be canceled, which also
         * prevents it being marked completed in the team's progression data.
         *
         * @param reward the reward about to be claimed
         * @param player the player receiving the award
         * @param blockEntity the loot crate opener if any; null if the player is receiving a reward via the quest book GUI
         */
        EventResult onClaim(Reward reward, ServerPlayer player, @Nullable LootCrateOpenerBlockEntity blockEntity);
    }

    @FunctionalInterface
    public interface GrantItem {
        Event<GrantItem> EVENT = EventFactory.createEventResult();

        /**
         * Fired when an item reward is about to award the item to the player, after {@link ClaimRewardEvent.Pre#EVENT}.
         * The event can be canceled, which also prevents it being marked completed in the team's progression data.
         *
         * @param player the player receiving the award, may be null if this is received via a Loot Crate Opener and the owning player of the block is offline
         * @param playerId the player's UUID
         * @param stack the item being given to the player; note that the size may be greater than the item's usual max stack size
         * @return an event result indicating if this item should be given
         */
        EventResult onClaimItem(@Nullable ServerPlayer player, UUID playerId, ItemStack stack);
    }
}
