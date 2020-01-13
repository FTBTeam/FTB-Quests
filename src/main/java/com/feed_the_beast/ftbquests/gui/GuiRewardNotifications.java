package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.quest.QuestShape;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;
import com.feed_the_beast.mods.ftbguilibrary.widget.WrappedIngredient;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiRewardNotifications extends GuiBase implements IRewardListenerGui
{
	private class RewardNotification extends Widget
	{
		private final RewardKey key;

		public RewardNotification(Panel p, RewardKey e)
		{
			super(p);
			setSize(22, 22);
			key = e;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			if (!key.title.isEmpty())
			{
				list.add(key.title);
			}
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			QuestShape.RSQUARE.outline.draw(x, y, w, h);
			key.icon.draw(x + 3, y + 3, 16, 16);

			int count = rewards.getInt(key);

			if (count > 1)
			{
				RenderSystem.pushMatrix();
				RenderSystem.translatef(0, 0, 600);
				String s = StringUtils.formatDouble(count, true);
				theme.drawString(TextFormatting.YELLOW + s, x + 22 - theme.getStringWidth(s), y + 12, Theme.SHADOW);
				RenderSystem.popMatrix();
			}
		}

		@Override
		@Nullable
		public Object getIngredientUnderMouse()
		{
			return new WrappedIngredient(key.icon.getIngredient()).tooltip();
		}
	}

	public final Object2IntOpenHashMap<RewardKey> rewards;
	private final SimpleTextButton closeButton;
	private final Panel itemPanel;

	public GuiRewardNotifications()
	{
		rewards = new Object2IntOpenHashMap<>();
		closeButton = new SimpleTextButton(this, I18n.format("gui.close"), Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				playClickSound();
				getGui().closeGui();
			}
		};

		itemPanel = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				List<RewardKey> keys = new ArrayList<>(rewards.keySet());
				keys.sort((o1, o2) -> Integer.compare(rewards.getInt(o2), rewards.getInt(o1)));

				for (RewardKey key : keys)
				{
					add(new RewardNotification(this, key));
				}
			}

			@Override
			public void alignWidgets()
			{
				if (widgets.size() < 9)
				{
					setWidth(align(new WidgetLayout.Horizontal(0, 1, 0)));
					setHeight(22);
				}
				else
				{
					setWidth(23 * 9);
					setHeight(23 * MathHelper.ceil(widgets.size() / 9F));

					for (int i = 0; i < widgets.size(); i++)
					{
						widgets.get(i).setPos((i % 9) * 23, (i / 9) * 23);
					}
				}

				setPos((GuiRewardNotifications.this.width - itemPanel.width) / 2, (GuiRewardNotifications.this.height - itemPanel.height) / 2);
			}
		};

		itemPanel.setOnlyRenderWidgetsInside(false);
		itemPanel.setUnicode(true);
	}

	@Override
	public void addWidgets()
	{
		add(itemPanel);
		add(closeButton);
		closeButton.setPos((width - closeButton.width) / 2, height * 2 / 3 + 16);
	}

	@Override
	public boolean onInit()
	{
		return setFullscreen();
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		RenderSystem.pushMatrix();
		RenderSystem.translatef((int) (w / 2F), (int) (h / 5F), 0F);
		RenderSystem.scalef(2, 2, 1);
		String s = I18n.format("ftbquests.rewards");
		theme.drawString(s, -theme.getStringWidth(s) / 2F, 0, Color4I.WHITE, 0);
		RenderSystem.popMatrix();
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	public void rewardReceived(RewardKey key, int count)
	{
		rewards.put(key, rewards.getInt(key) + count);
		itemPanel.refreshWidgets();
	}
}