package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.ConfigValue;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
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
import com.feed_the_beast.ftbquests.net.edit.MessageMoveQuest;
import com.feed_the_beast.ftbquests.net.edit.MessageResetProgress;
import com.feed_the_beast.ftbquests.net.edit.MessageSetDep;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
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
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

public class GuiQuestTree extends GuiBase
{
	public static int zoom = 16;

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
			selectChapter(chapter);

			if (questFile.editingMode && button.isRight())
			{
				List<ContextMenuItem> contextMenu = new ArrayList<>();
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.add_quest"), GuiIcons.ADD, GuiQuestTree.this::addQuest));
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.add_chapter"), GuiIcons.ADD, GuiQuestTree.this::addChapter));
				contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), GuiIcons.SETTINGS, () -> new MessageEditObject(chapter.id).sendToServer()));
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.move_up"), GuiIcons.UP, () -> new MessageMoveChapter(chapter.id, true).sendToServer()).setEnabled(chapter.index > 0));
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.move_down"), GuiIcons.DOWN, () -> new MessageMoveChapter(chapter.id, false).sendToServer()).setEnabled(chapter.index < questFile.chapters.size() - 1));
				contextMenu.add(ContextMenuItem.SEPARATOR);
				contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> new MessageDeleteObject(chapter.id).sendToServer()).setYesNo(I18n.format("delete_item", chapter.getDisplayName().getFormattedText())));
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> new MessageResetProgress(chapter.id, false).sendToServer()).setYesNo(I18n.format("ftbquests.gui.reset_progress_q")));
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.copy_id"), GuiIcons.INFO, () -> setClipboardString(QuestFile.formatID(chapter.id))));
				getGui().openContextMenu(contextMenu);
			}
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			list.add(getTitle() + questFile.getCompletionSuffix(chapter));
			list.addAll(description);
		}

		@Override
		public void draw()
		{
			int ax = getAX();
			int ay = getAY();

			boolean selected = chapter.equals(selectedChapter);
			getTheme().getHorizontalTab(selected).draw(selected ? ax : ax - 1, ay, width, height);

			if (!icon.isEmpty())
			{
				icon.draw(ax + 10, ay + (height - 16) / 2, 16, 16);
			}

			int r = 0;

			for (Quest quest : chapter.quests)
			{
				if (quest.isComplete(questFile))
				{
					for (QuestReward reward : quest.rewards)
					{
						if (!questFile.isRewardClaimed(ClientUtils.MC.player, reward))
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
			else if (chapter.isComplete(questFile))
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 500);
				GuiIcons.CHECK.draw(ax + width - 14, ay + 4, 8, 8);
				GlStateManager.popMatrix();
			}
		}
	}

	public interface XYPosition
	{
		byte getX();

		byte getY();
	}

	public class ButtonQuest extends Button
	{
		public Quest quest;
		public String description;
		public List<ButtonQuest> dependencies = null;

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

		public List<ButtonQuest> getDependencies()
		{
			if (dependencies == null)
			{
				dependencies = new ArrayList<>();

				for (ConfigValue value : quest.dependencies)
				{
					Quest q = questFile.getQuest((short) value.getInt());

					if (q != null && q != quest)
					{
						for (Widget widget : quests.widgets)
						{
							if (widget instanceof ButtonQuest && q == ((ButtonQuest) widget).quest)
							{
								dependencies.add((ButtonQuest) widget);
							}
						}
					}
				}

				dependencies = dependencies.isEmpty() ? Collections.emptyList() : dependencies;
			}

			return dependencies;
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();

			if (questFile.editingMode && button.isRight())
			{
				List<ContextMenuItem> contextMenu = new ArrayList<>();
				contextMenu.add(new ContextMenuItem(I18n.format("selectWorld.edit"), GuiIcons.SETTINGS, () -> new MessageEditObject(quest.id).sendToServer()));
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.move"), GuiIcons.RIGHT, () ->
				{
					//ButtonQuest
				}));

				QuestObject object = questFile.get(selectedQuest);

				if (object != null && quest.hasDependency(object.id))
				{
					contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.rem_dep"), GuiIcons.REMOVE, () -> new MessageSetDep(quest.id, selectedQuest, false).sendToServer()));
				}
				else
				{
					contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.set_dep"), GuiIcons.ADD, () -> new MessageSetDep(quest.id, selectedQuest, true).sendToServer()).setEnabled(object != quest && object instanceof Quest));
				}

				contextMenu.add(ContextMenuItem.SEPARATOR);
				contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> new MessageDeleteObject(quest.id).sendToServer()).setYesNo(I18n.format("delete_item", quest.getDisplayName().getFormattedText())));
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> new MessageResetProgress(quest.id, false).sendToServer()).setYesNo(I18n.format("ftbquests.gui.reset_progress_q")));
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.copy_id"), GuiIcons.INFO, () -> setClipboardString(QuestFile.formatID(quest.id))));
				getGui().openContextMenu(contextMenu);
			}
			else if (questFile.editingMode && button.isLeft() && isCtrlKeyDown())
			{
				if (selectedQuest == quest.id)
				{
					selectedQuest = 0;
				}
				else
				{
					selectedQuest = quest.id;
				}
			}
			else if (button.isLeft())
			{
				questFile.questGui = new GuiQuest(GuiQuestTree.this, quest);
				questFile.questGui.openGui();
			}
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			list.add(getTitle() + questFile.getCompletionSuffix(quest));

			if (!description.isEmpty())
			{
				list.add(description);
			}
		}

		@Override
		public WidgetType getWidgetType()
		{
			if (selectedQuest == quest.id)
			{
				return WidgetType.MOUSE_OVER;
			}

			return quest.isVisible(questFile) ? WidgetType.mouseOver(isMouseOver()) : WidgetType.DISABLED;
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

			if (quest.isComplete(questFile))
			{
				for (QuestReward reward : quest.rewards)
				{
					if (!questFile.isRewardClaimed(ClientUtils.MC.player, reward))
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
			else if (quest.isComplete(questFile))
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 500);
				GuiIcons.CHECK.draw(ax + width - 1 - zoom / 2, ay + 1, zoom / 2, zoom / 2);
				GlStateManager.popMatrix();
			}
		}
	}

	public static boolean dummyButtonsVisible = true;

	public class ButtonDummyQuest extends Widget implements Runnable
	{
		public final byte x, y;

		public ButtonDummyQuest(Panel panel, byte _x, byte _y)
		{
			super(panel);
			setSize(20, 20);
			x = _x;
			y = _y;
		}

		@Override
		public boolean mousePressed(MouseButton button)
		{
			if (isMouseOver() && button.isRight())
			{
				GuiHelper.playClickSound();
				List<ContextMenuItem> contextMenu = new ArrayList<>();
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.add_quest"), GuiIcons.ADD, this));
				getGui().openContextMenu(contextMenu);
				return true;
			}

			return false;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
		}

		@Override
		public void draw()
		{
			if (dummyButtonsVisible)
			{
				int ax = getAX();
				int ay = getAY();
				Color4I.WHITE.withAlpha(30).draw(ax, ay, width, height);
			}
		}

		@Override
		public void run()
		{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setByte("x", x);
			nbt.setByte("y", y);
			Quest quest = new Quest(selectedChapter, nbt);
			ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
			quest.getConfig(group.getGroup(QuestObjectType.QUEST.getName()));
			new GuiEditConfig(group, (group1, sender) -> {
				NBTTagCompound nbt1 = new NBTTagCompound();
				quest.writeData(nbt1);
				new MessageCreateObject(QuestObjectType.QUEST, selectedChapter.id, nbt1).sendToServer();
			}).openGui();
		}
	}

	public class ChapterOptionButton extends SimpleTextButton
	{
		private final String hover;
		private final Runnable callback;
		private final BooleanSupplier typeCallback;

		public ChapterOptionButton(Panel panel, String text, String h, Runnable c, BooleanSupplier t)
		{
			super(panel, text, Icon.EMPTY);
			setSize(12, 12);
			hover = h;
			callback = c;
			typeCallback = t;
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

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			callback.run();
		}

		@Override
		public WidgetType getWidgetType()
		{
			return typeCallback.getAsBoolean() ? super.getWidgetType() : WidgetType.DISABLED;
		}
	}

	public final ClientQuestFile questFile;
	private int scrollWidth, scrollHeight, prevMouseX, prevMouseY, grabbed;
	public QuestChapter selectedChapter;
	private short selectedQuest;
	public final Panel chapterPanel, quests, chapterOptionButtons;

	public GuiQuestTree(ClientQuestFile q)
	{
		questFile = q;

		chapterPanel = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				for (int i = 0; i < questFile.chapters.size(); i++)
				{
					add(new ButtonChapter(this, i, questFile.chapters.get(i)));
				}
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

		selectedChapter = questFile.chapters.isEmpty() ? null : questFile.chapters.get(0);

		/*
		chaptersScrollBar = new PanelScrollBar(this, 14, chapterPanel)
		{
			@Override
			public boolean isEnabled()
			{
				return width > 0;
			}
		};*/

		quests = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				if (selectedChapter == null)
				{
					return;
				}

				if (!questFile.editingMode)
				{
					for (Quest quest : selectedChapter.quests)
					{
						add(new ButtonQuest(this, quest));
					}

					return;
				}

				Int2ObjectOpenHashMap<Widget> buttons = new Int2ObjectOpenHashMap<>();

				for (Quest quest : selectedChapter.quests)
				{
					for (int x = -4; x <= 4; x++)
					{
						for (int y = -4; y <= 4; y++)
						{
							if (x == 0 && y == 0)
							{
								continue;
							}

							int ax = quest.x.getInt() + x;
							int ay = quest.y.getInt() + y;

							if (ax <= Quest.POS_LIMIT && ax >= -Quest.POS_LIMIT && ay <= Quest.POS_LIMIT && ay >= -Quest.POS_LIMIT)
							{
								buttons.put(((ax & 0xFF) << 8) | (ay & 0xFF), new ButtonDummyQuest(this, (byte) ax, (byte) ay));
							}
						}
					}
				}

				for (Quest quest : selectedChapter.quests)
				{
					buttons.put(((quest.x.getInt() & 0xFF) << 8) | (quest.y.getInt() & 0xFF), new ButtonQuest(this, quest));
				}

				addAll(buttons.values());
			}

			@Override
			public void alignWidgets()
			{
				scrollWidth = 0;
				scrollHeight = 0;

				int minX = Quest.POS_LIMIT + 1, minY = Quest.POS_LIMIT + 1, maxX = -(Quest.POS_LIMIT + 1), maxY = -(Quest.POS_LIMIT + 1);

				for (Widget widget : widgets)
				{
					int x, y;

					if (widget instanceof ButtonQuest)
					{
						Quest quest = ((ButtonQuest) widget).quest;
						x = quest.x.getInt();
						y = quest.y.getInt();
					}
					else
					{
						ButtonDummyQuest button = (ButtonDummyQuest) widget;
						x = button.x;
						y = button.y;
					}

					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				}

				if (!questFile.editingMode)
				{
					minX -= 4;
					minY -= 4;
					maxX += 4;
					maxY += 4;
				}

				int bsize = zoom * 5 / 4; //16 * 5 / 4 = 20
				int bspace = zoom * 3 / 2; //16 * 3 / 2 = 24
				int offset = bspace - bsize;

				scrollWidth = offset + (maxX - minX + 1) * bspace;
				scrollHeight = offset + (maxY - minY + 1) * bspace;

				for (Widget widget : widgets)
				{
					int x, y;

					if (widget instanceof ButtonQuest)
					{
						Quest quest = ((ButtonQuest) widget).quest;
						x = quest.x.getInt();
						y = quest.y.getInt();
					}
					else
					{
						ButtonDummyQuest button = (ButtonDummyQuest) widget;
						x = button.x;
						y = button.y;
					}

					widget.setPosAndSize(offset + (x - minX) * bspace, offset + (y - minY) * bspace, bsize, bsize);
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
						for (ButtonQuest b : ((ButtonQuest) widget).getDependencies())
						{
							buffer.pos(widget.getAX() + widget.width / 2D, widget.getAY() + widget.height / 2D, 0).color(100, 200, 100, 255).endVertex();
							buffer.pos(b.getAX() + b.width / 2D, b.getAY() + b.height / 2D, 0).color(50, 50, 50, 255).endVertex();
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
			public void addMouseOverText(List<String> list)
			{
				super.addMouseOverText(list);

				setOffset(true);

				int bsize = zoom * 5 / 4; //16 * 5 / 4 = 20
				int bspace = zoom * 3 / 2; //16 * 3 / 2 = 24
				int offset = bspace - bsize;

				int mx = getMouseX() - getAX() - offset / 2;
				int my = getMouseY() - getAY() - offset / 2;

				if (mx >= 0 && my >= 0 && mx < width && my < height)
				{
					list.add((mx / bspace) + " ; " + (my / bspace));
				}

				setOffset(false);
			}

			@Override
			public boolean mousePressed(MouseButton button)
			{
				boolean b = super.mousePressed(button);

				if (!b)
				{
					if (button.isLeft())
					{
						prevMouseX = getMouseX();
						prevMouseY = getMouseY();
						grabbed = 1;
					}
					else if (button.isRight() && questFile.editingMode)
					{
					}
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

		chapterOptionButtons = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				if (questFile.editingMode)
				{
					if (chapterPanel.widgets.isEmpty())
					{
						add(new ChapterOptionButton(this, "C", I18n.format("ftbquests.gui.add_chapter"), () -> addChapter(), () -> true));
					}

					add(new ChapterOptionButton(this, "D", I18n.format("ftbquests.gui.dummy_buttons"), () -> dummyButtonsVisible = !dummyButtonsVisible, () -> true));
				}

				add(new ChapterOptionButton(this, "-", I18n.format("ftbquests.gui.zoom_out"), () -> zoomOut(), () -> canZoomOut()));
				add(new ChapterOptionButton(this, "+", I18n.format("ftbquests.gui.zoom_in"), () -> zoomIn(), () -> canZoomIn()));
			}

			@Override
			public void alignWidgets()
			{
				setWidth(align(new WidgetLayout.Horizontal(0, 4, 0)));
				setPosAndSize(getGui().width - width - 7, 7, width, 12);
			}
		};
	}

	public void selectChapter(@Nullable QuestChapter chapter)
	{
		if (selectedChapter != chapter)
		{
			selectedChapter = chapter;
			quests.setScrollX(0);
			quests.setScrollY(0);
			selectedQuest = 0;
			quests.refreshWidgets();
			chapterOptionButtons.refreshWidgets();
		}
	}

	private boolean canZoomIn()
	{
		return zoom < 28;
	}

	private boolean canZoomOut()
	{
		return zoom > 4;
	}

	public void resetScroll(boolean realign)
	{
		if (realign)
		{
			quests.alignWidgets();
		}

		quests.setScrollX((scrollWidth - quests.width) / 2);
		quests.setScrollY((scrollHeight - quests.height) / 2);
	}

	private void zoomIn()
	{
		zoom += 4;
		grabbed = 0;
		resetScroll(true);
	}

	private void zoomOut()
	{
		zoom -= 4;
		grabbed = 0;
		resetScroll(true);
	}

	private void moveObject(int direction)
	{
		Quest quest = questFile.getQuest(selectedQuest);

		if (quest != null)
		{
			quest.move((byte) direction);
			new MessageMoveQuest(quest.id, (byte) direction).sendToServer();
		}
		else if (direction == 0)
		{
			new MessageMoveChapter(selectedChapter.id, true).sendToServer();
		}
		else if (direction == 4)
		{
			new MessageMoveChapter(selectedChapter.id, false).sendToServer();
		}
	}

	private void addChapter()
	{
		new GuiEditConfigValue("title", new ConfigString(), (value, set) ->
		{
			GuiQuestTree.this.openGui();

			if (set)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("title", value.getString());
				new MessageCreateObject(QuestObjectType.CHAPTER, (short) 0, nbt).sendToServer();
			}
		}).openGui();
	}

	private void addQuest()
	{
		int x = 0;
		int y = 0;

		IntOpenHashSet set = new IntOpenHashSet();

		for (Quest quest : selectedChapter.quests)
		{
			set.add((quest.x.getInt() & 0xFF) | (quest.y.getInt() & 0xFF) >> 8);
		}

		if (set.size() >= (Quest.POS_LIMIT * 2 + 1) * (Quest.POS_LIMIT * 2 + 1))
		{
			return;
		}

		while (set.contains((x & 0xFF) | (y & 0xFF) >> 8))
		{
			x++;

			if (x == Quest.POS_LIMIT + 1)
			{
				x = -Quest.POS_LIMIT;
				y++;

				if (y == Quest.POS_LIMIT + 1)
				{
					y = -Quest.POS_LIMIT;
				}
			}
		}

		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByte("x", (byte) x);
		nbt.setByte("y", (byte) y);
		Quest quest = new Quest(selectedChapter, nbt);
		ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
		quest.getConfig(group.getGroup(QuestObjectType.QUEST.getName()));
		new GuiEditConfig(group, (group1, sender) -> {
			NBTTagCompound nbt1 = new NBTTagCompound();
			quest.writeData(nbt1);
			new MessageCreateObject(QuestObjectType.QUEST, selectedChapter.id, nbt1).sendToServer();
		}).openGui();
	}

	@Override
	public boolean keyPressed(int key, char keyChar)
	{
		if (super.keyPressed(key, keyChar))
		{
			return true;
		}
		else if (keyChar == '+' || key == Keyboard.KEY_EQUALS)
		{
			if (canZoomIn())
			{
				zoomIn();
				return true;
			}
		}
		else if (keyChar == '-' || key == Keyboard.KEY_MINUS)
		{
			if (canZoomOut())
			{
				zoomOut();
				return true;
			}
		}
		else if (key == Keyboard.KEY_TAB)
		{
			if (selectedChapter != null && !questFile.chapters.isEmpty())
			{
				selectChapter(questFile.chapters.get((selectedChapter.index + 1) % questFile.chapters.size()));
			}
		}
		else if (keyChar >= '1' && keyChar <= '9')
		{
			int i = keyChar - '1';

			if (i < questFile.chapters.size())
			{
				selectChapter(questFile.chapters.get(i));
			}
		}
		else if (selectedChapter != null && questFile.editingMode && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown())
		{
			switch (key)
			{
				case Keyboard.KEY_D:
					selectedQuest = 0;
					break;
				case Keyboard.KEY_UP:
					moveObject(0);
					break;
				case Keyboard.KEY_DOWN:
					moveObject(4);
					break;
				case Keyboard.KEY_LEFT:
					moveObject(6);
					break;
				case Keyboard.KEY_RIGHT:
					moveObject(2);
					break;
			}
		}

		return false;
	}

	@Override
	public void addWidgets()
	{
		add(chapterPanel);
		add(quests);
		add(chapterOptionButtons);
	}

	@Override
	public void alignWidgets()
	{
		chapterOptionButtons.alignWidgets();
		chapterPanel.alignWidgets();
		resetScroll(true);
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
		Keyboard.enableRepeatEvents(true);
		return true;
	}

	@Override
	public void onClosed()
	{
		super.onClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void drawBackground()
	{
		if (selectedChapter != null && selectedChapter.isInvalid())
		{
			selectChapter(null);
		}

		if (selectedChapter == null && !questFile.chapters.isEmpty())
		{
			selectChapter(questFile.chapters.get(0));
		}

		super.drawBackground();

		if (grabbed != 0)
		{
			int x = getMouseX();
			int y = getMouseY();

			if (scrollWidth > quests.width)
			{
				quests.setScrollX(Math.max(Math.min(quests.getScrollX() + (prevMouseX - x), scrollWidth - quests.width), 0));
			}
			else
			{
				quests.setScrollX((scrollWidth - quests.width) / 2);
			}

			if (scrollHeight > quests.height)
			{
				quests.setScrollY(Math.max(Math.min(quests.getScrollY() + (prevMouseY - y), scrollHeight - quests.height), 0));
			}
			else
			{
				quests.setScrollY((scrollHeight - quests.height) / 2);
			}

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
				if (canZoomIn())
				{
					zoomIn();
					return true;
				}
			}
			else if (scroll < 0)
			{
				if (canZoomOut())
				{
					zoomOut();
					return true;
				}
			}
		}

		return super.mouseScrolled(scroll);
	}

	@Override
	public void drawForeground()
	{
		Widget widget = selectedChapter == null ? null : chapterPanel.getWidget(selectedChapter.index);

		if (widget instanceof ButtonChapter)
		{
			drawString(widget.getTitle(), getAX() + 8, getAY() + 8, getTheme().getInvertedContentColor(), 0);
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