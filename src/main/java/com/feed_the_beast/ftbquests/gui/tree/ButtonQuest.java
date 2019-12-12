package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageCreateObject;
import com.feed_the_beast.ftbquests.net.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.ChapterImage;
import com.feed_the_beast.ftbquests.quest.Movable;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestShape;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonQuest extends Button
{
	public GuiQuestTree treeGui;
	public Quest quest;
	public ButtonQuest[] dependencies = null;

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
		if (treeGui.movingObjects || treeGui.viewQuestPanel.isMouseOver() || treeGui.chapterHoverPanel.isMouseOverAnyWidget())
		{
			return false;
		}

		return super.checkMouseOver(mouseX, mouseY);
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY)
	{
		super.updateMouseOver(mouseX, mouseY);

		if (treeGui.questPanel.mouseOverQuest == null)
		{
			if (isMouseOver)
			{
				treeGui.questPanel.mouseOverQuest = this;
			}
		}
		else if (treeGui.questPanel.mouseOverQuest != this)
		{
			isMouseOver = false;
		}
	}

	public ButtonQuest[] getDependencies()
	{
		if (dependencies == null)
		{
			ArrayList<ButtonQuest> list = new ArrayList<>();

			for (QuestObject dependency : quest.dependencies)
			{
				if (!dependency.invalid && dependency instanceof Quest)
				{
					for (Widget widget : treeGui.questPanel.widgets)
					{
						if (widget instanceof ButtonQuest && dependency == ((ButtonQuest) widget).quest)
						{
							list.add((ButtonQuest) widget);
						}
					}
				}
			}

			dependencies = list.toArray(new ButtonQuest[0]);
		}

		return dependencies;
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();

		if (treeGui.file.canEdit() && button.isRight())
		{
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			if (!treeGui.selectedObjects.isEmpty())
			{
				if (!treeGui.selectedObjects.contains(quest))
				{
					contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.add_dependencies"), ThemeProperties.ADD_ICON.get(), () -> {
						for (Movable q : treeGui.selectedObjects)
						{
							if (q instanceof Quest)
							{
								editDependency(quest, (Quest) q, true);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.remove_dependencies"), ThemeProperties.DELETE_ICON.get(), () -> {
						for (Movable q : treeGui.selectedObjects)
						{
							if (q instanceof Quest)
							{
								editDependency(quest, (Quest) q, false);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.add_dependencies_self"), ThemeProperties.ADD_ICON.get(), () -> {
						for (Movable q : treeGui.selectedObjects)
						{
							if (q instanceof Quest)
							{
								editDependency((Quest) q, quest, true);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.remove_dependencies_self"), ThemeProperties.DELETE_ICON.get(), () -> {
						for (Movable q : treeGui.selectedObjects)
						{
							if (q instanceof Quest)
							{
								editDependency((Quest) q, quest, false);
							}
						}
					}));
				}
				else
				{
					contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.add_reward_all"), ThemeProperties.ADD_ICON.get(quest), () -> {
						List<ContextMenuItem> contextMenu2 = new ArrayList<>();

						for (RewardType type : RewardType.getRegistry())
						{
							contextMenu2.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
								playClickSound();
								type.getGuiProvider().openCreationGui(this, quest, reward -> {
									for (Movable movable : treeGui.selectedObjects)
									{
										if (movable instanceof Quest)
										{
											Reward r = type.provider.create((Quest) movable);
											CompoundNBT nbt1 = new CompoundNBT();
											reward.writeData(nbt1);
											r.readData(nbt1);
											CompoundNBT extra = new CompoundNBT();
											extra.putString("type", type.getTypeForNBT());
											new MessageCreateObject(r, extra).sendToServer();
										}
									}
								});
							}));
						}

						getGui().openContextMenu(contextMenu2);
					}));

					contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), ThemeProperties.DELETE_ICON.get(quest), () -> {
						treeGui.selectedObjects.forEach(q -> {
							if (q instanceof Quest)
							{
								ClientQuestFile.INSTANCE.deleteObject(((Quest) q).id);
							}
							else if (q instanceof ChapterImage)
							{
								((ChapterImage) q).chapter.images.remove(q);
								new MessageEditObject(((ChapterImage) q).chapter).sendToServer();
							}
						});
						treeGui.selectedObjects.clear();
					}).setYesNo(new TranslationTextComponent("delete_item", I18n.format("ftbquests.quests") + " [" + treeGui.selectedObjects.size() + "]")));
				}

				contextMenu.add(ContextMenuItem.SEPARATOR);
				contextMenu.add(new ContextMenuItem(TextFormatting.GRAY + "Ctrl+A to select all quests", GuiIcons.INFO, null));
				contextMenu.add(new ContextMenuItem(TextFormatting.GRAY + "Ctrl+D to deselect all quests", GuiIcons.INFO, null));
				contextMenu.add(new ContextMenuItem(TextFormatting.GRAY + "Ctrl+Arrow Key to move selected quests", GuiIcons.INFO, null));
			}
			else
			{
				contextMenu.add(new ContextMenuItem(I18n.format("gui.move"), ThemeProperties.MOVE_UP_ICON.get(quest), () -> {
					treeGui.movingObjects = true;
					treeGui.selectedObjects.clear();
					treeGui.toggleSelected(quest);
				})
				{
					@Override
					public void addMouseOverText(List<String> list)
					{
						list.add(TextFormatting.DARK_GRAY + I18n.format("ftbquests.gui.move_tooltip"));
					}
				});

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
			else if (!quest.guidePage.isEmpty() && quest.tasks.isEmpty() && quest.rewards.isEmpty() && quest.getDescription().length == 0)
			{
				handleClick("guide", quest.guidePage);
			}
			else if (quest.customClick.isEmpty() || !handleClick(quest.customClick))
			{
				treeGui.open(quest, false);
			}
		}
		else if (treeGui.file.canEdit() && button.isMiddle())
		{
			if (!treeGui.selectedObjects.contains(quest))
			{
				treeGui.toggleSelected(quest);
			}

			treeGui.movingObjects = true;
		}
		else if (button.isRight())
		{
			treeGui.movingObjects = false;

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
				quest.dependencies.remove(object);
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
			GuiQuestTree.displayError(new TranslationTextComponent("ftbquests.gui.looping_dependencies"));
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
			int p = treeGui.file.self.getRelativeProgress(quest);

			if (p > 0 && p < 100)
			{
				title += " " + TextFormatting.DARK_GRAY + p + "%";
			}
		}

		list.add(title);

		String description = quest.getSubtitle();

		if (!description.isEmpty())
		{
			list.add(TextFormatting.GRAY + description);
		}

		int r = treeGui.file.self.getUnclaimedRewards(quest, true);

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

		boolean cantStart = !treeGui.file.self.canStartTasks(quest);

		if (!cantStart)
		{
			int progress = treeGui.file.self.getRelativeProgress(quest);

			if (progress >= 100)
			{
				boolean hasRewards = false;

				for (Reward reward : quest.rewards)
				{
					if (!treeGui.file.self.isRewardClaimed(reward.id))
					{
						hasRewards = true;
						break;
					}
				}

				if (hasRewards)
				{
					qicon = ThemeProperties.ALERT_ICON.get(quest);
				}
				else
				{
					qicon = ThemeProperties.CHECK_ICON.get(quest);
				}

				outlineColor = ThemeProperties.QUEST_COMPLETED_COLOR.get(quest);
			}
			else if (progress > 0)
			{
				outlineColor = ThemeProperties.QUEST_STARTED_COLOR.get(quest);
			}
		}
		else
		{
			outlineColor = Color4I.GRAY;
		}

		QuestShape shape = quest.getShape();

		shape.shape.withColor(Color4I.DARK_GRAY).draw(x, y, w, h);
		shape.background.withColor(Color4I.WHITE.withAlpha(150)).draw(x, y, w, h);
		shape.outline.withColor(outlineColor).draw(x, y, w, h);

		if (!icon.isEmpty())
		{
			double s = w * 2D / 3D;
			GlStateManager.pushMatrix();
			GlStateManager.translated(x + (w - s) / 2D, y + (h - s) / 2D, 0F);
			GlStateManager.scaled(s, s, 1D);
			icon.draw(0, 0, 1, 1);
			GlStateManager.popMatrix();
		}

		GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1F, 1F, 1F, 1F);

		if (quest == treeGui.viewQuestPanel.quest || treeGui.selectedObjects.contains(quest))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 0, 500);
			Color4I col = Color4I.WHITE.withAlpha((int) (190D + Math.sin(System.currentTimeMillis() * 0.003D) * 50D));
			shape.outline.withColor(col).draw(x, y, w, h);
			shape.background.withColor(col).draw(x, y, w, h);
			GlStateManager.popMatrix();
		}

		if (cantStart)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 0, 500);
			shape.shape.withColor(Color4I.BLACK.withAlpha(100)).draw(x, y, w, h);
			GlStateManager.popMatrix();
		}

		if (isMouseOver())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 0, 500);
			shape.shape.withColor(Color4I.WHITE.withAlpha(100)).draw(x, y, w, h);
			GlStateManager.popMatrix();
		}

		if (!qicon.isEmpty())
		{
			double s = w / 2D;//(int) (treeGui.getZoom() / 2 * quest.size);
			GlStateManager.pushMatrix();
			GlStateManager.translated(x + w - s, y, 500);
			GlStateManager.scaled(s, s, 1D);
			qicon.draw(0, 0, 1, 1);
			GlStateManager.popMatrix();
		}
	}
}