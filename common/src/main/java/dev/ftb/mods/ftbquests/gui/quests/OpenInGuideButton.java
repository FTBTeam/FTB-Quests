package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

/**
 * @author LatvianModder
 */
public class OpenInGuideButton extends SimpleTextButton {
	private final Quest quest;

	public OpenInGuideButton(Panel panel, Quest q) {
		super(panel, Component.translatable("ftbquests.gui.open_in_guide"), ItemIcon.getItemIcon(Items.BOOK));
		setHeight(13);
		setX((panel.width - width) / 2);
		quest = q;
	}

	@Override
	public void onClicked(MouseButton button) {
		handleClick("guide", quest.guidePage);
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
	}
}
