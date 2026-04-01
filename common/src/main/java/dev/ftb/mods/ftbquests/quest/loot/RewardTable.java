package dev.ftb.mods.ftbquests.quest.loot;

import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.gui.widget.BaseScreen;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.icon.AnimatedIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.EditRewardTableScreen;
import dev.ftb.mods.ftbquests.client.gui.RewardTablesScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.net.CreateObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.net.SyncTranslationMessageToClient;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class RewardTable extends QuestObjectBase {
	private final BaseQuestFile file;
	private final List<WeightedReward> weightedRewards;
	private final Quest fakeQuest;
	private float emptyWeight;
	private int lootSize;
	private boolean hideTooltip;
	private boolean useTitle;
	@Nullable
	private LootCrate lootCrate;
	@Nullable
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

	public static QuestObjectBase createRewardForTable(long id, String type, BaseQuestFile file) {
		return RewardType.createReward(id, makeFakeQuest(file), type);
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
	public void writeData(Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);

		if (emptyWeight > 0f) json.addProperty("empty_weight", emptyWeight);
		json.addProperty("loot_size", lootSize);
		if (hideTooltip) json.addProperty("hide_tooltip", true);
		if (useTitle) json.addProperty("use_title", true);

		Json5Array list = new Json5Array();
		for (WeightedReward wr : weightedRewards) {
			Json5Object rewardJson = new Json5Object();
			rewardJson.addProperty("id", wr.getReward().getCodeString());
			wr.getReward().writeData(rewardJson, provider);
			wr.getReward().addAnyProtoTranslations(rewardJson);

			if (wr.getReward().getType() != RewardTypes.ITEM) {
				rewardJson.addProperty("type", wr.getReward().getType().getTypeForSerialization());
			}
			if (wr.getWeight() != 1f) {
				rewardJson.addProperty("weight", wr.getWeight());
			}
//			if (rewardJson.size() < 3) {
//				rewardJson.singleLine();
//			}

			list.add(rewardJson);
		}

		json.add("rewards", list);

		if (lootCrate != null) json.add("loot_crate", Util.make(new Json5Object(), o -> lootCrate.writeData(o)));
		if (lootTableId != null) json.addProperty("loot_table_id", lootTableId.toString());
	}

	@Override
	public void readData(Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);
		emptyWeight = Json5Util.getFloat(json, "empty_weight").orElse(0f);
		lootSize = Json5Util.getInt(json, "loot_size").orElse(0);
		hideTooltip = Json5Util.getBoolean(json, "hide_tooltip").orElse(true);
		useTitle = Json5Util.getBoolean(json,  "use_title").orElse(true);

		Set<Long> prevRewards = weightedRewards.stream().map(wr -> wr.getReward().getId()).collect(Collectors.toSet());
		weightedRewards.clear();

		boolean refreshIds = false;

		Json5Array list = Json5Util.getJson5Array(json, "rewards").orElse(new Json5Array());
		for (var el : list) {
			boolean newReward = false;
			if (el instanceof Json5Object rewardTag) {
				if (!rewardTag.has("id") && file.isServerSide()) {
					// can happen on server when reading in an older quest book where reward table rewards didn't have IDs
					rewardTag.addProperty("id", QuestObjectBase.getCodeString(file.newID()));
				}
				long rewardId = QuestObjectBase.parseCodeString(Json5Util.getString(rewardTag, "id").orElse("0"));
				if (rewardId == 0L && file.isServerSide()) {
					// Can happen on server when the client has sent a reward table with new reward(s)
					// Note: can also happen on client when copying rewards that haven't been sent to server yet (reward editor screen)
					//       - in that case, an id of 0 is fine, so don't do anything here
					rewardId = file.newID();
					rewardTag.addProperty("id", QuestObjectBase.getCodeString(rewardId));
					newReward = refreshIds = true;
				}

				Reward reward = RewardType.createReward(rewardId, fakeQuest, Json5Util.getString(rewardTag, "type").orElse(""));
				getQuestFile().getTranslationManager().processInitialTranslation(rewardTag, reward);
				reward.readData(rewardTag, provider);
				float weight = rewardTag.has("weight") ? Json5Util.getFloat(rewardTag, "weight").orElse(0f) : 1;
				weightedRewards.add(new WeightedReward(reward, weight));
				prevRewards.remove(rewardId);
				if (newReward && getFile() instanceof ServerQuestFile sqf) {
					Server2PlayNetworking.sendToAllPlayers(sqf.server, CreateObjectResponseMessage.create(reward, rewardTag));
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

		lootCrate = Json5Util.getJson5Object(json, "loot_crate")
				.map(o -> Util.make(new LootCrate(this, false), c -> c.readData(o)))
				.orElse(null);

		lootTableId = Json5Util.getString(json, "loot_table_id").map(Identifier::tryParse).orElse(null);
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
			buffer.writeVarInt(wr.getReward().getType().internalId);
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
	public void fillConfigGroup(EditableConfigGroup config) {
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
						Server2PlayNetworking.sendToAllPlayers(server, SyncTranslationMessageToClient.create(wr.getReward(), file.getLocale(), TranslationKey.TITLE, title))));
	}

	@Override
	public void onCreated() {
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
				weightedRewards.getFirst().getReward().getTitle() :
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
			Play2ServerNetworking.send(EditObjectMessage.forQuestObject(editedReward));
			clearCachedData();
		}).openGui();
	}

	public void addMouseOverText(TooltipList list, boolean includeWeight, boolean includeEmpty) {
		if (ClientQuestFile.getInstance().canEdit() || !hideTooltip) {
			float totalWeight = getTotalWeight(includeEmpty);

			if (includeWeight && includeEmpty && emptyWeight > 0) {
				addItem(list, Component.translatable("ftbquests.reward_table.nothing"), emptyWeight, totalWeight);
			}

			List<WeightedReward> sortedRewards = weightedRewards.stream().sorted().toList();

			BaseScreen gui = ClientUtils.getCurrentGuiAs(BaseScreen.class);
			int maxLines = gui == null ? 12 : (gui.height - 20) / (gui.getTheme().getFontHeight() + 2);
			int nRewards = sortedRewards.size();
			int start = nRewards > maxLines ?
					(int) ((ClientUtils.getClientLevel().getGameTime() / 10) % nRewards) :
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

	@Nullable
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
		copy.setRawTitle(getRawTitle());
		copy.weightedRewards.clear();
		getWeightedRewards().forEach(wr -> copy.weightedRewards.add(wr.copy()));
		return copy;
	}
}
