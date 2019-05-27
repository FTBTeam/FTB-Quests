package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObjectDirect;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonQuest extends Button
{
	public static final Color4I COL_COMPLETED = Color4I.rgb(0x56FF56);
	public static final Color4I COL_STARTED = Color4I.rgb(0x00FFFF);

	public GuiQuestTree treeGui;
	public Quest quest;
	public List<ButtonQuest> dependencies = null;

	public ButtonQuest(Panel panel, Quest q)
	{
		super(panel, GuiQuestTree.fixI18n(null, q.getDisplayName().getFormattedText()), q.getIcon());
		treeGui = (GuiQuestTree) panel.getGui();
		setSize(20, 20);
		quest = q;
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY)
	{
		if (treeGui.viewQuestPanel.isMouseOver() || treeGui.chapterHoverPanel.isMouseOverAnyWidget())
		{
			return false;
		}

		return super.checkMouseOver(mouseX, mouseY);
	}

	public List<ButtonQuest> getDependencies()
	{
		if (dependencies == null)
		{
			dependencies = new ArrayList<>();

			for (QuestObject dependency : quest.dependencies)
			{
				if (!dependency.invalid && dependency instanceof Quest)
				{
					for (Widget widget : treeGui.questPanel.widgets)
					{
						if (widget instanceof ButtonQuest && dependency == ((ButtonQuest) widget).quest)
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
		Quest selectedQuest = treeGui.selectedQuests.size() == 1 ? treeGui.selectedQuests.iterator().next() : null;

		if (treeGui.file.canEdit() && button.isRight())
		{
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			if (treeGui.selectedQuests.size() > 1)
			{
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.add_reward_all"), FTBQuestsTheme.ADD, () -> {
					List<ContextMenuItem> contextMenu2 = new ArrayList<>();

					for (QuestRewardType type : QuestRewardType.getRegistry())
					{
						contextMenu2.add(new ContextMenuItem(type.getDisplayName().getFormattedText(), type.getIcon(), () -> {
							GuiHelper.playClickSound();
							type.getGuiProvider().openCreationGui(this, quest, reward -> {
								for (Quest quest1 : treeGui.selectedQuests)
								{
									QuestReward r = type.provider.create(quest1);
									NBTTagCompound nbt1 = new NBTTagCompound();
									reward.writeData(nbt1);
									r.readData(nbt1);
									NBTTagCompound extra = new NBTTagCompound();
									extra.setString("type", type.getTypeForNBT());
									new MessageCreateObject(r, extra).sendToServer();
								}
							});
						}));
					}

					getGui().openContextMenu(contextMenu2);
				}));

				contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> {
					treeGui.selectedQuests.forEach(q -> ClientQuestFile.INSTANCE.deleteObject(q.id));
					treeGui.closeQuest();
				}).setYesNo(I18n.format("delete_item", I18n.format("ftbquests.quests") + " [" + treeGui.selectedQuests.size() + "]")));
			}
			else
			{
				contextMenu.add(new ContextMenuItem(I18n.format("gui.move"), GuiIcons.UP, () -> {
					treeGui.movingQuest = true;
					treeGui.selectedQuests.clear();
					treeGui.toggleSelected(quest);

				}));

				if (selectedQuest != null && selectedQuest != quest)
				{
					if (selectedQuest.hasDependency(quest))
					{
						contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.rem_dep"), GuiIcons.REMOVE, () -> editDependency(selectedQuest, quest, false)));
					}
					else if (quest.hasDependency(selectedQuest))
					{
						contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.rem_dep"), GuiIcons.REMOVE, () -> editDependency(quest, selectedQuest, false)));
					}
					else
					{
						contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.set_dep"), FTBQuestsTheme.ADD, () -> editDependency(quest, selectedQuest, true)).setEnabled(selectedQuest != null && selectedQuest != quest && !selectedQuest.canRepeat));
					}
				}

				contextMenu.add(ContextMenuItem.SEPARATOR);
				GuiQuestTree.addObjectMenuItems(contextMenu, getGui(), quest);
			}

			getGui().openContextMenu(contextMenu);
		}
		else if (button.isLeft())
		{
			if (treeGui.movingQuest && selectedQuest == quest)
			{
				treeGui.movingQuest = false;
				treeGui.viewQuestPanel.hidePanel = false;
				treeGui.viewQuest(quest);
			}
			else
			{
				if (isCtrlKeyDown() && treeGui.file.canEdit())
				{
					if (treeGui.getViewedQuest() != null)
					{
						treeGui.closeQuest();
					}

					treeGui.toggleSelected(quest);
				}
				else if (!quest.guidePage.isEmpty() && quest.tasks.isEmpty() && quest.rewards.isEmpty() && quest.text.isEmpty())
				{
					handleClick("guide", quest.guidePage);
				}
				else if (quest.customClick.isEmpty() || !handleClick(quest.customClick))
				{
					treeGui.open(quest);
				}
			}
		}
		else if (treeGui.file.canEdit() && button.isMiddle())
		{
			treeGui.movingQuest = true;
			treeGui.closeQuest();
			treeGui.toggleSelected(quest);
		}
		else if (button.isRight())
		{
			treeGui.movingQuest = false;

			if (treeGui.getViewedQuest() != quest)
			{
				treeGui.viewQuestPanel.hidePanel = true;
				treeGui.viewQuest(quest);
			}
			else
			{
				treeGui.closeQuest();
			}
		}
	}

	private void editDependency(Quest quest, QuestObject object, boolean add)
	{
		List<QuestObject> prevDeps = new ArrayList<>(quest.dependencies);

		if (add != quest.hasDependency(object))
		{
			if (add)
			{
				quest.dependencies.add(object);
			}
			else
			{
				for (int i = 0; i < quest.dependencies.size(); i++)
				{
					if (quest.dependencies.get(i) == object)
					{
						quest.dependencies.remove(i);
						break;
					}
				}
			}
		}

		if (quest.verifyDependencies(false))
		{
			new MessageEditObjectDirect(quest).sendToServer();
			treeGui.questPanel.refreshWidgets();
		}
		else
		{
			quest.dependencies.clear();
			quest.dependencies.addAll(prevDeps);
			GuiQuestTree.displayError(new TextComponentTranslation("ftbquests.gui.looping_dependencies"));
		}
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse()
	{
		return quest.tasks.size() == 1 ? quest.tasks.get(0).getIngredient() : null;
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		String title = getTitle();

		if (treeGui.file.self != null)
		{
			int p = quest.getRelativeProgress(treeGui.file.self);

			if (p > 0 && p < 100)
			{
				title += " " + TextFormatting.DARK_GRAY + p + "%";
			}
		}

		list.add(title);

		String description = GuiQuestTree.fixI18n(TextFormatting.GRAY, quest.description);

		if (!description.isEmpty())
		{
			list.add(description);
		}

		if (treeGui.file.self != null)
		{
			int r = quest.getUnclaimedRewards(Minecraft.getMinecraft().player.getUniqueID(), treeGui.file.self);

			if (r > 0)// || quest.canRepeat)
			{
				list.add("");
			}

			if (r > 0)
			{
				list.add(I18n.format("ftbquests.gui.collect_rewards", TextFormatting.GOLD.toString() + r));
			}

			/*
			if (quest.canRepeat)
			{
				list.add(I18n.format("ftbquests.gui.times_completed") + ": " + TextFormatting.GOLD + treeGui.questFile.self.getTimesCompleted(quest));
			}
			*/
		}
	}

	@Override
	public WidgetType getWidgetType()
	{
		if (treeGui.getViewedQuest() == quest)
		{
			return WidgetType.MOUSE_OVER;
		}

		return treeGui.file.editingMode || quest.getActualVisibility(treeGui.file.self).isVisible() ? super.getWidgetType() : WidgetType.DISABLED;
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		Color4I outlineColor = Color4I.WHITE.withAlpha(150);
		Icon qicon = Icon.EMPTY;

		boolean cantStart = treeGui.file.self == null || !quest.canStartTasks(treeGui.file.self);

		if (!cantStart)
		{
			int progress = quest.getRelativeProgress(treeGui.file.self);

			if (progress >= 100)
			{
				boolean hasRewards = false;

				for (QuestReward reward : quest.rewards)
				{
					if (!treeGui.file.isRewardClaimed(reward))
					{
						hasRewards = true;
						break;
					}
				}

				if (hasRewards)
				{
					qicon = FTBQuestsTheme.ALERT;
				}
				else
				{
					qicon = FTBQuestsTheme.COMPLETED;
				}

				outlineColor = COL_COMPLETED.withAlpha(200);
			}
			else if (progress > 0)
			{
				outlineColor = COL_STARTED.withAlpha(200);
			}
		}
		else
		{
			outlineColor = Color4I.GRAY;
		}

		int z = treeGui.getZoom();

		int s = z * 3 / 2;
		int sx = x + (w - s) / 2;
		int sy = y + (h - s) / 2;

		if (treeGui.selectedQuests.contains(quest))
		{
			int s1 = s + z / 5;
			int sx1 = x + (w - s1) / 2;
			int sy1 = y + (h - s1) / 2;
			GlStateManager.pushMatrix();
			GlStateManager.translate(sx1, sy1, 0D);
			GlStateManager.scale(s1, s1, 1D);
			quest.shape.outline.draw(0, 0, 1, 1, Color4I.WHITE.withAlpha(150 + (int) (Math.sin(System.currentTimeMillis() * 0.003D) * 80)));
			GlStateManager.popMatrix();
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(sx, sy, 0D);
		GlStateManager.scale(s, s, 1D);
		quest.shape.shape.draw(0, 0, 1, 1, Color4I.DARK_GRAY);
		quest.shape.background.draw(0, 0, 1, 1, Color4I.WHITE.withAlpha(150));
		quest.shape.outline.draw(0, 0, 1, 1, outlineColor);
		GlStateManager.popMatrix();

		if (!icon.isEmpty())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + (w - z) / 2, y + (h - z) / 2, 0D);
			GlStateManager.scale(z, z, 1D);
			icon.draw(0, 0, 1, 1);
			GlStateManager.popMatrix();
		}

		if (cantStart)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(sx, sy, 500);
			GlStateManager.scale(s, s, 1D);
			quest.shape.shape.draw(0, 0, 1, 1, Color4I.BLACK.withAlpha(100));
			GlStateManager.popMatrix();
		}

		if (isMouseOver())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(sx, sy, 500);
			GlStateManager.scale(s, s, 1D);
			quest.shape.shape.draw(0, 0, 1, 1, Color4I.WHITE.withAlpha(50));
			GlStateManager.popMatrix();
		}

		if (!qicon.isEmpty())
		{
			int s1 = z / 2;
			int os1 = s1 / 4;
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + w - s1 - os1, y + os1, 500);
			GlStateManager.scale(s1, s1, 1D);
			qicon.draw(0, 0, 1, 1);
			GlStateManager.popMatrix();
		}
	}
}