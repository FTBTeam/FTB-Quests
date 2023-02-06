package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class AddTaskButton extends Button {
	public final Quest quest;

	public AddTaskButton(Panel panel, Quest q) {
		super(panel, new TranslatableComponent("gui.add"), ThemeProperties.ADD_ICON.get());
		quest = q;
		setSize(18, 18);
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (TaskType type : TaskTypes.TYPES.values()) {
			contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
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
				if (FTBQuests.PROXY.getQuestFile(true).get(taskId) instanceof Task task) {
					contextMenu.add(ContextMenuItem.SEPARATOR);
					contextMenu.add(new PasteTaskMenuItem(task, () -> copyAndCreateTask(task)));
				}
			} catch (NumberFormatException ignored) {
			}
		}

		getGui().openContextMenu(contextMenu);
	}

	private void copyAndCreateTask(Task task) {
		Task task2 = TaskType.createTask(quest, task.getType().id.toString());
		if (task2 != null) {
			CompoundTag tag = new CompoundTag();
			task.writeData(tag);
			task2.readData(tag);
			CompoundTag extra = new CompoundTag();
			extra.putString("type", task2.getType().getTypeForNBT());
			new CreateObjectMessage(task2, extra).sendToServer();
		}
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(matrixStack, theme, x, y, w, h);
		}
	}

	public static class PasteTaskMenuItem extends TooltipContextMenuItem {
		public PasteTaskMenuItem(Task task, @Nullable Runnable callback) {
			super(new TranslatableComponent("ftbquests.gui.paste_task"), Icons.ADD, callback,
					new TextComponent("\"").append(task.getTitle()).append("\""),
					new TextComponent(QuestObjectBase.getCodeString(task.id)).withStyle(ChatFormatting.DARK_GRAY)
			);
		}
	}
}
