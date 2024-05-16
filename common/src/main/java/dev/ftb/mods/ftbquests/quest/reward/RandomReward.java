package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RandomReward extends Reward {
	private RewardTable table;

	public RandomReward(long id, Quest parent) {
		super(id, parent);
		table = null;
	}

	@Override
	public RewardType getType() {
		return RewardTypes.RANDOM;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);

		if (getTable() != null) {
			nbt.putLong("table_id", table.id);

			if (table.id == -1L) {
				SNBTCompoundTag tag = new SNBTCompoundTag();
				table.writeData(tag, provider);
				nbt.put("table_data", tag);
			}
		}
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		table = null;
		BaseQuestFile file = getQuestFile();

		long id = nbt.getLong("table_id");
		if (id != 0L) {
			table = file.getRewardTable(id);
		}

		if (table == null && nbt.contains("table_data")) {
			table = new RewardTable(-1L, file);
			table.readData(nbt.getCompound("table_data"), provider);
			table.setRawTitle("Internal");
		}
	}

	@Nullable
	public RewardTable getTable() {
		if (table != null && !table.isValid()) {
			table = null;
		}

		return table;
	}

	public void setTable(RewardTable table) {
		this.table = table;
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);

		RewardTable table = getTable();
		buffer.writeLong(table == null ? 0L : table.id);

		if (table != null && table.id == -1L) {
			table.writeNetData(buffer);
		}
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		BaseQuestFile file = getQuestFile();

		long t = buffer.readLong();

		if (t == -1L) {
			table = new RewardTable(-1L, file);
			table.readNetData(buffer);
			table.setRawTitle("Internal");
		} else {
			table = file.getRewardTable(t);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.add("table", new ConfigQuestObject<>(QuestObjectType.REWARD_TABLE), table, v -> table = v, getTable()).setNameKey("ftbquests.reward_table");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		RewardTable table = getTable();

		if (table != null) {
			for (WeightedReward wr : table.generateWeightedRandomRewards(player.getRandom(), 1, false)) {
				wr.getReward().claim(player, notify);
			}
		}

	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		return getTable() == null ? super.getAltTitle() : getTable().getTitleOrElse(super.getAltTitle());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		return getTable() == null ? super.getAltIcon() : getTable().getIcon();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void addMouseOverText(TooltipList list) {
		if (getTable() != null) {
			getTable().addMouseOverText(list, true, false);
		}
	}

	@Override
	public boolean getExcludeFromClaimAll() {
		return false;
	}

	@Override
	public boolean isClaimAllHardcoded() {
		return true;
	}

	@Override
    @Environment(EnvType.CLIENT)
	public Optional<PositionedIngredient> getIngredient(Widget widget) {
		return getTable() != null && getTable().getLootCrate() != null ?
				PositionedIngredient.of(getTable().getLootCrate().createStack(), widget) :
				Optional.empty();
	}

	@Override
	public boolean automatedClaimPre(BlockEntity blockEntity, List<ItemStack> items, RandomSource random, UUID playerId, @Nullable ServerPlayer player) {
		return false;
	}

	@Override
	public void automatedClaimPost(BlockEntity blockEntity, UUID playerId, @Nullable ServerPlayer player) {
	}
}