package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.FTBLibConfig;
import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.client.ClientQuestProgress;
import com.feed_the_beast.ftbquests.net.MessageCompleteInstantly;
import com.feed_the_beast.ftbquests.net.MessageResetProgress;
import com.feed_the_beast.ftbquests.net.edit.MessageChangeID;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.net.edit.MessageDeleteObject;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveChapter;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveQuest;
import com.feed_the_beast.ftbquests.net.edit.MessageSetDep;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.QuestReward;
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
import java.util.UUID;
import java.util.regex.Pattern;

public class GuiQuestTree extends GuiBase
{
	public static int zoom = 24;

	public abstract class ButtonTab extends Button
	{
		public ButtonTab(Panel panel, String title, Icon icon)
		{
			super(panel, title, icon);
			setSize(20, chapterPanel.height);
		}
	}

	public class ButtonChapter extends ButtonTab
	{
		public int index;
		public QuestChapter chapter;
		public List<String> description;

		public ButtonChapter(Panel panel, int idx, QuestChapter c)
		{
			super(panel, c.getDisplayName().getFormattedText(), c.getIcon());
			index = idx;
			chapter = c;
			description = new ArrayList<>();

			for (String v : chapter.description)
			{
				description.add(TextFormatting.GRAY + v);
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			selectChapter(chapter);

			if (questFile.canEdit() && button.isRight())
			{
				List<ContextMenuItem> contextMenu = new ArrayList<>();
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.move"), GuiIcons.LEFT, () -> new MessageMoveChapter(chapter.getID(), true).sendToServer()).setEnabled(chapter.index > 0));
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.move"), GuiIcons.RIGHT, () -> new MessageMoveChapter(chapter.getID(), false).sendToServer()).setEnabled(chapter.index < questFile.chapters.size() - 1));
				contextMenu.add(ContextMenuItem.SEPARATOR);
				addObjectMenuItems(contextMenu, getGui(), chapter);
				getGui().openContextMenu(contextMenu);
			}
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			list.add(getTitle() + ClientQuestProgress.getCompletionSuffix(questFile.self, chapter));
			list.addAll(description);
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			if (selectedChapter != chapter)
			{
				borderColor.draw(x, y + h - 1, w + 1, 1);
				backgroundColor.draw(x, y + 1, w, h - 2);
			}
			else
			{
				borderColor.draw(x + w, y + h - 1, 1, 1);
			}

			borderColor.draw(x + w, y + 1, 1, h - 2);
			icon.draw(x + (w - 16) / 2, y + (h - 16) / 2, 16, 16);

			if (isMouseOver())
			{
				backgroundColor.draw(x, y + 1, w, h - (selectedChapter == chapter ? 1 : 2));
			}

			if (questFile.self == null)
			{
				return;
			}

			int r = 0;
			UUID playerId = ClientUtils.MC.player.getUniqueID();

			for (Quest quest : chapter.quests)
			{
				if (quest.isComplete(questFile.self))
				{
					for (QuestReward reward : quest.rewards)
					{
						if (!questFile.self.isRewardClaimed(playerId, reward))
						{
							r++;
						}
					}
				}
			}

			if (r > 0)
			{
				String s = Integer.toString(r);
				int nw = theme.getStringWidth(s);
				GlStateManager.pushMatrix();
				GlStateManager.translate(x + w - nw, y + 2, 500);
				theme.drawString(s, -1, 0, Color4I.LIGHT_RED, 0);
				theme.drawString(s, 1, 0, Color4I.LIGHT_RED, 0);
				theme.drawString(s, 0, -1, Color4I.LIGHT_RED, 0);
				theme.drawString(s, 0, 1, Color4I.LIGHT_RED, 0);
				theme.drawString(s, 0, 0, Color4I.WHITE, 0);
				GlStateManager.popMatrix();
			}
			else if (chapter.isComplete(questFile.self))
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 500);
				QuestsTheme.COMPLETED.draw(x + w - 10, y + 2, 8, 8);
				GlStateManager.popMatrix();
			}
		}
	}

	public class ButtonAddChapter extends ButtonTab
	{
		public ButtonAddChapter(Panel panel)
		{
			super(panel, I18n.format("gui.add"), QuestsTheme.ADD);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();

			new GuiEditConfigValue("title", new ConfigString("", Pattern.compile("^.+$")), (value, set) ->
			{
				GuiQuestTree.this.openGui();

				if (set)
				{
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("title", value.getString());
					new MessageCreateObject(QuestObjectType.CHAPTER, "", nbt).sendToServer();
				}
			}).openGui();
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			borderColor.draw(x, y + h - 1, w + 1, 1);
			backgroundColor.draw(x, y + 1, w, h - 2);

			borderColor.draw(x + w, y + 1, 1, h - 2);
			icon.draw(x + (w - 16) / 2, y + (h - 16) / 2, 16, 16);

			if (isMouseOver())
			{
				backgroundColor.draw(x, y + 1, w, h - 2);
			}
		}
	}

	public class ButtonOtherOptions extends ButtonTab
	{
		public ButtonOtherOptions(Panel panel)
		{
			super(panel, "", Icon.EMPTY);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();

			if (getGui().contextMenu != null)
			{
				getGui().openContextMenu((Panel) null);
				return;
			}

			List<ContextMenuItem> contextMenu = new ArrayList<>();

			if (questFile.canEdit())
			{
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.edit_file"), GuiIcons.SETTINGS, () -> new MessageEditObject(questFile.getID()).sendToServer()));
			}

			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.file.emergency_items"), GuiIcons.LOCK_OPEN, () -> new GuiEmergencyItems().openGui()).setEnabled(!questFile.emergencyItems.isEmpty() && (questFile.self != null || questFile.canEdit())));

			if (questFile.canEdit())
			{
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> new MessageResetProgress(questFile.getID()).sendToServer()).setYesNo(I18n.format("ftbquests.gui.reset_progress_q")));
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.complete_instantly"), QuestsTheme.COMPLETED, () -> new MessageCompleteInstantly(questFile.getID()).sendToServer()).setYesNo(I18n.format("ftbquests.gui.complete_instantly_q")));
			}

			contextMenu.add(new ContextMenuItem("Wiki", GuiIcons.INFO, () -> handleClick("https://minecraft.curseforge.com/projects/ftb-quests/pages")));

			if (FTBLibConfig.debugging.gui_widget_bounds)
			{
				contextMenu.add(new ContextMenuItem("Reload GUI", GuiIcons.REFRESH, () -> questFile.refreshGui(questFile)));
			}

			Panel panel = getGui().openContextMenu(contextMenu);
			panel.setPos(getGui().width - panel.width - 2, height + 1);
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			borderColor.draw(x - 1, y + h - 1, w + 1, 1);
			borderColor.draw(x - 1, y + 1, 1, h - 2);
			backgroundColor.draw(x, y + 1, w, h - 2);

			int x1 = x + w / 2 - 1;
			int y1 = y + h / 2 - 1;
			borderColor.draw(x1, y1, 2, 2);
			borderColor.draw(x1 - 5, y1, 2, 2);
			borderColor.draw(x1 + 5, y1, 2, 2);

			if (isMouseOver())
			{
				backgroundColor.draw(x, y + 1, w, h - 2);
			}
		}
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
			description = TextFormatting.GRAY + quest.description;

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

				for (String value : quest.dependencies)
				{
					Quest q = questFile.getQuest(value);

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

			if (questFile.canEdit() && button.isRight())
			{
				List<ContextMenuItem> contextMenu = new ArrayList<>();
				/*contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.move"), GuiIcons.RIGHT, () ->
				{
					//ButtonQuest
				}));*/

				QuestObject object = questFile.get(selectedQuest);

				if (object != null && quest.hasDependency(object))
				{
					contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.rem_dep"), GuiIcons.REMOVE, () -> new MessageSetDep(quest.getID(), selectedQuest, false).sendToServer()));
				}
				else
				{
					contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.set_dep"), QuestsTheme.ADD, () -> new MessageSetDep(quest.getID(), selectedQuest, true).sendToServer()).setEnabled(object != quest && object instanceof Quest));
				}

				contextMenu.add(ContextMenuItem.SEPARATOR);
				addObjectMenuItems(contextMenu, getGui(), quest);
				getGui().openContextMenu(contextMenu);
			}
			else if (questFile.canEdit() && button.isLeft() && isCtrlKeyDown())
			{
				if (selectedQuest.equals(quest.getID()))
				{
					selectedQuest = "";
				}
				else
				{
					selectedQuest = quest.getID();
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
			list.add(getTitle() + ClientQuestProgress.getCompletionSuffix(questFile.self, quest));

			if (!description.isEmpty())
			{
				list.add(description);
			}
		}

		@Override
		public WidgetType getWidgetType()
		{
			if (selectedQuest.equals(quest.getID()))
			{
				return WidgetType.MOUSE_OVER;
			}

			return questFile.editingMode || quest.isVisible(questFile.self) ? super.getWidgetType() : WidgetType.DISABLED;
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
			QuestsTheme.BUTTON.draw(x - zoom / 6, y - zoom / 6, w + zoom / 3, h + zoom / 3);

			if (isMouseOver())
			{
				QuestsTheme.BUTTON.draw(x - zoom / 6, y - zoom / 6, w + zoom / 3, h + zoom / 3);
			}

			if (!selectedQuest.isEmpty() && selectedQuest.equals(quest.getID()) && System.currentTimeMillis() % 900L >= 450L)
			{
				QuestsTheme.BUTTON.draw(x - zoom / 6, y - zoom / 6, w + zoom / 3, h + zoom / 3);
			}

			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

			if (!icon.isEmpty())
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(x + (width - zoom) / 2F, y + (h - zoom) / 2F, 0F);
				icon.draw(0, 0, zoom, zoom);
				GlStateManager.popMatrix();
			}

			if (questFile.self != null && quest.isComplete(questFile.self))
			{
				int r = 0;
				UUID playerId = ClientUtils.MC.player.getUniqueID();

				for (QuestReward reward : quest.rewards)
				{
					if (!questFile.self.isRewardClaimed(playerId, reward))
					{
						r++;
					}
				}

				if (r > 0)
				{
					String s = Integer.toString(r);
					int nw = theme.getStringWidth(s);
					GlStateManager.pushMatrix();
					GlStateManager.translate(x + width - nw - zoom / 8F, y + zoom / 8F, 500);
					theme.drawString(s, -1, 0, Color4I.LIGHT_RED, 0);
					theme.drawString(s, 1, 0, Color4I.LIGHT_RED, 0);
					theme.drawString(s, 0, -1, Color4I.LIGHT_RED, 0);
					theme.drawString(s, 0, 1, Color4I.LIGHT_RED, 0);
					theme.drawString(s, 0, 0, Color4I.WHITE, 0);
					GlStateManager.popMatrix();
				}
				else
				{
					GlStateManager.pushMatrix();
					GlStateManager.translate(0, 0, 500);
					QuestsTheme.COMPLETED.draw(x + width - 1 - zoom / 2, y + 1, zoom / 2, zoom / 2);
					GlStateManager.popMatrix();
				}
			}
		}
	}

	public class ButtonDummyQuest extends Widget
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
			if (isMouseOver() && questFile.canEdit())
			{
				if (button.isRight())
				{
					GuiHelper.playClickSound();
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setByte("x", x);
					nbt.setByte("y", y);
					Quest quest = new Quest(selectedChapter, nbt);
					ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
					ConfigGroup g = group.getGroup(QuestObjectType.QUEST.getName());
					quest.getConfig(g);
					quest.getExtraConfig(g);
					new GuiEditConfig(group, (group1, sender) -> {
						NBTTagCompound nbt1 = new NBTTagCompound();
						quest.writeData(nbt1);
						new MessageCreateObject(QuestObjectType.QUEST, selectedChapter.getID(), nbt1).sendToServer();
					}).openGui();

					return true;
				}
				else if (button.isLeft())
				{
					//Move quest here
				}
			}

			return false;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			if (isAltKeyDown())
			{
				Color4I.WHITE.withAlpha(30).draw(x, y, w, h);
			}

			if (isMouseOver())
			{
				Color4I.WHITE.withAlpha(30).draw(x, y, w, h);
			}
		}
	}

	public final ClientQuestFile questFile;
	private int scrollWidth, scrollHeight, prevMouseX, prevMouseY, grabbed;
	public QuestChapter selectedChapter;
	private String selectedQuest = "";
	public final Panel chapterPanel, quests;
	public Color4I borderColor, backgroundColor;
	public final Button otherOptions;

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

				if (questFile.canEdit() && questFile.chapters.size() < 14)
				{
					add(new ButtonAddChapter(this));
				}
			}

			@Override
			public void alignWidgets()
			{
				setX(1);
				setWidth(getGui().width - otherOptions.width - 2);
				align(new WidgetLayout.Horizontal(0, 1, 0));
			}
		};

		chapterPanel.setHeight(20);

		selectedChapter = questFile.chapters.isEmpty() ? null : questFile.chapters.get(0);
		borderColor = Color4I.WHITE.withAlpha(88);
		backgroundColor = Color4I.WHITE.withAlpha(33);

		quests = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				if (selectedChapter == null)
				{
					return;
				}

				for (int y = -Quest.POS_LIMIT; y <= Quest.POS_LIMIT; y++)
				{
					for (int x = -Quest.POS_LIMIT; x <= Quest.POS_LIMIT; x++)
					{
						add(new ButtonDummyQuest(this, (byte) x, (byte) y));
					}
				}

				int s = Quest.POS_LIMIT * 2 + 1;

				for (Quest quest : selectedChapter.quests)
				{
					widgets.set((quest.x + Quest.POS_LIMIT) + (quest.y + Quest.POS_LIMIT) * s, new ButtonQuest(this, quest));
				}
			}

			@Override
			public void alignWidgets()
			{
				scrollWidth = 0;
				scrollHeight = 0;

				int minX = Quest.POS_LIMIT + 1, minY = Quest.POS_LIMIT + 1, maxX = -(Quest.POS_LIMIT + 1), maxY = -(Quest.POS_LIMIT + 1);

				for (Widget widget : widgets)
				{
					if (widget instanceof ButtonQuest)
					{
						Quest quest = ((ButtonQuest) widget).quest;
						minX = Math.min(minX, quest.x);
						minY = Math.min(minY, quest.y);
						maxX = Math.max(maxX, quest.x);
						maxY = Math.max(maxY, quest.y);
					}
				}

				minX -= 6;
				minY -= 6;
				maxX += 6;
				maxY += 6;

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
						x = quest.x;
						y = quest.y;
					}
					else
					{
						ButtonDummyQuest button = (ButtonDummyQuest) widget;
						x = button.x;
						y = button.y;
					}

					widget.setPosAndSize(offset + (x - minX) * bspace, offset + (y - minY) * bspace, bsize, bsize);
				}

				setPosAndSize(0, chapterPanel.height, getGui().width, getGui().height - chapterPanel.height);
			}

			@Override
			public void drawOffsetBackground(Theme theme, int x, int y, int w, int h)
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
							buffer.pos(widget.getX() + widget.width / 2D, widget.getY() + widget.height / 2D, 0).color(100, 200, 100, 255).endVertex();
							buffer.pos(b.getX() + b.width / 2D, b.getY() + b.height / 2D, 0).color(50, 50, 50, 255).endVertex();
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

				if (!b && button.isLeft() && isMouseOver())
				{
					prevMouseX = getMouseX();
					prevMouseY = getMouseY();
					grabbed = 1;
					b = true;
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

		otherOptions = new ButtonOtherOptions(this);
	}

	@Override
	public void addWidgets()
	{
		add(chapterPanel);
		add(quests);
		add(otherOptions);
	}

	@Override
	public void alignWidgets()
	{
		otherOptions.setX(width - otherOptions.width - 1);
		chapterPanel.alignWidgets();
		resetScroll(true);
	}

	@Override
	public boolean onInit()
	{
		Keyboard.enableRepeatEvents(true);
		return setFullscreen();
	}

	@Override
	public void onClosed()
	{
		super.onClosed();
		Keyboard.enableRepeatEvents(false);
	}

	public void selectChapter(@Nullable QuestChapter chapter)
	{
		if (selectedChapter != chapter)
		{
			selectedChapter = chapter;
			quests.setScrollX(0);
			quests.setScrollY(0);
			selectedQuest = "";
			quests.refreshWidgets();
			resetScroll(true);
		}
	}

	private boolean canZoomIn()
	{
		return zoom < 24;
	}

	private boolean canZoomOut()
	{
		return zoom > 8;
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
			new MessageMoveQuest(quest.getID(), quest.x, quest.y).sendToServer();
		}
		else if (direction == 0)
		{
			new MessageMoveChapter(selectedChapter.getID(), true).sendToServer();
		}
		else if (direction == 4)
		{
			new MessageMoveChapter(selectedChapter.getID(), false).sendToServer();
		}
	}

	public void addObjectMenuItems(List<ContextMenuItem> contextMenu, GuiBase prevGui, QuestObject object)
	{
		contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), GuiIcons.SETTINGS, () -> new MessageEditObject(object.getID()).sendToServer()));
		contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> new MessageDeleteObject(object.getID()).sendToServer()).setYesNo(I18n.format("delete_item", object.getDisplayName().getFormattedText())));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> new MessageResetProgress(object.getID()).sendToServer()).setYesNo(I18n.format("ftbquests.gui.reset_progress_q")));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.complete_instantly"), QuestsTheme.COMPLETED, () -> new MessageCompleteInstantly(object.getID()).sendToServer()).setYesNo(I18n.format("ftbquests.gui.complete_instantly_q")));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.change_id"), GuiIcons.NOTES, () -> new GuiEditConfigValue("id", new ConfigString(object.id, Pattern.compile("^[a-z0-9_]{1,32}$")), (value, set) -> {
			prevGui.openGui();

			if (set)
			{
				new MessageChangeID(object.getID(), value.getString()).sendToServer();
			}
		}).openGui()));

		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.copy_id"), GuiIcons.INFO, () -> setClipboardString(object.getID())));
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
		else if (selectedChapter != null && questFile.canEdit() && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown())
		{
			switch (key)
			{
				case Keyboard.KEY_D:
					selectedQuest = "";
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
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		if (selectedChapter != null && selectedChapter.invalid)
		{
			selectChapter(null);
		}

		if (selectedChapter == null && !questFile.chapters.isEmpty())
		{
			selectChapter(questFile.chapters.get(0));
		}

		super.drawBackground(theme, x, y, w, h);

		if (grabbed != 0)
		{
			int mx = getMouseX();
			int my = getMouseY();

			if (scrollWidth > quests.width)
			{
				quests.setScrollX(Math.max(Math.min(quests.getScrollX() + (prevMouseX - mx), scrollWidth - quests.width), 0));
			}
			else
			{
				quests.setScrollX((scrollWidth - quests.width) / 2);
			}

			if (scrollHeight > quests.height)
			{
				quests.setScrollY(Math.max(Math.min(quests.getScrollY() + (prevMouseY - my), scrollHeight - quests.height), 0));
			}
			else
			{
				quests.setScrollY((scrollHeight - quests.height) / 2);
			}

			prevMouseX = mx;
			prevMouseY = my;
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
	public void drawForeground(Theme theme, int x, int y, int w, int h)
	{
		GuiHelper.drawHollowRect(x, y, w, h, borderColor, false);

		int start = 1;

		if (!chapterPanel.widgets.isEmpty())
		{
			Widget last = chapterPanel.widgets.get(chapterPanel.widgets.size() - 1);
			start = last.getX() + last.width + 1;
		}

		backgroundColor.draw(start, y + 1, w - start - otherOptions.width - 2, chapterPanel.height - 2);
		borderColor.draw(start, y + chapterPanel.height - 1, w - start - otherOptions.width - 2, 1);

		super.drawForeground(theme, x, y, w, h);
	}

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}

	@Override
	public boolean drawDefaultBackground()
	{
		return false;
	}
}