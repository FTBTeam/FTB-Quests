package com.feed_the_beast.ftbquests.gui.quests;

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
import com.feed_the_beast.mods.ftbguilibrary.utils.PixelBuffer;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
	public GuiQuests treeGui;
	public Quest quest;
	public ButtonQuest[] dependencies = null;

	public ButtonQuest(Panel panel, Quest q)
	{
		super(panel, q.getTitle(), q.getIcon());
		treeGui = (GuiQuests) panel.getGui();
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

		if (treeGui.questPanel.mouseOverQuest != null && treeGui.questPanel.mouseOverQuest != this)
		{
			isMouseOver = false;
		}

		if (isMouseOver)
		{
			QuestShape shape = QuestShape.get(quest.getShape());

			int ax = getX();
			int ay = getY();

			double relX = (mouseX - ax) / (double) width;
			double relY = (mouseY - ay) / (double) height;

			PixelBuffer pixelBuffer = shape.getShapePixels();

			int rx = (int) (relX * pixelBuffer.getWidth());
			int ry = (int) (relY * pixelBuffer.getHeight());

			if (rx < 0 || ry < 0 || rx >= pixelBuffer.getWidth() || ry >= pixelBuffer.getHeight())
			{
				isMouseOver = false;
			}
			else
			{
				int a = (pixelBuffer.getRGB(rx, ry) >> 24) & 0xFF;

				if (a < 5)
				{
					isMouseOver = false;
				}
			}
		}

		if (isMouseOver && treeGui.questPanel.mouseOverQuest == null)
		{
			treeGui.questPanel.mouseOverQuest = this;
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
					contextMenu.add(new ContextMenuItem(new TranslationTextComponent("ftbquests.gui.add_dependencies"), ThemeProperties.ADD_ICON.get(), () -> {
						for (Movable q : treeGui.selectedObjects)
						{
							if (q instanceof Quest)
							{
								editDependency(quest, (Quest) q, true);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(new TranslationTextComponent("ftbquests.gui.remove_dependencies"), ThemeProperties.DELETE_ICON.get(), () -> {
						for (Movable q : treeGui.selectedObjects)
						{
							if (q instanceof Quest)
							{
								editDependency(quest, (Quest) q, false);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(new TranslationTextComponent("ftbquests.gui.add_dependencies_self"), ThemeProperties.ADD_ICON.get(), () -> {
						for (Movable q : treeGui.selectedObjects)
						{
							if (q instanceof Quest)
							{
								editDependency((Quest) q, quest, true);
							}
						}
					}));

					contextMenu.add(new ContextMenuItem(new TranslationTextComponent("ftbquests.gui.remove_dependencies_self"), ThemeProperties.DELETE_ICON.get(), () -> {
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
					contextMenu.add(new ContextMenuItem(new TranslationTextComponent("ftbquests.gui.add_reward_all"), ThemeProperties.ADD_ICON.get(quest), () -> {
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

					contextMenu.add(new ContextMenuItem(new TranslationTextComponent("selectServer.delete"), ThemeProperties.DELETE_ICON.get(quest), () -> {
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
					}).setYesNo(new TranslationTextComponent("delete_item", new TranslationTextComponent("ftbquests.quests") + " [" + treeGui.selectedObjects.size() + "]")));
				}

				contextMenu.add(ContextMenuItem.SEPARATOR);
				contextMenu.add(new ContextMenuItem(new StringTextComponent("Ctrl+A to select all quests").mergeStyle(TextFormatting.GRAY), GuiIcons.INFO, null));
				contextMenu.add(new ContextMenuItem(new StringTextComponent("Ctrl+D to deselect all quests").mergeStyle(TextFormatting.GRAY), GuiIcons.INFO, null));
				contextMenu.add(new ContextMenuItem(new StringTextComponent("Ctrl+Arrow Key to move selected quests").mergeStyle(TextFormatting.GRAY), GuiIcons.INFO, null));
			}
			else
			{
				contextMenu.add(new ContextMenuItem(new TranslationTextComponent("gui.move"), ThemeProperties.MOVE_UP_ICON.get(quest), () -> {
					treeGui.movingObjects = true;
					treeGui.selectedObjects.clear();
					treeGui.toggleSelected(quest);
				})
				{
					@Override
					public void addMouseOverText(TooltipList list)
					{
						list.add(new TranslationTextComponent("ftbquests.gui.move_tooltip").mergeStyle(TextFormatting.DARK_GRAY));
					}
				});

				contextMenu.add(ContextMenuItem.SEPARATOR);
				GuiQuests.addObjectMenuItems(contextMenu, getGui(), quest);
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
			else
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
			GuiQuests.displayError(new TranslationTextComponent("ftbquests.gui.looping_dependencies"));
		}
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse()
	{
		return quest.tasks.size() == 1 ? quest.tasks.get(0).getIngredient() : null;
	}

	@Override
	public void addMouseOverText(TooltipList list)
	{
		ITextComponent title = getTitle();

		if (treeGui.file.self != null)
		{
			int p = treeGui.file.self.getRelativeProgress(quest);

			if (p > 0 && p < 100)
			{
				title = title.deepCopy().append(new StringTextComponent(" " + p + "%").mergeStyle(TextFormatting.DARK_GRAY));
			}
		}

		list.add(title);

		IFormattableTextComponent description = quest.getSubtitle();

		if (description != StringTextComponent.EMPTY)
		{
			list.add(description.mergeStyle(TextFormatting.GRAY));
		}
	}

	@Override
	public void draw(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		Color4I outlineColor = Color4I.WHITE.withAlpha(150);
		Icon qicon = Icon.EMPTY;

		boolean cantStart = !treeGui.file.self.canStartTasks(quest);

		if (!cantStart)
		{
			int progress = treeGui.file.self.getRelativeProgress(quest);

			if (progress >= 100)
			{
				if (treeGui.file.self.hasUnclaimedRewards(quest))
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

		QuestShape shape = QuestShape.get(quest.getShape());

		shape.shape.withColor(Color4I.DARK_GRAY).draw(matrixStack, x, y, w, h);
		shape.background.withColor(Color4I.WHITE.withAlpha(150)).draw(matrixStack, x, y, w, h);
		shape.outline.withColor(outlineColor).draw(matrixStack, x, y, w, h);

		if (!icon.isEmpty())
		{
			float s = w * 2F / 3F;
			matrixStack.push();
			matrixStack.translate(x + (w - s) / 2D, y + (h - s) / 2D, 0F);
			matrixStack.scale(s, s, 1F);
			icon.draw(matrixStack, 0, 0, 1, 1);
			matrixStack.pop();
		}

		RenderSystem.enableAlphaTest();
		RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1F, 1F, 1F, 1F);

		if (quest == treeGui.viewQuestPanel.quest || treeGui.selectedObjects.contains(quest))
		{
			matrixStack.push();
			matrixStack.translate(0, 0, 500);
			Color4I col = Color4I.WHITE.withAlpha((int) (190D + Math.sin(System.currentTimeMillis() * 0.003D) * 50D));
			shape.outline.withColor(col).draw(matrixStack, x, y, w, h);
			shape.background.withColor(col).draw(matrixStack, x, y, w, h);
			matrixStack.pop();
		}

		if (cantStart)
		{
			matrixStack.push();
			matrixStack.translate(0, 0, 500);
			shape.shape.withColor(Color4I.BLACK.withAlpha(100)).draw(matrixStack, x, y, w, h);
			matrixStack.pop();
		}

		if (isMouseOver())
		{
			matrixStack.push();
			matrixStack.translate(0, 0, 500);
			shape.shape.withColor(Color4I.WHITE.withAlpha(100)).draw(matrixStack, x, y, w, h);
			matrixStack.pop();
		}

		if (!qicon.isEmpty())
		{
			float s = w / 2F;//(int) (treeGui.getZoom() / 2 * quest.size);
			matrixStack.push();
			matrixStack.translate(x + w - s, y, 500);
			matrixStack.scale(s, s, 1F);
			qicon.draw(matrixStack, 0, 0, 1, 1);
			matrixStack.pop();
		}
	}
}