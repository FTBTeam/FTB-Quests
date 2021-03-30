package dev.ftb.mods.ftbquests.quest.loot;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconAnimation;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Bits;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import dev.ftb.mods.ftbquests.gui.EditRewardTableScreen;
import dev.ftb.mods.ftbquests.gui.RewardTablesScreen;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.jei.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.MessageEditObject;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import me.shedaniel.architectury.utils.NbtType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
public final class RewardTable extends QuestObjectBase {
	public final QuestFile file;
	public final List<WeightedReward> rewards;
	public final Quest fakeQuest;
	public int emptyWeight;
	public int lootSize;
	public boolean hideTooltip;
	public boolean useTitle;
	public LootCrate lootCrate;
	public ResourceLocation lootTableId;
	public String filename;

	public RewardTable(QuestFile f) {
		file = f;
		rewards = new ArrayList<>();
		fakeQuest = new Quest(new Chapter(file, f.defaultChapterGroup));
		emptyWeight = 0;
		lootSize = 1;
		hideTooltip = false;
		useTitle = false;
		lootCrate = null;
		lootTableId = null;
		filename = "";
	}

	@Override
	public QuestObjectType getObjectType() {
		return QuestObjectType.REWARD_TABLE;
	}

	@Override
	public QuestFile getQuestFile() {
		return file;
	}

	public int getTotalWeight(boolean includeEmpty) {
		int w = includeEmpty ? emptyWeight : 0;

		for (WeightedReward r : rewards) {
			w += r.weight;
		}

		return w;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);

		if (emptyWeight > 0) {
			nbt.putInt("empty_weight", emptyWeight);
		}

		nbt.putInt("loot_size", lootSize);

		if (hideTooltip) {
			nbt.putBoolean("hide_tooltip", true);
		}

		if (useTitle) {
			nbt.putBoolean("use_title", true);
		}

		ListTag list = new ListTag();

		for (WeightedReward reward : rewards) {
			CompoundTag nbt1 = new CompoundTag();
			reward.reward.writeData(nbt1);

			if (reward.reward.getType() != RewardTypes.ITEM) {
				nbt1.putString("type", reward.reward.getType().getTypeForNBT());
			}

			if (reward.weight > 1) {
				nbt1.putInt("weight", reward.weight);
			}

			list.add(nbt1);
		}

		nbt.put("rewards", list);

		if (lootCrate != null) {
			CompoundTag nbt1 = new CompoundTag();
			lootCrate.writeData(nbt1);
			nbt.put("loot_crate", nbt1);
		}

		if (lootTableId != null) {
			nbt.putString("loot_table_id", lootTableId.toString());
		}
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		emptyWeight = nbt.getInt("empty_weight");
		lootSize = nbt.getInt("loot_size");
		hideTooltip = nbt.getBoolean("hide_tooltip");
		useTitle = nbt.getBoolean("use_title");

		rewards.clear();
		ListTag list = nbt.getList("rewards", NbtType.COMPOUND);

		for (int i = 0; i < list.size(); i++) {
			CompoundTag nbt1 = list.getCompound(i);
			Reward reward = RewardType.createReward(fakeQuest, nbt1.getString("type"));

			if (reward != null) {
				reward.readData(nbt1);
				rewards.add(new WeightedReward(reward, nbt1.getInt("weight")));
			}
		}

		lootCrate = null;

		if (nbt.contains("loot_crate")) {
			lootCrate = new LootCrate(this);
			lootCrate.readData(nbt.getCompound("loot_crate"));
		}

