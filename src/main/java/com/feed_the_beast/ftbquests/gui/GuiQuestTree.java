package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiLang;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectors;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestDependency;
import com.feed_the_beast.ftbquests.quest.QuestPosition;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GuiQuestTree extends GuiBase
{
	public class ButtonChapter extends Button
	{
		public QuestChapter chapter;
		public List<String> description;

		public ButtonChapter(Panel panel, QuestChapter c)
		{
			super(panel, c.title.getFormattedText(), c.icon);
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
			list.add(getTitle());
			list.addAll(description);

			if (isShiftKeyDown())
			{
				list.add(chapter.getCompletionString(ClientQuestList.INSTANCE));
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

			boolean selected = selectedChapter != null && chapter.equals(selectedChapter.chapter);
			getTheme().getHorizontalTab(selected).draw(selected ? ax : ax - 1, ay, width, height);

			if (!icon.isEmpty())
			{
				icon.draw(ax + (selected ? 10 : 12), ay + (height - 16) / 2, 16, 16);
			}
		}
	}

	public class ButtonQuest extends Button
	{
		public Quest quest;
		public String description;

		public ButtonQuest(Panel panel, Quest q)
		{
			super(panel, q.title.getFormattedText(), q.icon);
			setSize(20, 20);
			quest = q;
			ITextComponent component = quest.description.createCopy();
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
			FTBQuestsClient.questGui = new GuiQuest(quest);
			FTBQuestsClient.questGui.openGui();
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			list.add(getTitle());

			if (!description.isEmpty())
			{
				list.add(description);
			}

			if (isShiftKeyDown())
			{
				list.add(quest.getCompletionString(ClientQuestList.INSTANCE));
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
		}
	}

	public class ButtonCreateQuest extends Widget
	{
		public final QuestPosition pos;

		public ButtonCreateQuest(Panel panel, QuestPosition p)
		{
			super(panel);
			pos = p;
		}

		@Override
		public boolean isEnabled()
		{
			return isCtrlKeyDown();
		}

		@Override
		public boolean shouldDraw()
		{
			return isCtrlKeyDown() && isMouseOver();
		}

		@Override
		public boolean mousePressed(MouseButton button)
		{
			if (selectedChapter != null && isMouseOver())
			{
				GuiHelper.playClickSound();
				GuiSelectors.selectJson(new ConfigString(), (value, set) ->
				{
					if (set)
					{
						Quest quest = new Quest(selectedChapter.chapter, value.getString());
						ClientUtils.execClientCommand("/ftb edit_quests add_quest " + selectedChapter.chapter.getName() + " " + quest.id.getResourcePath() + " " + pos.x + " " + pos.y);
						ClientUtils.execClientCommand("/ftb edit_quests sync @p");
					}

					GuiQuestTree.this.openGui();
				});
				return true;
			}

			return false;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			list.add(GuiLang.ADD.translate() + " @ " + pos);
		}

		@Override
		public Icon getIcon()
		{
			return Color4I.WHITE.withAlpha(100);
		}
	}

	public final Map<Quest, ButtonQuest> questButtonMap;
	public int zoom = 16, scrollWidth, scrollHeight, prevMouseX, prevMouseY, grabbed;
	public ButtonChapter selectedChapter;
	public List<ButtonChapter> chapterButtons;
	public final Panel chapterPanel, quests;
	public final Button zoomIn, zoomOut;

	public GuiQuestTree()
	{
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

		for (QuestChapter chapter : ClientQuestList.INSTANCE.chapters.values())
		{
			ButtonChapter b = new ButtonChapter(chapterPanel, chapter);

			if (selectedChapter == null)
			{
				selectedChapter = b;
			}

			chapterButtons.add(b);
		}

		chapterPanel.addFlags(DEFAULTS);

		/*
		chaptersScrollBar = new PanelScrollBar(this, 14, chapterPanel)
		{
			@Override
			public boolean isEnabled()
			{
				return width > 0;
			}
		};*/

		questButtonMap = new HashMap<>();

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

					for (Quest quest : selectedChapter.chapter.quests.values())
					{
						ButtonQuest widget = new ButtonQuest(this, quest);
						questButtonMap.put(quest, widget);
						mx = Math.max(mx, quest.pos.x);
						my = Math.max(my, quest.pos.y);
						add(widget);
					}

					if (ClientQuestList.INSTANCE.editing)
					{
						HashSet<QuestPosition> set = new HashSet<>();

						for (int y = 0; y < my + 3; y++)
						{
							for (int x = 0; x < mx + 3; x++)
							{
								set.add(new QuestPosition(x, y));
							}
						}

						for (Quest quest : selectedChapter.chapter.quests.values())
						{
							set.remove(quest.pos);
						}

						for (QuestPosition pos : set)
						{
							add(new ButtonCreateQuest(this, pos));
						}
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
						QuestPosition pos;

						if (widget instanceof ButtonQuest)
						{
							pos = ((ButtonQuest) widget).quest.pos;
							mx = Math.max(mx, pos.x);
							my = Math.max(my, pos.y);
						}
						else
						{
							pos = ((ButtonCreateQuest) widget).pos;
						}

						widget.setPosAndSize(1 + pos.x * (zoom * 3 / 2), 1 + pos.y * (zoom * 3 / 2), zoom * 5 / 4, zoom * 5 / 4);
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

						for (QuestDependency dependency : buttonQuest.quest.dependencies)
						{
							if (dependency.quest != null && buttonQuest.quest.chapter.equals(dependency.chapter))
							{
								ButtonQuest b = questButtonMap.get(dependency.quest);

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

		quests.addFlags(DEFAULTS);

		zoomIn = new SimpleTextButton(this, "+", Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();

				if (zoom != 24)
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

				if (zoom != 4)
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

		if (selectedChapter != null)
		{
			if (getMouseWheel() > 0)
			{
				if (zoom != 24)
				{
					zoom += 4;
					grabbed = 0;
					quests.setScrollX(0);
					quests.setScrollY(0);
					getGui().alignWidgets();
				}
			}
			else if (getMouseWheel() < 0)
			{
				if (zoom != 4)
				{
					zoom -= 4;
					grabbed = 0;
					quests.setScrollX(0);
					quests.setScrollY(0);
					getGui().alignWidgets();
				}
			}

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