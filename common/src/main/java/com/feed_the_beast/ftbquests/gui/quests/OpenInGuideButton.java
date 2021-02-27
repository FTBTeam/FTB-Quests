package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

/**
 * @author LatvianModder
 */
public class OpenInGuideButton extends SimpleTextButton {
	private final Quest quest;

	public OpenInGuideButton(Panel panel, Quest q) {
		super(panel, new TranslatableComponent("ftbquests.gui.open_in_guide"), ItemIcon.getItemIcon(Items.BOOK));
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