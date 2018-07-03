package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GuiQuestTree extends GuiBase
{
	public class ButtonChapter extends Button
	{
		public QuestChapter chapter;
		public List<String> description;

		public ButtonChapter(Panel panel, QuestChapter c)
		{
			super(panel, c.title.getFormattedText(), c.getIcon());
			setSize(35, 26);
			chapter = c;
			description = new ArrayList<>();

			for (ITextComponent component : chapter.description)
			{
				component = component.createCopy();
				component.getStyle().setColor(TextFormatting.GRAY);
				description.add(component.getFormattedText());
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			selectedChapter = this;
			quests.setScrollX(0);
			quests.setScrollY(0);
			getGui().refreshWidgets();
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			list.add(getTitle() + questList.getCompletionSuffix(chapter));
			list.addAll(description);
		}

		@Override
		public Icon getIcon()
		{
			return icon;
		}

		@Override
		public void draw()
		{
			int ax = getAX();
			int ay = getAY();

			boolean selected = selectedChapter != null && chapter.equals(selectedChapter.chapter);
			getTheme().getHorizontalTab(selected).draw(selected ? ax : ax - 1, ay, width, height);

			if (!icon.isEmpty())
			{
				icon.draw(ax + 10, ay + (height - 16) / 2, 16, 16);
			}

			int r = 0;

			for (Quest quest : chapter.quests)
			{
				if (quest.isComplete(questList))
				{
					for (QuestReward reward : quest.rewards)
					{
						if (!questList.isRewardClaimed(ClientUtils.MC.player, reward))
						{
							r++;
						}
					}
				}
			}

			if (r > 0)
			{
				String s = Integer.toString(r);
				int nw = getStringWidth(s);
				GlStateManager.pushMatrix();
				GlStateManager.translate(ax + width - nw, ay, 500);
				drawString(s, -7, 4, Color4I.LIGHT_RED, 0);
				drawString(s, -5, 4, Color4I.LIGHT_RED, 0);
				drawString(s, -6, 3, Color4I.LIGHT_RED, 0);
				drawString(s, -6, 5, Color4I.LIGHT_RED, 0);
				drawString(s, -6, 4, Color4I.WHITE, 0);
				GlStateManager.popMatrix();
			}
			else if (chapter.isComplete(questList))
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 500);
				GuiIcons.CHECK.draw(ax + width - 14, ay + 4, 8, 8);
				GlStateManager.popMatrix();
			}
		}
	}

	public class ButtonQuest extends Button
	{
		public Quest quest;
		public String description;

		public ButtonQuest(Panel panel, Quest q)
		{
			super(panel, q.getTitle().getFormattedText(), q.getIcon());
			setSize(20, 20);
			quest = q;
			ITextComponent component = quest.getDescription().createCopy();
			component.getStyle().setColor(TextFormatting.GRAY);
			description = component.getFormattedText();

			if (TextFormatting.getTextWithoutFormattingCodes(description).isEmpty())
			{
				description = "";
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			questList.questGui = new GuiQuest(GuiQuestTree.this, quest);
			questList.questGui.openGui();
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			list.add(getTitle() + questList.getCompletionSuffix(quest));

			if (!description.isEmpty())
			{
				list.add(description);
			}
		}

		@Override
		public Icon getIcon()
		{
			return icon;
		}

		@Override
		public void draw()
		{
			int ax = getAX();
			int ay = getAY();

			getButtonBackground().draw(ax, ay, width, height);

			if (!icon.isEmpty())
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(ax + (width - zoom) / 2F, ay + (height - zoom) / 2F, 0F);
				icon.draw(0, 0, zoom, zoom);
				GlStateManager.popMatrix();
			}

			int r = 0;

			if (quest.isComplete(questList))
			{
				for (QuestReward reward : quest.rewards)
				{
					if (!questList.isRewardClaimed(ClientUtils.MC.player, reward))
					{
						r++;
					}
				}
			}

			if (r > 0)
			{
				String s = Integer.toString(r);
				int nw = getStringWidth(s);
				GlStateManager.pushMatrix();
				GlStateManager.translate(ax + width, ay, 500);

				if (zoom != 16)
				{
					GlStateManager.scale(zoom / 16D, zoom / 16D, 1D);
				}

				drawString(s, -nw + 2, 0, Color4I.LIGHT_RED, 0);
				drawString(s, -nw, 0, Color4I.LIGHT_RED, 0);
				drawString(s, -nw + 1, 1, Color4I.LIGHT_RED, 0);
				drawString(s, -nw + 1, -1, Color4I.LIGHT_RED, 0);
				drawString(s, -nw + 1, 0, Color4I.WHITE, 0);
				GlStateManager.popMatrix();
			}
			else if (quest.isComplete(questList))
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 500);
				GuiIcons.CHECK.draw(ax + width - 1 - zoom / 2, ay + 1, zoom / 2, zoom / 2);
				GlStateManager.popMatrix();
			}
		}
	}

	public final ClientQuestList questList;
	private final Int2ObjectOpenHashMap<ButtonQuest> questButtonMap;
	public int zoom = 16;
	private int scrollWidth, scrollHeight, prevMouseX, prevMouseY, grabbed;
	public ButtonChapter selectedChapter;
	public List<ButtonChapter> chapterButtons;
	public final Panel chapterPanel, quests;
	private final Button zoomIn, zoomOut;

	public GuiQuestTree(ClientQuestList q)
	{
		questList = q;

		chapterPanel = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				addAll(chapterButtons);
			}

			@Override
			public void alignWidgets()
			{
				setPosAndSize(-32, 3, 35, getGui().height - 8);
				align(new WidgetLayout.Vertical(0, 1, 0));
			}

			@Override
			public Icon getIcon()
			{
				return Icon.EMPTY;
			}
		};

		selectedChapter = null;
		chapterButtons = new ArrayList<>();

		for (QuestChapter chapter : questList.chapters)
		{
			ButtonChapter b = new ButtonChapter(chapterPanel, chapter);

			if (selectedChapter == null)
			{
				selectedChapter = b;
			}

			chapterButtons.add(b);
		}

		/*
		chaptersScrollBar = new PanelScrollBar(this, 14, chapterPanel)
		{
			@Override
			public boolean isEnabled()
			{
				return width > 0;
			}
		};*/

		questButtonMap = new Int2ObjectOpenHashMap<>();

		quests = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				questButtonMap.clear();

				if (selectedChapter != null)
				{
					int mx = 0;
					int my = 0;

					for (Quest quest : selectedChapter.chapter.quests)
					{
						ButtonQuest widget = new ButtonQuest(this, quest);
						questButtonMap.put(quest.id, widget);
						mx = Math.max(mx, quest.x);
						my = Math.max(my, quest.y);
						add(widget);
					}
				}
			}

			@Override
			public void alignWidgets()
			{
				scrollWidth = 0;
				scrollHeight = 0;

				if (selectedChapter != null)
				{
					int mx = 0;
					int my = 0;

					for (Widget widget : widgets)
					{
						Quest quest = ((ButtonQuest) widget).quest;
						mx = Math.max(mx, quest.x);
						my = Math.max(my, quest.y);
						widget.setPosAndSize(1 + quest.x * (zoom * 3 / 2), 1 + quest.y * (zoom * 3 / 2), zoom * 5 / 4, zoom * 5 / 4);
					}

					scrollWidth = 2 + mx * (zoom * 3 / 2) + zoom * 5 / 4;
					scrollHeight = 2 + my * (zoom * 3 / 2) + zoom * 5 / 4;
				}

				setPosAndSize(8, 24, getGui().width - 16, getGui().height - 32);
			}

			@Override
			public Icon getIcon()
			{
				return getTheme().getPanelBackground();
			}

			@Override
			protected void drawOffsetPanelBackground(int ax, int ay)
			{
				GlStateManager.disableTexture2D();
				GlStateManager.color(1F, 1F, 1F, 1F);
				GlStateManager.glLineWidth(3F);
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
				GlStateManager.shadeModel(GL11.GL_SMOOTH);

				for (Widget widget : widgets)
				{
					if (widget instanceof ButtonQuest)
					{
						ButtonQuest buttonQuest = (ButtonQuest) widget;

						for (int id : buttonQuest.quest.dependencies)
						{
							QuestObject dependency = questList.get(id);

							if (dependency instanceof Quest && buttonQuest.quest.chapter.equals(((Quest) dependency).chapter))
							{
								ButtonQuest b = questButtonMap.get(dependency.id);

								if (b != null)
								{
									buffer.pos(widget.getAX() + widget.width / 2, widget.getAY() + widget.height / 2, 0).color(100, 200, 100, 255).endVertex();
									buffer.pos(b.getAX() + b.width / 2, b.getAY() + b.height / 2, 0).color(50, 50, 50, 255).endVertex();
								}
							}
						}
					}
				}

				tessellator.draw();
				GlStateManager.glLineWidth(1F);
				GlStateManager.color(1F, 1F, 1F, 1F);
				GlStateManager.enableTexture2D();
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}

			@Override
			public boolean mousePressed(MouseButton button)
			{
				boolean b = super.mousePressed(button);

				if (!b)
				{
					prevMouseX = getMouseX();
					prevMouseY = getMouseY();
					grabbed = 1;
				}

				return b;
			}

			@Override
			public void mouseReleased(MouseButton button)
			{
				super.mouseReleased(button);
				grabbed = 0;
			}
		};

		zoomIn = new SimpleTextButton(this, "+", Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();

				if (zoom < 24)
				{
					zoom += 4;
					grabbed = 0;
					quests.setScrollX(0);
					quests.setScrollY(0);
					getGui().alignWidgets();
				}
			}

			@Override
			public void addMouseOverText(List<String> list)
			{
				list.add("Zoom In"); //LANG
			}

			@Override
			public void draw()
			{
				int ax = getAX();
				int ay = getAY();
				getButtonBackground().draw(ax, ay, width, height);
				drawString(getTitle(), ax + 3, ay + 2, SHADOW);
			}
		};

		zoomOut = new Button(this, "-", Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();

				if (zoom > 8)
				{
					zoom -= 4;
					grabbed = 0;
					quests.setScrollX(0);
					quests.setScrollY(0);
					getGui().alignWidgets();
				}
			}

			@Override
			public void addMouseOverText(List<String> list)
			{
				list.add("Zoom Out"); //LANG
			}

			@Override
			public void draw()
			{
				int ax = getAX();
				int ay = getAY();
				getButtonBackground().draw(ax, ay, width, height);
				drawString(getTitle(), ax + 3, ay + 2, SHADOW);
			}
		};
	}

	@Override
	public void addWidgets()
	{
		add(chapterPanel);
		add(quests);
		add(zoomOut);
		add(zoomIn);
	}

	@Override
	public void alignWidgets()
	{
		zoomOut.setPosAndSize(width - 36, 7, 12, 12);
		zoomIn.setPosAndSize(width - 19, 7, 12, 12);
		chapterPanel.alignWidgets();
		quests.alignWidgets();
	}

	@Override
	public int getAX()
	{
		return 36;
	}

	@Override
	public boolean onInit()
	{
		setSize(getScreen().getScaledWidth() - 76, getScreen().getScaledHeight() - 8);
		return true;
	}

	@Override
	public void drawBackground()
	{
		super.drawBackground();

		if (grabbed != 0)
		{
			int x = getMouseX();
			int y = getMouseY();
			quests.setScrollX(Math.max(Math.min(quests.getScrollX() + (prevMouseX - x), scrollWidth - quests.width), 0));
			quests.setScrollY(Math.max(Math.min(quests.getScrollY() + (prevMouseY - y), scrollHeight - quests.height), 0));
			prevMouseX = x;
			prevMouseY = y;
		}
	}

	@Override
	public boolean mouseScrolled(int scroll)
	{
		if (selectedChapter != null)
		{
			if (scroll > 0)
			{
				if (zoom < 24)
				{
					zoom += 4;
					grabbed = 0;
					quests.setScrollX(0);
					quests.setScrollY(0);
					getGui().alignWidgets();
					return true;
				}
			}
			else if (scroll < 0)
			{
				if (zoom > 8)
				{
					zoom -= 4;
					grabbed = 0;
					quests.setScrollX(0);
					quests.setScrollY(0);
					getGui().alignWidgets();
					return true;
				}
			}
		}

		return super.mouseScrolled(scroll);
	}

	@Override
	public void drawForeground()
	{
		if (selectedChapter != null)
		{
			drawString(selectedChapter.getTitle(), getAX() + 8, getAY() + 8, getTheme().getInvertedContentColor(), 0);
		}

		super.drawForeground();
	}

	@Override
	@Nullable
	public GuiScreen getPrevScreen()
	{
		return null;
	}
}