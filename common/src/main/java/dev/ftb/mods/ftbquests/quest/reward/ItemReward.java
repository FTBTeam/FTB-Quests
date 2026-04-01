package dev.ftb.mods.ftbquests.quest.reward;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.net.NotifyItemRewardMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.registry.ModItems;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

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
	public void writeData(@UnknownNullability Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);

		if (!item.isEmpty()) Json5Util.store(json, "item", ItemStack.CODEC, item);
		if (count > 1) json.addProperty("count", count);
		if (randomBonus > 0) json.addProperty("random_bonus", randomBonus);
		if (onlyOne) json.addProperty("only_one", true);
	}

	@Override
	public void readData(@UnknownNullability Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);

		item = itemOrMissingFromJson(Json5Util.getJson5Object(json, "item").orElse(new Json5Object()), provider);
		count = Json5Util.getInt(json,"count").orElse(1);
		if (count == 0) {
			count = item.getCount();
			item.setCount(1);
		}
		randomBonus = Json5Util.getInt(json, "random_bonus").orElse(0);
		onlyOne = Json5Util.getBoolean(json, "only_one").orElse(false);
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
	public void fillConfigGroup(EditableConfigGroup config) {
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

		int size = count + player.level().getRandom().nextInt(randomBonus + 1);
		while (size > 0) {
			int s = Math.min(size, item.getMaxStackSize());
			player.getInventory().placeItemBackInInventory(item.copyWithCount(s));
			size -= s;
		}

		if (notify) {
			Server2PlayNetworking.send(player, new NotifyItemRewardMessage(item, size, disableRewardScreenBlur));
		}
	}

	@Override
	public boolean automatedClaimPre(BlockEntity blockEntity, List<ItemStack> items, RandomSource random, UUID playerId, @Nullable ServerPlayer player) {
		int size = count + random.nextInt(randomBonus + 1);

		while (size > 0) {
			int s = Math.min(size, item.getMaxStackSize());
			items.add(item.copyWithCount(s));
			size -= s;
		}

		return true;
	}

	@Override
	public void automatedClaimPost(BlockEntity blockEntity, UUID playerId, @Nullable ServerPlayer player) {
	}

	@Override
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
	public Icon<?> getAltIcon() {
		return item.isEmpty() ? super.getAltIcon() : ItemIcon.ofItemStack(item.copyWithCount(1));
	}

	@Override
	public Optional<PositionedIngredient> getIngredient(Widget widget) {
		return PositionedIngredient.of(item, widget, true);
	}

	@Override
	public String getButtonText() {
		return randomBonus > 0 ? count + "-" + (count + randomBonus) : Integer.toString(count);
	}
}
