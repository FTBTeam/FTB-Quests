package dev.ftb.mods.ftbquests.quest.task;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.WrappedIngredient;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbquests.events.CustomTaskEvent;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.jei.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.MessageDisplayCompletionToast;
import dev.ftb.mods.ftbquests.net.MessageSubmitTask;
import dev.ftb.mods.ftbquests.quest.ChangeProgress;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class Task extends QuestObject {
	public final Quest quest;

	public Task(Quest q) {
		quest = q;
	}

	@Override
	public final String toString() {
		return quest.chapter.filename + ":" + quest.getCodeString() + ":T:" + getCodeString();
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

	public abstract TaskData createData(TeamData data);

	@Override
	public final int getRelativeProgressFromChildren(TeamData data) {
		return data.getTaskData(this).getRelativeProgress();
	}

	@Override
	public final void onCompleted(TeamData data, List<ServerPlayer> onlineMembers, List<ServerPlayer> notifiedPlayers) {
		super.onCompleted(data, onlineMembers, notifiedPlayers);
		ObjectCompletedEvent.TASK.invoker().act(new ObjectCompletedEvent.TaskEvent(data, this, onlineMembers, notifiedPlayers));
		boolean questComplete = data.isComplete(quest);

		if (quest.tasks.size() > 1 && !questComplete && !disableToast) {
			new MessageDisplayCompletionToast(id).sendTo(notifiedPlayers);
		}

		if (questComplete) {
			quest.onCompleted(data, onlineMembers, notifiedPlayers);
		}
	}

	public long getMaxProgress() {
		return 1L;
	}

	public String getMaxProgressString() {
		return StringUtils.formatDouble(getMaxProgress(), true);
	}

	@Override
	public final void changeProgress(TeamData data, ChangeProgress type) {
		data.getTaskData(this).setProgress(type.reset ? 0L : getMaxProgress());
	}

	@Override
	public final void deleteSelf() {
		quest.tasks.remove(this);

		for (TeamData data : quest.chapter.file.getAllData()) {
			data.removeTaskData(this);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren() {
		for (TeamData data : quest.chapter.file.getAllData()) {
			data.removeTaskData(this);
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

		for (TeamData data : quest.chapter.file.getAllData()) {
			data.createTaskData(this, true);
		}

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

	public void drawGUI(@Nullable TaskData data, PoseStack matrixStack, int x, int y, int w, int h) {
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
	public void addMouseOverText(TooltipList list, @Nullable TaskData data) {
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
			new MessageSubmitTask(id).sendToServer();
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
		return getMaxProgress() > 1L || consumesResources() ? new TextComponent(getMaxProgressString()) : (MutableComponent) TextComponent.EMPTY;
	}

	public int autoSubmitOnPlayerTick() {
		return 0;
	}

	@Override
	public final boolean cacheProgress() {
		return false;
	}
}