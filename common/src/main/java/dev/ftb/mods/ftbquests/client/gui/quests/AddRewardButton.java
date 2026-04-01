package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class AddRewardButton extends Button {
	private final Quest quest;

	public AddRewardButton(Panel panel, Quest q) {
		super(panel, Component.translatable("gui.add"), ThemeProperties.ADD_ICON.get());
		quest = q;
		setSize(18, 18);
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (RewardType type : RewardTypes.TYPES.values()) {
			if (type.getGuiProvider() != null) {
				contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIconSupplier(), b -> {
					playClickSound();
					type.getGuiProvider().openCreationGui(parent, quest, reward ->
							Play2ServerNetworking.send(CreateObjectMessage.create(reward, type.makeExtraJson()))
					);
				}));
			}
		}

		getGui().openContextMenu(contextMenu);
	}

	@Override
	public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(graphics, theme, x, y, w, h);
		}
	}
}
