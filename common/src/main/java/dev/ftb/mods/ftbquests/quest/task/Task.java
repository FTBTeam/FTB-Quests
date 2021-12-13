package dev.ftb.mods.ftbquests.quest.task;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.WrappedIngredient;
import dev.ftb.mods.ftbquests.events.CustomTaskEvent;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.jei.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.DisplayCompletionToastMessage;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public abstract class Task extends QuestObject {
	public final Quest quest;

	public boolean exclusive = false;
	public boolean global = false;

	public Task(Quest q) {
		quest = q;
	}

	@Override
	public final QuestObjectType getObjectType() {
		return QuestObjectType.TASK;
	}

	@Override
	public final QuestFile getQuestFile() {
		return quest.chapter.file;
	}

	@Override
	public final Chapter getQuestChapter() {
		return quest.chapter;
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
		data.teamData.setStarted(id, data.time);
		
		ObjectStartedEvent.TASK.invoker().act(new ObjectStartedEvent.TaskEvent(data.withObject(this)));
		quest.onStarted(data.withObject(quest));
	}

	@Override
	public final void onCompleted(QuestProgressEventData<?> data) {
		data.teamData.setCompleted(id, data.time);
		
		ObjectCompletedEvent.TASK.invoker().act(new ObjectCompletedEvent.TaskEvent(data.withObject(this)));

		boolean questCompleted = quest.isCompletedRaw(data.teamData);

		if (quest.tasks.size() > 1 && !questCompleted && !disableToast) {
			new DisplayCompletionToastMessage(id).sendTo(data.notifiedPlayers);
		}

		if (questCompleted) {
			quest.onCompleted(data.withObject(quest));
		}
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);

		if (exclusive) { nbt.putBoolean("exclusive", exclusive); }
		if (global) { nbt.putBoolean("global", global); }
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		exclusive = nbt.getBoolean("exclusive");
		global = nbt.getBoolean("global");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeBoolean(exclusive);
		buffer.writeBoolean(global);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		exclusive = buffer.readBoolean();
		global = buffer.readBoolean();
	}

	public boolean canExclusive() {
		return false;
	};

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		if (canExclusive()) config.addBool("exclusive", exclusive, v -> exclusive = v, false).setNameKey("ftbquests.task.exclusive");
		config.addBool("global", global, v -> global = v, false).setNameKey("ftbquests.task.global");
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
		teamData.setProgress(this, progressChange.reset ? 0L : getMaxProgress());
	}

	@Override
	public final void deleteSelf() {
		quest.tasks.remove(this);

		for (TeamData data : quest.chapter.file.getAllData()) {
			data.resetProgress(this);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren() {
		for (TeamData data : quest.chapter.file.getAllData()) {
			data.resetProgress(this);
		}

		super.deleteChildren();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null) {
			gui.questPanel.refreshWidgets();
			gui.viewQuestPanel.refreshWidgets();
		}
	}

	@Override
	public final void onCreated() {
		quest.tasks.add(this);

		if (this instanceof CustomTask && quest.chapter.file.isServerSide()) {
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
		return getType().getIcon();
	}

	@Override
	public final ConfigGroup createSubGroup(ConfigGroup group) {
		TaskType type = getType();
		return group.getGroup(getObjectType().id).getGroup(type.id.getNamespace()).getGroup(type.id.getPath());
	}

	@Environment(EnvType.CLIENT)
	public void drawGUI(TeamData teamData, PoseStack matrixStack, int x, int y, int w, int h) {
		getIcon().draw(matrixStack, x, y, w, h);
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

	@Environment(EnvType.CLIENT)
	public void addMouseOverText(TooltipList list, TeamData teamData) {
		if (consumesResources()) {
			list.blankLine();
			list.add(new TranslatableComponent("ftbquests.task.click_to_submit").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
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
			new SubmitTaskMessage(id).sendToServer();
		}
	}

	public boolean submitItemsOnInventoryChange() {
		return false;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public Object getIngredient() {
		if (addTitleInMouseOverText()) {
			return getIcon().getIngredient();
		}

		return new WrappedIngredient(getIcon().getIngredient()).tooltip();
	}

	@Override
	public final int refreshJEI() {
		return FTBQuestsJEIHelper.QUESTS;
	}

	@Environment(EnvType.CLIENT)
	public MutableComponent getButtonText() {
		return getMaxProgress() > 1L || consumesResources() ? new TextComponent(formatMaxProgress()) : (MutableComponent) TextComponent.EMPTY;
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

	public boolean checkOnLogin() {
		return !consumesResources();
	}
}