		lootTableId = nbt.contains("loot_table_id") ? new ResourceLocation(nbt.getString("loot_table_id")) : null;
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(filename, Short.MAX_VALUE);
		buffer.writeVarInt(emptyWeight);
		buffer.writeVarInt(lootSize);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, hideTooltip);
		flags = Bits.setFlag(flags, 2, useTitle);
		flags = Bits.setFlag(flags, 4, lootCrate != null);
		flags = Bits.setFlag(flags, 8, lootTableId != null);
		buffer.writeVarInt(flags);
		buffer.writeVarInt(rewards.size());

		for (WeightedReward reward : rewards) {
			buffer.writeVarInt(reward.reward.getType().intId);
			reward.reward.writeNetData(buffer);
			buffer.writeVarInt(reward.weight);
		}

		if (lootCrate != null) {
			lootCrate.writeNetData(buffer);
		}

		if (lootTableId != null) {
			buffer.writeResourceLocation(lootTableId);
		}
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		filename = buffer.readUtf(Short.MAX_VALUE);
		emptyWeight = buffer.readVarInt();
		lootSize = buffer.readVarInt();
		int flags = buffer.readVarInt();
		hideTooltip = Bits.getFlag(flags, 1);
		useTitle = Bits.getFlag(flags, 2);
		boolean hasCrate = Bits.getFlag(flags, 4);
		boolean hasLootTableId = Bits.getFlag(flags, 8);
		rewards.clear();
		int s = buffer.readVarInt();

		for (int i = 0; i < s; i++) {
			RewardType type = file.rewardTypeIds.get(buffer.readVarInt());
			Reward reward = type.provider.create(fakeQuest);
			reward.readNetData(buffer);
			int w = buffer.readVarInt();
			rewards.add(new WeightedReward(reward, w));
		}

		lootCrate = null;

		if (hasCrate) {
			lootCrate = new LootCrate(this);
			lootCrate.readNetData(buffer);
		}

		lootTableId = hasLootTableId ? buffer.readResourceLocation() : null;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addInt("empty_weight", emptyWeight, v -> emptyWeight = v, 0, 0, Integer.MAX_VALUE);
		config.addInt("loot_size", lootSize, v -> lootSize = v, 1, 1, Integer.MAX_VALUE);
		config.addBool("hide_tooltip", hideTooltip, v -> hideTooltip = v, false);
		config.addBool("use_title", useTitle, v -> useTitle = v, false);

		if (lootCrate != null) {
			lootCrate.getConfig(config.getGroup("loot_crate").setNameKey("item.ftbquests.lootcrate.name"));
		}

		// TODO: Implement this: config.addString("loot_table_id", lootTableId == null ? "" : lootTableId.toString(), v -> lootTableId = v.isEmpty() ? null : new ResourceLocation(v), "");
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();

		for (WeightedReward reward : rewards) {
			reward.reward.clearCachedData();
		}
	}

	@Override
	public void deleteSelf() {
		file.rewardTables.remove(this);
		super.deleteSelf();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null && gui.getViewedQuest() != null) {
			gui.viewQuestPanel.refreshWidgets();
		} else {
			RewardTablesScreen gui1 = ClientUtils.getCurrentGuiAs(RewardTablesScreen.class);

			if (gui1 != null) {
				gui1.refreshWidgets();
			}
		}
	}

	@Override
	public void onCreated() {
		if (filename.isEmpty()) {
			String s = titleToID(title).orElse(toString());
			filename = s;

			Set<String> existingNames = file.rewardTables.stream().map(rt -> rt.filename).collect(Collectors.toSet());
			int i = 2;

			while (existingNames.contains(filename)) {
				filename = s + "_" + i;
				i++;
			}
		}

		file.rewardTables.add(this);
	}

	public String getFilename() {
		if (filename.isEmpty()) {
			filename = getCodeString(this);
		}

		return filename;
	}

	@Override
	public String getPath() {
		return "reward_tables/" + getFilename() + ".snbt";
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		if (rewards.size() == 1) {
			return rewards.get(0).reward.getTitle();
		}

		return new TranslatableComponent("ftbquests.reward_table");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		if (lootCrate != null) {
			return ItemIcon.getItemIcon(lootCrate.createStack());
		}

		if (rewards.isEmpty()) {
			return GuiIcons.DICE;
		}

		List<Icon> icons = new ArrayList<>();

		for (WeightedReward reward : rewards) {
			icons.add(reward.reward.getIcon());
		}

		return IconAnimation.fromList(icons, false);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onEditButtonClicked(Runnable gui) {
		new EditRewardTableScreen(this, () -> new MessageEditObject(this).sendToServer()).openGui();
	}

	public void addMouseOverText(TooltipList list, boolean includeWeight, boolean includeEmpty) {
		if (hideTooltip) {
			return;
		}

		int totalWeight = getTotalWeight(includeEmpty);

		if (includeWeight && includeEmpty && emptyWeight > 0) {
			list.add(new TextComponent("").withStyle(ChatFormatting.GRAY).append("- ").append(new TranslatableComponent("ftbquests.reward_table.nothing")).append(new TextComponent(" [" + WeightedReward.chanceString(emptyWeight, totalWeight) + "]").withStyle(ChatFormatting.DARK_GRAY)));
		}

		List<WeightedReward> rewards1;

		if (rewards.size() > 1) {
			rewards1 = new ArrayList<>(rewards);
			rewards1.sort(null);
		} else {
			rewards1 = rewards;
		}

		for (int i = 0; i < rewards1.size(); i++) {
			if (i == 10) {
				list.add(new TextComponent("").withStyle(ChatFormatting.GRAY).append("- ").append(new TranslatableComponent("ftbquests.reward_table.and_more", rewards1.size() - 10)));
				return;
			}

			WeightedReward r = rewards1.get(i);

			if (includeWeight) {
				list.add(new TextComponent("").withStyle(ChatFormatting.GRAY).append("- ").append(r.reward.getTitle()).append(new TextComponent(" [" + WeightedReward.chanceString(r.weight, totalWeight) + "]").withStyle(ChatFormatting.DARK_GRAY)));
			} else {
				list.add(new TextComponent("").withStyle(ChatFormatting.GRAY).append("- ").append(r.reward.getTitle()));
			}
		}
	}

	@Override
	public int refreshJEI() {
		return FTBQuestsJEIHelper.LOOTCRATES;
	}
}