package dev.ftb.mods.ftbquests.quest.reward;

import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.net.NotifyItemRewardMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.registry.ModItems;
import io.netty.handler.codec.EncoderException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ItemReward extends Reward {
	private ItemStack item;
	private int count;
	private int randomBonus;
	private boolean onlyOne;

	public ItemReward(long id, Quest quest, ItemStack is) {
		this(id, quest, is, 1);
	}

	public ItemReward(long id, Quest quest, ItemStack is, int count) {
		super(id, quest);
		item = is;
		this.count = count;
		randomBonus = 0;
		onlyOne = false;
	}

	public ItemReward(long id, Quest quest) {
		this(id, quest, new ItemStack(Items.APPLE));
	}

	public ItemStack getItem() {
		return item;
	}

	public int getCount() {
		return count;
	}

	@Override
	public RewardType getType() {
		return RewardTypes.ITEM;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);

		if (!item.isEmpty()) {
			nbt.store("item", ItemStack.CODEC, item);
		}

		if (count > 1) {
			nbt.putInt("count", count);
		}
		if (randomBonus > 0) {
			nbt.putInt("random_bonus", randomBonus);
		}
		if (onlyOne) {
			nbt.putBoolean("only_one", true);
		}
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);

		item = itemOrMissingFromNBT(nbt.get("item"), provider);

		count = nbt.getIntOr("count", 1);
		if (count == 0) {
			count = item.getCount();
			item.setCount(1);
		}

		randomBonus = nbt.getIntOr("random_bonus", 0);
		onlyOne = nbt.getBooleanOr("only_one", false);
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);

		try {
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, item);
		} catch (EncoderException e) {
			FTBQuests.LOGGER.error("Caught EncoderException while encoding item for client sync! {}", e.getMessage());
			FTBQuests.LOGGER.error("- Item:");
			FTBQuests.LOGGER.error(item);
			FTBQuests.LOGGER.error("- Item components:");
			FTBQuests.LOGGER.error(item.getComponents());
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, ModItems.MISSING_ITEM.get().getDefaultInstance());
		}
		buffer.writeVarInt(count);
		buffer.writeVarInt(randomBonus);
		buffer.writeBoolean(onlyOne);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);

		item = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
		count = buffer.readVarInt();
		randomBonus = buffer.readVarInt();
		onlyOne = buffer.readBoolean();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		config.addItemStack("item", item, v -> item = v, ItemStack.EMPTY, true, false).setNameKey("ftbquests.reward.ftbquests.item");
		config.addInt("count", count, v -> count = v, 1, 1, 8192);
		config.addInt("random_bonus", randomBonus, v -> randomBonus = v, 0, 0, 8192).setNameKey("ftbquests.reward.random_bonus");
		config.addBool("only_one", onlyOne, v -> onlyOne = v, false);
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		if (onlyOne && player.getInventory().contains(item)) {
			return;
		}

		int size = count + player.level().random.nextInt(randomBonus + 1);
		while (size > 0) {
			int s = Math.min(size, item.getMaxStackSize());
			ItemStackHooks.giveItem(player, ItemStackHooks.copyWithCount(item, s));
			size -= s;
		}

		if (notify) {
			NetworkManager.sendToPlayer(player, new NotifyItemRewardMessage(item, size, disableRewardScreenBlur));
		}
	}

	@Override
	public boolean automatedClaimPre(BlockEntity blockEntity, List<ItemStack> items, RandomSource random, UUID playerId, @Nullable ServerPlayer player) {
		int size = count + random.nextInt(randomBonus + 1);

		while (size > 0) {
			int s = Math.min(size, item.getMaxStackSize());
			items.add(ItemStackHooks.copyWithCount(item, s));
			size -= s;
		}

		return true;
	}

	@Override
	public void automatedClaimPost(BlockEntity blockEntity, UUID playerId, @Nullable ServerPlayer player) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		if (randomBonus > 0) {
			return Component.literal(count + "-" + (count + randomBonus) + "x ").append(item.getHoverName());
		} else if (count > 1) {
			return Component.literal(count + "x ").append(item.getHoverName());
		} else {
			return item.getHoverName().copy();
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		return item.isEmpty() ? super.getAltIcon() : ItemIcon.getItemIcon(ItemStackHooks.copyWithCount(item, 1));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Optional<PositionedIngredient> getIngredient(Widget widget) {
		return PositionedIngredient.of(item, widget, true);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public String getButtonText() {
		return randomBonus > 0 ? count + "-" + (count + randomBonus) : Integer.toString(count);
	}
}
