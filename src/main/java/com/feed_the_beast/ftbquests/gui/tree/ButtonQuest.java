package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestShape;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
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
	public GuiQuestTree treeGui;
	public Quest quest;
	public List<ButtonQuest> dependencies = null;

	public ButtonQuest(Panel panel, Quest q)
	{
		super(panel, q.getTitle(), q.getIcon());
		treeGui = (GuiQuestTree) panel.getGui();
		setSize(20, 20);
		quest = q;
	}

	@Override
	public boolean checkMouseOver(int mouseX, int mouseY)
	{
		if (treeGui.movingQuests || treeGui.viewQuestPanel.isMouseOver() || treeGui.chapterHoverPanel.isMouseOverAnyWidget())
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
						if (dependency == ((ButtonQuest) widget).quest)
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
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.add_reward_all"), ThemeProperties.ADD_ICON.get(), () -> {
					List<ContextMenuItem> contextMenu2 = new ArrayList<>();

					for (RewardType type : RewardType.getRegistry())
					{
						contextMenu2.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
							GuiHelper.playClickSound();
							type.getGuiProvider().openCreationGui(this, quest, reward -> {
								for (Quest quest1 : treeGui.selectedQuests)
								{
									Reward r = type.provider.create(quest1);
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

				contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), ThemeProperties.DELETE_ICON.get(), () -> {
					treeGui.selectedQuests.forEach(q -> ClientQuestFile.INSTANCE.deleteObject(q.id));
					treeGui.closeQuest();
				}).setYesNo(I18n.format("delete_item", I18n.format("ftbquests.quests") + " [" + treeGui.selectedQuests.size() + "]")));
			}
			else
			{
				contextMenu.add(new ContextMenuItem(I18n.format("gui.move"), ThemeProperties.MOVE_UP_ICON.get(), () -> {
					treeGui.movingQuests = true;
					treeGui.selectedQuests.clear();
					treeGui.toggleSelected(quest);
				})
				{
					@Override
					public void addMouseOverText(List<String> list)
					{
						list.add(TextFormatting.DARK_GRAY + I18n.format("ftbquests.gui.move_tooltip"));
					}
				});

				/*
				contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.move_to_chapter"), GuiIcons.RIGHT, () -> {
					ConfigQuestObject object = new ConfigQuestObject(treeGui.file, 0, o -> o instanceof Chapter && o != quest.chapter && !quest.hasDependency((Chapter) o));
					new GuiSelectQuestObject(object, this, () -> {
						treeGui.open(treeGui.file.get(object.getObject()));
					}).openGui();
				}));
				*/

				if (selectedQuest != null && selectedQuest != quest)
				{
					if (selectedQuest.hasDependency(quest))
					{
						contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.rem_dep"), ThemeProperties.DELETE_ICON.get(), () -> editDependency(selectedQuest, quest, false)));
					}
					else if (quest.hasDependency(selectedQuest))
					{
						contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.rem_dep"), ThemeProperties.DELETE_ICON.get(), () -> editDependency(quest, selectedQuest, false)));
					}
					else
					{
						contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.set_dep"), ThemeProperties.ADD_ICON.get(), () -> editDependency(quest, selectedQuest, true)).setEnabled(selectedQuest != null && selectedQuest != quest && !selectedQuest.canRepeat));
					}
				}

				contextMenu.add(ContextMenuItem.SEPARATOR);
				GuiQuestTree.addObjectMenuItems(contextMenu, getGui(), quest);
			}

			getGui().openContextMenu(contextMenu);
		}
		else if (button.isLeft())
		{
			if (isCtrlKeyDown() && treeGui.file.canEdit())
			{
				if (treeGui.getViewedQuest() != null)
				{
					treeGui.closeQuest();
				}

				treeGui.toggleSelected(quest);
			}
			else if (!quest.guidePage.isEmpty() && quest.tasks.isEmpty() && quest.rewards.isEmpty() && quest.getText().length == 0)
			{
				handleClick("guide", quest.guidePage);
			}
			else if (quest.customClick.isEmpty() || !handleClick(quest.customClick))
			{
				treeGui.open(quest);
			}
		}
		else if (treeGui.file.canEdit() && button.isMiddle())
		{
			if (!treeGui.selectedQuests.contains(quest))
			{
				treeGui.toggleSelected(quest);
			}

			treeGui.movingQuests = true;
		}
		else if (button.isRight())
		{
			treeGui.movingQuests = false;

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
			new MessageEditObject(quest).sendToServer();
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

		String description = quest.getDescription();

		if (!description.isEmpty())
		{
			list.add(TextFormatting.GRAY + description);
		}

		int r = quest.getUnclaimedRewards(Minecraft.getMinecraft().player.getUniqueID(), treeGui.file.self, true);

		if (r > 0)
		{
			list.add("");
			list.add(I18n.format("ftbquests.gui.collect_rewards", TextFormatting.GOLD.toString() + r));
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		Color4I outlineColor = Color4I.WHITE.withAlpha(150);
		Icon qicon = Icon.EMPTY;

		boolean cantStart = !quest.canStartTasks(treeGui.file.self);

		if (!cantStart)
		{
			int progress = quest.getRelativeProgress(treeGui.file.self);

			if (progress >= 100)
			{
				boolean hasRewards = false;

				for (Reward reward : quest.rewards)
				{
					if (!treeGui.file.self.isRewardClaimedSelf(reward))
					{
						hasRewards = true;
						break;
					}
				}

				if (hasRewards)
				{
					qicon = ThemeProperties.ALERT_ICON.get();
				}
				else
				{
					qicon = ThemeProperties.CHECK_ICON.get();
				}

				outlineColor = ThemeProperties.QUEST_COMPLETED_COLOR.get();
			}
			else if (progress > 0)
			{
				outlineColor = ThemeProperties.QUEST_STARTED_COLOR.get();
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

		QuestShape shape = quest.getShape();

		shape.shape.withColor(Color4I.DARK_GRAY).draw(sx, sy, s, s);
		shape.background.withColor(Color4I.WHITE.withAlpha(150)).draw(sx, sy, s, s);
		shape.outline.withColor(outlineColor).draw(sx, sy, s, s);

		if (!icon.isEmpty())
		{
			icon.draw(x + (w - z) / 2, y + (h - z) / 2, z, z);
		}

		if (quest == treeGui.viewQuestPanel.quest || treeGui.selectedQuests.contains(quest))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 500F);
			Color4I col = Color4I.WHITE.withAlpha(190 + (int) (Math.sin(System.currentTimeMillis() * 0.003D) * 50));
			shape.outline.withColor(col).draw(sx, sy, s, s);
			shape.background.withColor(col).draw(sx, sy, s, s);
			GlStateManager.popMatrix();
		}

		if (cantStart)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 500F);
			shape.shape.withColor(Color4I.BLACK.withAlpha(100)).draw(sx, sy, s, s);
			GlStateManager.popMatrix();
		}

		if (isMouseOver())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 500F);
			shape.shape.withColor(Color4I.WHITE.withAlpha(50)).draw(sx, sy, s, s);
			GlStateManager.popMatrix();
		}

		if (!qicon.isEmpty())
		{
			int s1 = z / 2;
			int os1 = s1 / 4;
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 500F);
			qicon.draw(x + w - s1 - os1, y + os1, s1, s1);
			GlStateManager.popMatrix();
		}
	}
}