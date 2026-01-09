package dev.ftb.mods.ftbquests.quest.loot;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.AnimatedIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.EditRewardTableScreen;
import dev.ftb.mods.ftbquests.client.gui.RewardTablesScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.net.CreateObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.net.SyncTranslationMessageToClient;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RewardTable extends QuestObjectBase {
	private final BaseQuestFile file;
	private final List<WeightedReward> weightedRewards;
	private final Quest fakeQuest;
	private float emptyWeight;
	private int lootSize;
	private boolean hideTooltip;
	private boolean useTitle;
	private LootCrate lootCrate;
	private Identifier lootTableId;
	private String filename;

	public RewardTable(long id, BaseQuestFile file) {
		this(id, file, "");
	}

	public RewardTable(long id, BaseQuestFile file, String filename) {
		super(id);

		this.file = file;
		this.filename = filename;

		weightedRewards = new ArrayList<>();
		fakeQuest = makeFakeQuest(file);
		emptyWeight = 0f;
		lootSize = 1;
		hideTooltip = false;
		useTitle = false;
		lootCrate = null;
		lootTableId = null;
	}

	private static Quest makeFakeQuest(BaseQuestFile file) {
		return new Quest(-1L, new Chapter(-1L, file, file.getDefaultChapterGroup()));
	}

	public static boolean isFakeQuestId(long id) {
		return id == -1L;
	}

	@NotNull
	public static QuestObjectBase createRewardForTable(long id, String type, BaseQuestFile file) {
		Reward reward = RewardType.createReward(id, makeFakeQuest(file), type);
		Validate.isTrue(reward != null, "Unknown reward type!");
		return reward;
	}

	public Component getTitleOrElse(Component def) {
		return useTitle ? getTitle() : def;
	}

	public BaseQuestFile getFile() {
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
	public BaseQuestFile getQuestFile() {
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
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);

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
			SNBTCompoundTag rewardTag = new SNBTCompoundTag();
			rewardTag.putString("id", wr.getReward().getCodeString());
			wr.getReward().writeData(rewardTag, provider);
			wr.getReward().addAnyProtoTranslations(rewardTag);

			if (wr.getReward().getType() != RewardTypes.ITEM) {
				rewardTag.putString("type", wr.getReward().getType().getTypeForNBT());
			}
			if (wr.getWeight() != 1f) {
				rewardTag.putFloat("weight", wr.getWeight());
			}
			if (rewardTag.size() < 3) {
				rewardTag.singleLine();
			}

			list.add(rewardTag);
		}

		nbt.put("rewards", list);

		if (lootCrate != null) {
			nbt.put("loot_crate", Util.make(new CompoundTag(), tag -> lootCrate.writeData(tag)));
		}

		if (lootTableId != null) {
			nbt.putString("loot_table_id", lootTableId.toString());
		}
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		emptyWeight = nbt.getFloat("empty_weight").orElse(0f);
		lootSize = nbt.getInt("loot_size").orElse(0);
		hideTooltip = nbt.getBoolean("hide_tooltip").orElse(true);
		useTitle = nbt.getBoolean("use_title").orElse(true);

		Set<Long> prevRewards = weightedRewards.stream().map(wr -> wr.getReward().getId()).collect(Collectors.toSet());
		weightedRewards.clear();

		boolean refreshIds = false;

		ListTag list = nbt.getList("rewards").orElse(new ListTag());
		for (int i = 0; i < list.size(); i++) {
			boolean newReward = false;
			Optional<CompoundTag> rewardTagOptional = list.getCompound(i);
            if (rewardTagOptional.isEmpty()) {
                continue;
            }

            CompoundTag rewardTag = rewardTagOptional.get();
			if (!rewardTag.contains("id") && file.isServerSide()) {
				// can happen on server when reading in an older quest book where reward table rewards didn't have IDs
				rewardTag.putString("id", QuestObjectBase.getCodeString(file.newID()));
			}
			long rewardId = QuestObjectBase.parseCodeString(rewardTag.getString("id").orElse("0"));
			if (rewardId == 0L && file.isServerSide()) {
				// Can happen on server when the client has sent a reward table with new reward(s)
				// Note: can also happen on client when copying rewards that haven't been sent to server yet (reward editor screen)
				//       - in that case, an id of 0 is fine, so don't do anything here
				rewardId = file.newID();
				rewardTag.putString("id", QuestObjectBase.getCodeString(rewardId));
				newReward = refreshIds = true;
			}

			Reward reward = RewardType.createReward(rewardId, fakeQuest, rewardTag.getString("type").orElse(""));
			if (reward != null) {
				getQuestFile().getTranslationManager().processInitialTranslation(rewardTag, reward);
				reward.readData(rewardTag, provider);
				weightedRewards.add(new WeightedReward(reward, rewardTag.contains("weight") ? rewardTag.getFloat("weight").orElse(0f) : 1));
				prevRewards.remove(rewardId);
				if (newReward && getFile() instanceof ServerQuestFile sqf) {
					NetworkHelper.sendToAll(sqf.server, CreateObjectResponseMessage.create(reward, rewardTag));
				}
			}
		}

		if (refreshIds) {
			file.refreshRewardTableRewardIDs();
		}

		// clean up translations for any rewards that are no longer in the list
		if (getFile().isServerSide()) {
			prevRewards.forEach(id -> getFile().deleteObject(id));
		}

        lootCrate = nbt.getCompound("loot_crate").map(e -> {
            LootCrate crate = new LootCrate(this, false);
            crate.readData(e);
            return crate;
        }).orElse(null);

		lootTableId = nbt.getString("loot_table_id").map(Identifier::tryParse).orElse(null);
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
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
			buffer.writeLong(wr.getReward().getId());
			buffer.writeVarInt(wr.getReward().getType().intId);
			wr.getReward().writeNetData(buffer);
			buffer.writeFloat(wr.getWeight());
		}

		if (lootCrate != null) {
			lootCrate.writeNetData(buffer);
		}

		if (lootTableId != null) {
			buffer.writeIdentifier(lootTableId);
		}
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
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
			long id = buffer.readLong();
			RewardType type = file.getRewardType(buffer.readVarInt());
			Reward reward = type.createReward(id, fakeQuest);
			reward.readNetData(buffer);
			float weight = buffer.readFloat();
			weightedRewards.add(new WeightedReward(reward, weight));
		}

		lootCrate = null;

		if (hasCrate) {
			lootCrate = new LootCrate(this, false);
			lootCrate.readNetData(buffer);
		}

		lootTableId = hasLootTableId ? buffer.readIdentifier() : null;
	}

	@Override
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

		MinecraftServer server = ((ServerQuestFile) file).server;

		weightedRewards.forEach(wr ->
				file.getTranslationManager().getStringTranslation(wr.getReward(), file.getLocale(), TranslationKey.TITLE).ifPresent(title ->
						NetworkHelper.sendToAll(server, SyncTranslationMessageToClient.create(wr.getReward(), file.getLocale(), TranslationKey.TITLE, title))));
	}

	@Override
	public void onCreated() {
//		if (filename.isEmpty()) {
//			filename = file.generateRewardTableName(titleToID(getRawTitle()).orElse(toString()));
//		}

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
	public Component getAltTitle() {
		return weightedRewards.size() == 1 ?
				weightedRewards.get(0).getReward().getTitle() :
				Component.translatable("ftbquests.reward_table");

	}

	@Override
	public Icon<?> getAltIcon() {
		if (lootCrate != null) {
			return ItemIcon.ofItemStack(lootCrate.createStack());
		}

		if (weightedRewards.isEmpty()) {
			return Icons.DICE;
		}

		List<Icon<?>> icons = weightedRewards.stream().map(reward -> reward.getReward().getIcon()).collect(Collectors.toList());
		return AnimatedIcon.fromList(icons, false);
	}

	@Override
	public void onEditButtonClicked(Runnable gui) {
		new EditRewardTableScreen(gui, this, editedReward -> {
			NetworkManager.sendToServer(EditObjectMessage.forQuestObject(editedReward));
			clearCachedData();
		}).openGui();
	}

	public void addMouseOverText(TooltipList list, boolean includeWeight, boolean includeEmpty) {
		if (ClientQuestFile.INSTANCE.canEdit() || !hideTooltip) {
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

	public boolean shouldShowTooltip() {
		return !hideTooltip;
	}

	public RewardTable copy() {
		RewardTable copy = QuestObjectBase.copy(this, () ->  new RewardTable(this.id, this.getFile()));
		if (copy != null) {
			copy.setRawTitle(getRawTitle());
			copy.weightedRewards.clear();
			getWeightedRewards().forEach(wr -> copy.weightedRewards.add(wr.copy()));
		}
		return copy;
	}
}
