package dev.ftb.mods.ftbquests.quest.task;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.events.CustomTaskEvent;
import dev.ftb.mods.ftbquests.events.progress.ProgressEventData;
import dev.ftb.mods.ftbquests.events.progress.ProgressType;
import dev.ftb.mods.ftbquests.events.progress.TaskProgressEvent;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.UnknownNullability;

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
	public Quest getRelatedQuest() {
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
	public void onStarted(ProgressEventData<?> data) {
		data.setStarted(id);
		NativeEventPosting.get().postEvent(new TaskProgressEvent.Data(ProgressType.STARTED, data.withObject(this)));
		quest.onStarted(data.withObject(quest));
	}

	@Override
	public final void onCompleted(ProgressEventData<?> data) {
		data.setCompleted(id);
		NativeEventPosting.get().postEvent(new TaskProgressEvent.Data(ProgressType.COMPLETED, data.withObject(this)));

		boolean questCompleted = quest.isCompletedRaw(data.teamData());

		if (quest.getTasks().size() > 1 && !questCompleted && !disableToast) {
			data.notifyPlayers(id);
		}

		if (questCompleted) {
			quest.onCompleted(data.withObject(quest));
		}
	}

	@Override
	public boolean isOptionalForProgression(TeamData teamData) {
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
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
		if (gui != null) {
			gui.refreshChapterPanel();
			gui.refreshQuestPanel();
			gui.refreshViewQuestPanel();
		}
	}

	@Override
	public final void onCreated() {
		quest.addTask(this);

		if (this instanceof CustomTask customTask && getQuestFile().isServerSide()) {
			NativeEventPosting.get().postEvent(new CustomTaskEvent.Data(customTask));
		}
	}

	@Override
	public Component getAltTitle() {
		return getType().getDisplayName();
	}

	@Override
	public Icon<?> getAltIcon() {
		return getType().getIconSupplier();
	}

	@Override
	public final EditableConfigGroup createSubGroup(EditableConfigGroup group) {
		TaskType type = getType();
		return group.getOrCreateSubgroup(getObjectType().getId())
				.getOrCreateSubgroup(type.getTypeId().getNamespace())
				.getOrCreateSubgroup(type.getTypeId().getPath());
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
	public void addMouseOverHeader(TooltipList list, TeamData teamData, boolean advanced) {
		list.add(getTitle());
	}

	/**
	 * Called after any progress information is added to the task tooltip
	 * @param list list to append text to
	 * @param teamData the team / player data
	 */
	public void addMouseOverText(TooltipList list, TeamData teamData) {
	}

	public boolean addTitleInMouseOverText() {
		return true;
	}

	public TaskClient client() {
		return TaskClient.Default.INSTANCE;
	}

	public boolean submitItemsOnInventoryChange() {
		return false;
	}

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
	public void writeData(@UnknownNullability Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);
		if (optionalTask) json.addProperty("optional_task", true);
	}

	@Override
	public void readData(@UnknownNullability Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);

		optionalTask = Json5Util.getBoolean(json, "optional_task").orElse(false);
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
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);

		config.addBool("optional_task", optionalTask, v -> optionalTask = v, false).setNameKey("ftbquests.quest.misc.optional_task");
	}

	protected Identifier safeResourceLocation(String str, Identifier fallback) {
		var location = Identifier.tryParse(str);
		if (location != null) {
			return location;
		}

		if (getQuestFile().isServerSide()) {
			FTBQuests.LOGGER.warn("Ignoring bad resource location '{}' for task {}", str, id);
		} else {
			ClientUtils.getClientPlayer().sendSystemMessage(
					Component.literal("Bad resource location: " + str).withStyle(ChatFormatting.RED));
		}

		return fallback;
	}
}
