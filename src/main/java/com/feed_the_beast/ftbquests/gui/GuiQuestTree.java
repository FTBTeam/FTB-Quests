package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.ConfigValue;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.net.edit.MessageDeleteObject;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveChapter;
import com.feed_the_beast.ftbquests.net.edit.MessageResetProgress;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GuiQuestTree extends GuiBase
{
	public class ButtonChapter extends Button
	{
		public int index;
		public QuestChapter chapter;
		public List<String> description;

		public ButtonChapter(Panel panel, int idx, QuestChapter c)
		{
			super(panel, c.getDisplayName().getFormattedText(), c.getIcon());
			setSize(35, 26);
			index = idx;
			chapter = c;
			description = new ArrayList<>();

			for (ConfigValue v : chapter.description)
			{
				description.add(TextFormatting.GRAY + v.getString());
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
			super(panel, q.getDisplayName().getFormattedText(), q.getIcon());
			setSize(20, 20);
			quest = q;
			description = TextFormatting.GRAY + quest.description.getString();

			if (TextFormatting.getTextWithoutFormattingCodes(description).isEmpty())
			{
				description = "";
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();

			if (questList.editingMode && isCtrlKeyDown())
			{
				if (button.isRight())
				{
					getGui().openYesNo(I18n.format("delete_item", quest.getDisplayName().getFormattedText()), "", () -> new MessageDeleteObject(quest.id).sendToServer());
				}
				else
				{
					new MessageEditObject(quest.id).sendToServer();
				}
			}
			else
			{
				questList.questGui = new GuiQuest(GuiQuestTree.this, quest);
				questList.questGui.openGui();
			}
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

	public abstract class ChapterOptionButton extends SimpleTextButton
	{
		public final String hover;

		public ChapterOptionButton(Panel panel, String text, String h)
		{
			super(panel, text, Icon.EMPTY);
			setSize(12, 12);
			hover = h;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			list.add(hover);
		}

		@Override
		public void draw()
		{
			int ax = getAX();
			int ay = getAY();
			getButtonBackground().draw(ax, ay, width, height);
			drawString(getTitle(), ax + 3, ay + 2, SHADOW);
		}
	}

	public final ClientQuestList questList;
	private final Int2ObjectOpenHashMap<ButtonQuest> questButtonMap;
	public int zoom = 16;
	private int scrollWidth, scrollHeight, prevMouseX, prevMouseY, grabbed;
	public ButtonChapter selectedChapter;
	public List<ButtonChapter> chapterButtons;
	public final Panel chapterPanel, quests;
	public final List<ChapterOptionButton> chapterOptionButtons;
	private final IntOpenHashSet selectedQuests;

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

		for (int i = 0; i < questList.chapters.size(); i++)
		{
			ButtonChapter b = new ButtonChapter(chapterPanel, i, questList.chapters.get(i));

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
						mx = Math.max(mx, quest.x.getInt());
						my = Math.max(my, quest.y.getInt());
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
						mx = Math.max(mx, quest.x.getInt());
						my = Math.max(my, quest.y.getInt());
						widget.setPosAndSize(1 + quest.x.getInt() * (zoom * 3 / 2), 1 + quest.y.getInt() * (zoom * 3 / 2), zoom * 5 / 4, zoom * 5 / 4);
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

						for (ConfigValue value : buttonQuest.quest.dependencies)
						{
							QuestObject dependency = questList.get(value.getInt());

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

		chapterOptionButtons = new ArrayList<>();
		selectedQuests = new IntOpenHashSet();
	}

	@Override
	public void addWidgets()
	{
		add(chapterPanel);
		add(quests);

		chapterOptionButtons.clear();

		chapterOptionButtons.add(new ChapterOptionButton(this, "+", I18n.format("ftbquests.gui.zoom_in"))
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
		});

		chapterOptionButtons.add(new ChapterOptionButton(this, "-", I18n.format("ftbquests.gui.zoom_out"))
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
		});

		if (questList.editingMode)
		{
			if (selectedChapter != null)
			{
				chapterOptionButtons.add(new ChapterOptionButton(this, "X", I18n.format("ftbquests.gui.delete_chapter"))
				{
					@Override
					public void onClicked(MouseButton button)
					{
						GuiHelper.playClickSound();
						getGui().openYesNo(I18n.format("delete_item", selectedChapter.chapter.getDisplayName().getFormattedText()), "", () -> new MessageDeleteObject(selectedChapter.chapter.id).sendToServer());
					}
				});

				chapterOptionButtons.add(new ChapterOptionButton(this, "E", I18n.format("ftbquests.gui.edit_chapter"))
				{
					@Override
					public void onClicked(MouseButton button)
					{
						GuiHelper.playClickSound();
						new MessageEditObject(selectedChapter.chapter.id).sendToServer();
					}
				});

				chapterOptionButtons.add(new ChapterOptionButton(this, "R", I18n.format("ftbquests.gui.reset_progress"))
				{
					@Override
					public void onClicked(MouseButton button)
					{
						GuiHelper.playClickSound();
						getGui().openYesNo(I18n.format("ftbquests.gui.reset_progress") + "?", "", () -> new MessageResetProgress(selectedChapter.chapter.id, isShiftKeyDown()).sendToServer());
					}
				});
			}

			chapterOptionButtons.add(new ChapterOptionButton(this, "A", I18n.format("ftbquests.gui.add_chapter"))
			{
				@Override
				public void onClicked(MouseButton button)
				{
					GuiHelper.playClickSound();

					new GuiEditConfigValue("title", new ConfigString(), (value, set) ->
					{
						GuiQuestTree.this.openGui();

						if (set)
						{
							NBTTagCompound nbt = new NBTTagCompound();
							nbt.setString("title", value.getString());
							new MessageCreateObject(QuestObjectType.CHAPTER, 0, nbt).sendToServer();
						}
					}).openGui();
				}
			});

			if (selectedChapter != null)
			{
				chapterOptionButtons.add(new ChapterOptionButton(this, "Q", I18n.format("ftbquests.gui.add_quest"))
				{
					@Override
					public void onClicked(MouseButton button)
					{
						GuiHelper.playClickSound();
						int x = 0;
						int y = 0;

						IntOpenHashSet set = new IntOpenHashSet();

						for (Quest quest : selectedChapter.chapter.quests)
						{
							set.add((quest.x.getInt() & 0xFF) | (quest.y.getInt() & 0xFF) >> 8);
						}

						if (set.size() >= 255 * 255)
						{
							return;
						}

						while (set.contains((x & 0xFF) | (y & 0xFF) >> 8))
						{
							x++;

							if (x == 128)
							{
								x = -127;
								y++;

								if (y == 128)
								{
									y = -127;
								}
							}
						}

						NBTTagCompound nbt = new NBTTagCompound();
						nbt.setByte("x", (byte) x);
						nbt.setByte("y", (byte) y);
						Quest quest = new Quest(selectedChapter.chapter, nbt);
						ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
						quest.getConfig(group.getGroup(QuestObjectType.QUEST.getName()));
						new GuiEditConfig(group, (group1, sender) -> {
							NBTTagCompound nbt1 = new NBTTagCompound();
							quest.writeData(nbt1);
							new MessageCreateObject(QuestObjectType.QUEST, selectedChapter.chapter.id, nbt1).sendToServer();
						}).openGui();
					}
				});

				chapterOptionButtons.add(new ChapterOptionButton(this, "^", I18n.format("ftbquests.gui.move_up"))
				{
					@Override
					public WidgetType getWidgetType()
					{
						return selectedChapter.index > 0 ? super.getWidgetType() : WidgetType.DISABLED;
					}

					@Override
					public void onClicked(MouseButton button)
					{
						GuiHelper.playClickSound();
						new MessageMoveChapter(selectedChapter.chapter.id, true).sendToServer();
					}
				});

				chapterOptionButtons.add(new ChapterOptionButton(this, "v", I18n.format("ftbquests.gui.move_down"))
				{
					@Override
					public WidgetType getWidgetType()
					{
						return selectedChapter.index < questList.chapters.size() - 1 ? super.getWidgetType() : WidgetType.DISABLED;
					}

					@Override
					public void onClicked(MouseButton button)
					{
						GuiHelper.playClickSound();
						new MessageMoveChapter(selectedChapter.chapter.id, false).sendToServer();
					}
				});
			}
		}

		addAll(chapterOptionButtons);
	}

	@Override
	public void alignWidgets()
	{
		for (int i = 0; i < chapterOptionButtons.size(); i++)
		{
			chapterOptionButtons.get(i).setPos(width - 2 - (i + 1) * 17, 7);
		}

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