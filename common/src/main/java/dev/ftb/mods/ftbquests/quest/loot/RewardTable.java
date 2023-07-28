package dev.ftb.mods.ftbquests.quest.loot;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.*;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.EditRewardTableScreen;
import dev.ftb.mods.ftbquests.client.gui.RewardTablesScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class RewardTable extends QuestObjectBase {
	private final QuestFile file;
	private final List<WeightedReward> weightedRewards;
	private final Quest fakeQuest;
	private float emptyWeight;
	private int lootSize;
	private boolean hideTooltip;
	private boolean useTitle;
	private LootCrate lootCrate;
	private ResourceLocation lootTableId;
	private String filename;

	public RewardTable(long id, QuestFile file) {
		this(id, file, "");
	}

	public RewardTable(long id, QuestFile file, String filename) {
		super(id);

		this.file = file;
		this.filename = filename;

		weightedRewards = new ArrayList<>();
		fakeQuest = new Quest(-1L, new Chapter(-1L, this.file, file.getDefaultChapterGroup()));
		emptyWeight = 0f;
		lootSize = 1;
		hideTooltip = false;
		useTitle = false;
		lootCrate = null;
		lootTableId = null;
	}

	public Component getTitleOrElse(Component def) {
		return useTitle ? getTitle() : def;
	}

	public QuestFile getFile() {
		return file;
	}

	public List<WeightedReward> getWeightedRewards() {
		return Collections.unmodifiableList(weightedRewards);
	}

	@Nullable
	public LootCrate getLootCrate() {
		return lootCrate;
	}

	public Quest getFakeQuest() {
		return fakeQuest;
	}

	@Override
	public QuestObjectType getObjectType() {
		return QuestObjectType.REWARD_TABLE;
	}

	@Override
	public QuestFile getQuestFile() {
		return file;
	}

	public float getTotalWeight(boolean includeEmpty) {
		float initial = includeEmpty ? emptyWeight : 0f;
		return weightedRewards.stream().map(WeightedReward::getWeight).reduce(initial, Float::sum);
	}

	public Collection<WeightedReward> generateWeightedRandomRewards(RandomSource random, int nAttempts, boolean includeEmpty) {
		float total = getTotalWeight(includeEmpty);
		if (total <= 0f) return List.of();

		// rewards with a weight of 0 are auto-granted
		List<WeightedReward> res  = weightedRewards.stream()
				.filter(reward -> reward.getWeight() == 0f)
				.collect(Collectors.toCollection(ArrayList::new));

		nAttempts *= lootSize;

		for (int i = 0; i < nAttempts; i++) {
			float threshold = random.nextFloat() * total;
			float currentWeight = includeEmpty ? emptyWeight : 0f;

			if (currentWeight < threshold) {
				for (WeightedReward reward : weightedRewards) {
					currentWeight += reward.getWeight();
					if (currentWeight >= threshold) {
						res.add(reward);
						break;
					}
				}
			}
		}
		return res;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);

		if (emptyWeight > 0f) {
			nbt.putFloat("empty_weight", emptyWeight);
		}

		nbt.putInt("loot_size", lootSize);

		if (hideTooltip) {
			nbt.putBoolean("hide_tooltip", true);
		}

		if (useTitle) {
			nbt.putBoolean("use_title", true);
		}

		ListTag list = new ListTag();

		for (WeightedReward wr : weightedRewards) {
			SNBTCompoundTag nbt1 = new SNBTCompoundTag();
			wr.getReward().writeData(nbt1);

			if (wr.getReward().getType() != RewardTypes.ITEM) {
				nbt1.putString("type", wr.getReward().getType().getTypeForNBT());
			} else if (nbt1.getTagType("item") == Tag.TAG_STRING) {
				nbt1.singleLine();
			}

			if (wr.getWeight() != 1f) {
				nbt1.putFloat("weight", wr.getWeight());
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
		emptyWeight = nbt.getFloat("empty_weight");
		lootSize = nbt.getInt("loot_size");
		hideTooltip = nbt.getBoolean("hide_tooltip");
		useTitle = nbt.getBoolean("use_title");

		weightedRewards.clear();
		ListTag list = nbt.getList("rewards", Tag.TAG_COMPOUND);

		for (int i = 0; i < list.size(); i++) {
			CompoundTag nbt1 = list.getCompound(i);
			Reward reward = RewardType.createReward(0L, fakeQuest, nbt1.getString("type"));

			if (reward != null) {
				reward.readData(nbt1);
				weightedRewards.add(new WeightedReward(reward, nbt1.contains("weight") ? nbt1.getFloat("weight") : 1));
			}
		}

		lootCrate = null;

		if (nbt.contains("loot_crate")) {
			lootCrate = new LootCrate(this, false);
			lootCrate.readData(nbt.getCompound("loot_crate"));
		}

		lootTableId = nbt.contains("loot_table_id") ? new ResourceLocation(nbt.getString("loot_table_id")) : null;
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(filename, Short.MAX_VALUE);
		buffer.writeFloat(emptyWeight);
		buffer.writeVarInt(lootSize);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, hideTooltip);
		flags = Bits.setFlag(flags, 2, useTitle);
		flags = Bits.setFlag(flags, 4, lootCrate != null);
		flags = Bits.setFlag(flags, 8, lootTableId != null);
		buffer.writeVarInt(flags);
		buffer.writeVarInt(weightedRewards.size());

		for (WeightedReward wr : weightedRewards) {
			buffer.writeVarInt(wr.getReward().getType().intId);
			wr.getReward().writeNetData(buffer);
			buffer.writeFloat(wr.getWeight());
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
		emptyWeight = buffer.readFloat();
		lootSize = buffer.readVarInt();
		int flags = buffer.readVarInt();
		hideTooltip = Bits.getFlag(flags, 1);
		useTitle = Bits.getFlag(flags, 2);
		boolean hasCrate = Bits.getFlag(flags, 4);
		boolean hasLootTableId = Bits.getFlag(flags, 8);
		weightedRewards.clear();
		int s = buffer.readVarInt();

		for (int i = 0; i < s; i++) {
			RewardType type = file.getRewardType(buffer.readVarInt());
			Reward reward = type.createReward(0L, fakeQuest);
			reward.readNetData(buffer);
			float weight = buffer.readFloat();
			weightedRewards.add(new WeightedReward(reward, weight));
		}

		lootCrate = null;

		if (hasCrate) {
			lootCrate = new LootCrate(this, false);
			lootCrate.readNetData(buffer);
		}

		lootTableId = hasLootTableId ? buffer.readResourceLocation() : null;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addDouble("empty_weight", emptyWeight, v -> emptyWeight = v.floatValue(), 0, 0, Integer.MAX_VALUE);
		config.addInt("loot_size", lootSize, v -> lootSize = v, 1, 1, Integer.MAX_VALUE);
		config.addBool("hide_tooltip", hideTooltip, v -> hideTooltip = v, false);
		config.addBool("use_title", useTitle, v -> useTitle = v, false);

		if (lootCrate != null) {
			lootCrate.fillConfigGroup(config.getOrCreateSubgroup("loot_crate").setNameKey("item.ftbquests.lootcrate"));
		}

		// TODO: Implement this: config.addString("loot_table_id", lootTableId == null ? "" : lootTableId.toString(), v -> lootTableId = v.isEmpty() ? null : new ResourceLocation(v), "");
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();

		weightedRewards.forEach(reward -> reward.getReward().clearCachedData());
	}

	@Override
	public void deleteSelf() {
		file.removeRewardTable(this);
		super.deleteSelf();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null && gui.isViewingQuest()) {
			gui.refreshViewQuestPanel();
		} else {
			RewardTablesScreen gui1 = ClientUtils.getCurrentGuiAs(RewardTablesScreen.class);
			if (gui1 != null) {
				gui1.refreshWidgets();
			}
		}

		file.updateLootCrates();
	}

	@Override
	public void editedFromGUIOnServer() {
		file.updateLootCrates();
	}

	@Override
	public void onCreated() {
		if (filename.isEmpty()) {
			filename = file.generateRewardTableName(titleToID(rawTitle).orElse(toString()));
		}

		file.addRewardTable(this);
	}

	public String getFilename() {
		if (filename.isEmpty()) {
			filename = getCodeString(this);
		}

		return filename;
	}

	@Override
	public Optional<String> getPath() {
		return Optional.of("reward_tables/" + getFilename() + ".snbt");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		return weightedRewards.size() == 1 ?
				weightedRewards.get(0).getReward().getTitle() :
				Component.translatable("ftbquests.reward_table");

	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		if (lootCrate != null) {
			return ItemIcon.getItemIcon(lootCrate.createStack());
		}

		if (weightedRewards.isEmpty()) {
			return Icons.DICE;
		}

		List<Icon> icons = weightedRewards.stream().map(reward -> reward.getReward().getIcon()).collect(Collectors.toList());
		return IconAnimation.fromList(icons, false);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onEditButtonClicked(Runnable gui) {
		new EditRewardTableScreen(this, () -> {
			new EditObjectMessage(this).sendToServer();
			clearCachedData();
		}).openGui();
	}

	public void addMouseOverText(TooltipList list, boolean includeWeight, boolean includeEmpty) {
		if (!hideTooltip) {
			float totalWeight = getTotalWeight(includeEmpty);

			if (includeWeight && includeEmpty && emptyWeight > 0) {
				addItem(list, Component.translatable("ftbquests.reward_table.nothing"), emptyWeight, totalWeight);
			}

			List<WeightedReward> sortedRewards = weightedRewards.stream().sorted().toList();

			BaseScreen gui = ClientUtils.getCurrentGuiAs(BaseScreen.class);
			int maxLines = gui == null ? 12 : (gui.height - 20) / (gui.getTheme().getFontHeight() + 2);
			int nRewards = sortedRewards.size();
			int start = nRewards > maxLines ?
					(int) ((FTBQuestsClient.getClientLevel().getGameTime() / 10) % nRewards) :
					0;

			int nLines = Math.min(maxLines, nRewards);
			for (int idx = 0; idx < nLines; idx++) {
				WeightedReward wr = sortedRewards.get((start + idx) % nRewards);
				if (includeWeight) {
					addItem(list, wr.getReward().getTitle(), wr.getWeight(), totalWeight);
				} else {
					list.add(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(wr.getReward().getTitle()));
				}
			}
		}
	}

	private static void addItem(TooltipList list, Component text, float weight, float totalWeight) {
		list.add(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(text)
				.append(Component.literal(" [" + WeightedReward.chanceString(weight, totalWeight) + "]").withStyle(ChatFormatting.DARK_GRAY)));
	}

	@Override
	public Set<RecipeModHelper.Components> componentsToRefresh() {
		return EnumSet.of(RecipeModHelper.Components.LOOT_CRATES);
	}

	public void addReward(WeightedReward weightedReward) {
		weightedRewards.add(weightedReward);
	}

	public void removeReward(WeightedReward weightedReward) {
		weightedRewards.remove(weightedReward);
	}

	public WeightedReward makeWeightedItemReward(ItemStack stack, float weight) {
		return new WeightedReward(new ItemReward(0L, fakeQuest, stack), weight);
	}

	public LootCrate toggleLootCrate() {
		if (lootCrate == null) {
			lootCrate = new LootCrate(this, true);
		} else {
			lootCrate = null;
		}
		return lootCrate;
	}
}
