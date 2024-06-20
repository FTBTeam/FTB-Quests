package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.events.CustomTaskEvent;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class Task extends QuestObject {
	private final Quest quest;
	private boolean optionalTask;

	public Task(long id, Quest quest) {
		super(id);

		this.quest = quest;
		optionalTask = false;
	}

	public Quest getQuest() {
		return quest;
	}

	@Override
	public final QuestObjectType getObjectType() {
		return QuestObjectType.TASK;
	}

	@Override
	public final BaseQuestFile getQuestFile() {
		return quest.getChapter().file;
	}

	@Override
	public final Chapter getQuestChapter() {
		return quest.getChapter();
	}

	@Override
	public final long getParentID() {
		return quest.id;
	}

	public abstract TaskType getType();

	@Override
	public final int getRelativeProgressFromChildren(TeamData data) {
		long max = getMaxProgress();

		if (max <= 0L) {
			return 0;
		}

		long progress = data.getProgress(this);

		if (progress <= 0L) {
			return 0;
		} else if (progress >= max) {
			return 100;
		}

		return (int) Math.max(1L, (progress * 100D / (double) max));
	}

	@Override
	public void onStarted(QuestProgressEventData<?> data) {
		data.setStarted(id);
		ObjectStartedEvent.TASK.invoker().act(new ObjectStartedEvent.TaskEvent(data.withObject(this)));
		quest.onStarted(data.withObject(quest));
	}

	@Override
	public final void onCompleted(QuestProgressEventData<?> data) {
		data.setCompleted(id);
		ObjectCompletedEvent.TASK.invoker().act(new ObjectCompletedEvent.TaskEvent(data.withObject(this)));

		boolean questCompleted = quest.isCompletedRaw(data.getTeamData());

		if (quest.getTasks().size() > 1 && !questCompleted && !disableToast) {
			data.notifyPlayers(id);
		}

		if (questCompleted) {
			quest.onCompleted(data.withObject(quest));
		}
	}

	@Override
	public boolean isOptionalForProgression() {
		return optionalTask;
	}

	public long getMaxProgress() {
		return 1L;
	}

	public String formatMaxProgress() {
		return StringUtils.formatDouble(getMaxProgress(), true);
	}

	public String formatProgress(TeamData teamData, long progress) {
		return StringUtils.formatDouble(progress, true);
	}

	@Override
	public final void forceProgress(TeamData teamData, ProgressChange progressChange) {
		teamData.setProgress(this, progressChange.shouldReset() ? 0L : getMaxProgress());
	}

	@Override
	public final void deleteSelf() {
		quest.removeTask(this);

		for (TeamData data : quest.getChapter().file.getAllTeamData()) {
			data.resetProgress(this);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren() {
		for (TeamData data : quest.getChapter().file.getAllTeamData()) {
			data.resetProgress(this);
		}

		super.deleteChildren();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
		if (gui != null) {
			gui.refreshQuestPanel();
			gui.refreshViewQuestPanel();
		}
	}

	@Override
	public final void onCreated() {
		quest.addTask(this);

		if (this instanceof CustomTask && getQuestFile().isServerSide()) {
			CustomTaskEvent.EVENT.invoker().act(new CustomTaskEvent((CustomTask) this));
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		return getType().getDisplayName();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		return getType().getIconSupplier();
	}

	@Override
	public final ConfigGroup createSubGroup(ConfigGroup group) {
		TaskType type = getType();
		return group.getOrCreateSubgroup(getObjectType().getId())
				.getOrCreateSubgroup(type.getTypeId().getNamespace())
				.getOrCreateSubgroup(type.getTypeId().getPath());
	}

	@Environment(EnvType.CLIENT)
	public void drawGUI(TeamData teamData, GuiGraphics graphics, int x, int y, int w, int h) {
		getIcon().draw(graphics, x, y, w, h);
	}

	public boolean canInsertItem() {
		return false;
	}

	public boolean consumesResources() {
		return canInsertItem();
	}

	public boolean hideProgressNumbers() {
		return getMaxProgress() <= 1L;
	}

	/**
	 * Called before any progress information is added to the task tooltip
	 * @param list list to append text to
	 * @param teamData the team / player data
	 * @param advanced true for advanced tooltips (when F3+H is in use)
	 */
	@Environment(EnvType.CLIENT)
	public void addMouseOverHeader(TooltipList list, TeamData teamData, boolean advanced) {
		list.add(getTitle());
	}

	/**
	 * Called after any progress information is added to the task tooltip
	 * @param list list to append text to
	 * @param teamData the team / player data
	 */
	@Environment(EnvType.CLIENT)
	public void addMouseOverText(TooltipList list, TeamData teamData) {
		if (consumesResources()) {
			list.blankLine();
			list.add(Component.translatable("ftbquests.task.click_to_submit").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
		}
	}

	@Environment(EnvType.CLIENT)
	public boolean addTitleInMouseOverText() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
		if (canClick && autoSubmitOnPlayerTick() <= 0) {
			button.playClickSound();
			NetworkManager.sendToServer(new SubmitTaskMessage(id));
		}
	}

	public boolean submitItemsOnInventoryChange() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public Optional<PositionedIngredient> getIngredient(Widget widget) {
		if (addTitleInMouseOverText()) {
			return PositionedIngredient.of(getIcon().getIngredient(), widget);
		}
		return Optional.empty();
	}

	@Override
	public Set<RecipeModHelper.Components> componentsToRefresh() {
		return EnumSet.of(RecipeModHelper.Components.QUESTS);
	}

	@Environment(EnvType.CLIENT)
	public MutableComponent getButtonText() {
		return getMaxProgress() > 1L || consumesResources() ? Component.literal(formatMaxProgress()) : Component.empty();
	}

	public int autoSubmitOnPlayerTick() {
		return 0;
	}

	@Override
	public final boolean cacheProgress() {
		return false;
	}

	public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
	}

	public final void submitTask(TeamData teamData, ServerPlayer player) {
		submitTask(teamData, player, ItemStack.EMPTY);
	}

	protected final boolean checkTaskSequence(TeamData teamData) {
		if (quest.getRequireSequentialTasks()) {
			List<Task> tasks = quest.getTasksAsList();
			int idx = tasks.indexOf(this);
			return idx >= 0 && (idx == 0 || teamData.isCompleted(tasks.get(idx - 1)));
		} else {
			return true;
		}
	}

	public boolean checkOnLogin() {
		return !consumesResources();
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		if (optionalTask) nbt.putBoolean("optional_task", true);
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);

		optionalTask = nbt.getBoolean("optional_task");
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);

		buffer.writeBoolean(optionalTask);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);

		optionalTask = buffer.readBoolean();
	}

	@Override
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		config.addBool("optional_task", optionalTask, v -> optionalTask = v, false).setNameKey("ftbquests.quest.misc.optional");
	}

	protected ResourceLocation safeResourceLocation(String str, ResourceLocation fallback) {
		var location = ResourceLocation.tryParse(str);
		if (location != null) {
			return location;
		}

		if (getQuestFile().isServerSide()) {
			FTBQuests.LOGGER.warn("Ignoring bad resource location '{}' for task {}", str, id);
		} else {
			FTBQuestsClient.getClientPlayer().displayClientMessage(
					Component.literal("Bad resource location: " + str).withStyle(ChatFormatting.RED), false);
		}

		return fallback;
	}
}
