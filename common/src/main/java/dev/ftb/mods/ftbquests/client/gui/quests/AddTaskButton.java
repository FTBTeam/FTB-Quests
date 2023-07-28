package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AddTaskButton extends Button {
	private final Quest quest;

	public AddTaskButton(Panel panel, Quest quest) {
		super(panel, Component.translatable("gui.add"), ThemeProperties.ADD_ICON.get());
		this.quest = quest;
		setSize(18, 18);
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (TaskType type : TaskTypes.TYPES.values()) {
			contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIconSupplier(), () -> {
				playClickSound();
				type.getGuiProvider().openCreationGui(this, quest, task -> {
					CompoundTag extra = new CompoundTag();
					extra.putString("type", type.getTypeForNBT());
					new CreateObjectMessage(task, extra).sendToServer();
				});
			}));
		}

		String clip = getClipboardString();
		if (!clip.isEmpty()) {
			try {
				long taskId = Long.valueOf(clip, 16);
				if (FTBQuests.getQuestFile(true).get(taskId) instanceof Task task) {
					contextMenu.add(ContextMenuItem.SEPARATOR);
					contextMenu.add(new PasteTaskMenuItem(task, () -> copyAndCreateTask(task)));
				}
			} catch (NumberFormatException ignored) {
			}
		}

		getGui().openContextMenu(contextMenu);
	}

	private void copyAndCreateTask(Task task) {
		Task newTask = QuestObjectBase.copy(task, () -> TaskType.createTask(0L, quest, task.getType().getTypeId().toString()));
		if (newTask != null) {
			CompoundTag extra = new CompoundTag();
			extra.putString("type", newTask.getType().getTypeForNBT());
			new CreateObjectMessage(newTask, extra).sendToServer();
		}
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(graphics, theme, x, y, w, h);
		}
	}

	public static class PasteTaskMenuItem extends TooltipContextMenuItem {
		public PasteTaskMenuItem(Task task, @Nullable Runnable callback) {
			super(Component.translatable("ftbquests.gui.paste_task"), Icons.ADD, callback,
					Component.literal("\"").append(task.getTitle()).append("\""),
					Component.literal(QuestObjectBase.getCodeString(task.id)).withStyle(ChatFormatting.DARK_GRAY)
			);
		}
	}
}
