package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class AddTaskButton extends Button {
	public final Quest quest;

	public AddTaskButton(Panel panel, Quest q) {
		super(panel, Component.translatable("gui.add"), ThemeProperties.ADD_ICON.get());
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

		getGui().openContextMenu(contextMenu);
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(matrixStack, theme, x, y, w, h);
		}
	}
}
