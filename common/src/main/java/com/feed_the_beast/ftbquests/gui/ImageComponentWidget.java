package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.client.ImageComponent;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.HoverEvent;

public class ImageComponentWidget extends Widget {
	public final ImageComponent component;

	public ImageComponentWidget(Panel panel, ImageComponent c) {
		super(panel);
		component = c;
		setSize(component.width, component.height);
	}

	public void addMouseOverText(TooltipList list) {
		if (component.getStyle().getHoverEvent() != null && component.getStyle().getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {
			list.add(component.getStyle().getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT));
		}
	}

	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		component.image.draw(matrixStack, x, y, w, h);
	}
}

