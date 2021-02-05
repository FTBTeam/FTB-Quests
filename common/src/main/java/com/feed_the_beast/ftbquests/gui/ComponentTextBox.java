package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Bits;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;

/**
 * @author LatvianModder
 */
public class ComponentTextBox extends Widget
{
	public Component component = TextComponent.EMPTY;
	private FormattedText[] formattedText = new FormattedText[0];
	public int textFlags = 0;
	public int maxWidth = 0;
	public int textSpacing = 10;
	public float scale = 1.0F;
	public Color4I textColor;

	public ComponentTextBox(Panel panel)
	{
		super(panel);
		textColor = Icon.EMPTY;
	}

	public ComponentTextBox addFlags(int flags)
	{
		textFlags |= flags;
		return this;
	}

	public ComponentTextBox setMaxWidth(int width)
	{
		maxWidth = width;
		return this;
	}

	public ComponentTextBox setColor(Color4I color)
	{
		textColor = color;
		return this;
	}

	public ComponentTextBox setScale(float s)
	{
		scale = s;
		return this;
	}

	public ComponentTextBox setSpacing(int s)
	{
		textSpacing = s;
		return this;
	}

	public ComponentTextBox setText(Component txt)
	{
		Theme theme = getGui().getTheme();
		formattedText = theme.listFormattedStringToWidth(new TextComponent("").append(txt), maxWidth).toArray(new FormattedText[0]);
		return resize(theme);
	}

	public ComponentTextBox resize(Theme theme)
	{
		if (maxWidth == 0)
		{
			setWidth(0);

			for (FormattedText s : formattedText)
			{
				setWidth(Math.max(width, (int) ((float) theme.getStringWidth(s) * scale)));
			}
		}
		else
		{
			setWidth(maxWidth);
		}

		setHeight((int) ((float) (Math.max(1, formattedText.length) * textSpacing - (textSpacing - theme.getFontHeight() + 1)) * scale));
		return this;
	}

	@Override
	public void addMouseOverText(TooltipList list)
	{
	}

	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		drawBackground(matrixStack, theme, x, y, w, h);

		if (formattedText.length != 0)
		{
			boolean centered = Bits.getFlag(textFlags, 4);
			boolean centeredV = Bits.getFlag(textFlags, 32);
			Color4I col = textColor;
			if (col.isEmpty())
			{
				col = theme.getContentColor(WidgetType.mouseOver(Bits.getFlag(textFlags, 16)));
			}

			int tx = x + (centered ? w / 2 : 0);
			int ty = y + (centeredV ? (h - theme.getFontHeight()) / 2 : 0);
			int i;
			if (scale == 1.0F)
			{
				for (i = 0; i < formattedText.length; ++i)
				{
					theme.drawString(matrixStack, formattedText[i], (float) tx, (float) (ty + i * textSpacing), col, textFlags);
				}
			}
			else
			{
				matrixStack.pushPose();
				matrixStack.translate(tx, ty, 0.0D);
				matrixStack.scale(scale, scale, 1.0F);

				for (i = 0; i < formattedText.length; ++i)
				{
					theme.drawString(matrixStack, formattedText[i], 0.0F, (float) (i * textSpacing), col, textFlags);
				}

				matrixStack.popPose();
			}

		}
	}
}
