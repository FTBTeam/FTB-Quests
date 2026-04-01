package dev.ftb.mods.ftbquests.quest.reward;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.config.EditableQuestObject;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RandomReward extends Reward {
	@Nullable
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
	public void writeData(Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);

		if (getTable() != null) {
			json.addProperty("table_id", table.id);
			if (table.id == -1L) {
				json.add("table_data", Util.make(new Json5Object(), o -> table.writeData(o, provider)));
			}
		}
	}

	@Override
	public void readData(Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);
		table = null;
		BaseQuestFile file = getQuestFile();

		Json5Util.getLong(json, "table_id").ifPresent(tableId -> {
			if (tableId != 0L) {
				table = file.getRewardTable(tableId);
			}

			if (table == null) return;
			Json5Util.getJson5Object(json, "table_data").ifPresent(tag -> {
				table.readData(tag, provider);
				table.setRawTitle("Internal");
			});
		});
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
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);
		config.add("table", new EditableQuestObject<>(QuestObjectType.REWARD_TABLE), table, v -> table = v, getTable()).setNameKey("ftbquests.reward_table");
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
	public Component getAltTitle() {
		return getTable() == null ? super.getAltTitle() : getTable().getTitleOrElse(super.getAltTitle());
	}

	@Override
	public Icon<?> getAltIcon() {
		return getTable() == null ? super.getAltIcon() : getTable().getIcon();
	}

	@Override
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
