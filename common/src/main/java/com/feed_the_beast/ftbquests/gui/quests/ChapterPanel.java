package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.gui.ChangeChapterGroupScreen;
import com.feed_the_beast.ftbquests.net.MessageMoveChapter;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.ChapterGroup;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TextComponentParser;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ChapterPanel extends Panel {
	public static final Icon ARROW_COLLAPSED = Icon.getIcon("ftbquests:textures/gui/arrow_collapsed.png");
	public static final Icon ARROW_EXPANDED = Icon.getIcon("ftbquests:textures/gui/arrow_expanded.png");

	public interface ActualWidth {
		int getActualWidth(QuestScreen screen);
	}

	public static class ChapterGroupButton extends Button implements ActualWidth {
		public final ChapterGroup group;
		public final List<Chapter> visibleChapters;

		public ChapterGroupButton(ChapterPanel panel, ChapterGroup g) {
			super(panel, g.getTitle(), g.getIcon());
			setSize(100, 18);
			group = g;
			visibleChapters = g.getVisibleChapters(panel.questScreen.file.self);
		}

		@Override
		public void onClicked(MouseButton mouseButton) {
			group.guiCollapsed = !group.guiCollapsed;
			parent.refreshWidgets();
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();

			if (isMouseOver()) {
				Color4I.WHITE.withAlpha(40).draw(matrixStack, x + 1, y, w - 2, h);
			}

			ChatFormatting f = isMouseOver() ? ChatFormatting.WHITE : ChatFormatting.GRAY;

			(group.guiCollapsed ? ARROW_COLLAPSED : ARROW_EXPANDED).withColor(Color4I.getChatFormattingColor(f)).draw(matrixStack, x + 3, y + 5, 8, 8);
			theme.drawString(matrixStack, new TextComponent("").append(title).withStyle(f), x + 15, y + 5);
		}

		@Override
		public void addMouseOverText(TooltipList list) {
		}

		@Nullable
		public Object getIngredientUnderMouse() {
			return icon.getIngredient();
		}

		@Override
		public int getActualWidth(QuestScreen screen) {
			return screen.getTheme().getStringWidth(title) + 20;
		}
	}

	public static class ChapterButton extends Button implements ActualWidth {
		public final Chapter chapter;
		public List<Component> description;

		public ChapterButton(ChapterPanel panel, Chapter c) {
			super(panel, c.getTitle(), c.getIcon());
			chapter = c;
			setSize(100, 14);

			/*
			if (panel.questScreen.file.self != null) {
				int p = panel.questScreen.file.self.getRelativeProgress(c);

				if (p > 0 && p < 100) {
					setTitle(new TextComponent("").append(getTitle()).append(" ").append(new TextComponent(p + "%").withStyle(ChatFormatting.DARK_GREEN)));
				}
			}
			*/

			description = new ArrayList<>();

			for (String v : chapter.subtitle) {
				description.add(new TextComponent("").append(TextComponentParser.parse(v, FTBQuestsClient.DEFAULT_STRING_TO_COMPONENT)).withStyle(ChatFormatting.GRAY));
			}
		}

		@Override
		public void onClicked(MouseButton button) {
			QuestScreen questScreen = ((ChapterPanel) parent).questScreen;

			if (questScreen.file.canEdit() || !chapter.quests.isEmpty()) {
				playClickSound();

				if (questScreen.selectedChapter != chapter) {
					questScreen.open(chapter, false);
				}
			}

			if (questScreen.file.canEdit() && button.isRight()) {
				List<ContextMenuItem> contextMenu = new ArrayList<>();
				contextMenu.add(new ContextMenuItem(new TranslatableComponent("gui.move"), ThemeProperties.MOVE_UP_ICON.get(), () -> new MessageMoveChapter(chapter.id, true).sendToServer()).setEnabled(() -> chapter.getIndex() > 0).setCloseMenu(false));
				contextMenu.add(new ContextMenuItem(new TranslatableComponent("gui.move"), ThemeProperties.MOVE_DOWN_ICON.get(), () -> new MessageMoveChapter(chapter.id, false).sendToServer()).setEnabled(() -> chapter.getIndex() < chapter.group.chapters.size() - 1).setCloseMenu(false));
				contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.change_group"), GuiIcons.COLOR_RGB, () -> new ChangeChapterGroupScreen(chapter).openGui()));
				contextMenu.add(ContextMenuItem.SEPARATOR);
				QuestScreen.addObjectMenuItems(contextMenu, questScreen, chapter);
				questScreen.openContextMenu(contextMenu);
			}
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();

			if (isMouseOver()) {
				Color4I.WHITE.withAlpha(40).draw(matrixStack, x + 1, y, w - 2, h);
			}

			ChatFormatting f = isMouseOver() ? ChatFormatting.WHITE : ChatFormatting.GRAY;
			int o = chapter.group.isDefaultGroup() ? 0 : 7;

			icon.draw(matrixStack, x + 2 + o, y + 1, 12, 12);
			theme.drawString(matrixStack, new TextComponent("").append(title).withStyle(f), x + 16 + o, y + 3);

			GuiHelper.setupDrawing();

			/*
			int w2 = 20;

			if (chapter.quests.isEmpty()) {
				matrixStack.pushPose();
				matrixStack.translate(0, 0, 450);
				ThemeProperties.CLOSE_ICON.get().draw(matrixStack, x + w2 - 8, y + 2, 8, 8);
				matrixStack.popPose();
			} else if (questScreen.file.self.hasUnclaimedRewards(chapter)) {
				matrixStack.pushPose();
				matrixStack.translate(0, 0, 450);
				ThemeProperties.ALERT_ICON.get().draw(matrixStack, x + w2 - 7, y + 3, 6, 6);
				matrixStack.popPose();
			} else if (questScreen.file.self.isComplete(chapter)) {
				matrixStack.pushPose();
				matrixStack.translate(0, 0, 450);
				ThemeProperties.CHECK_ICON.get().draw(matrixStack, x + w2 - 8, y + 2, 8, 8);
				matrixStack.popPose();
			}
			 */
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			for (Component s : description) {
				list.add(s);
			}
		}

		@Nullable
		public Object getIngredientUnderMouse() {
			return icon.getIngredient();
		}

		@Override
		public int getActualWidth(QuestScreen screen) {
			int o = chapter.group.isDefaultGroup() ? 0 : 7;
			return screen.getTheme().getStringWidth(title) + 20 + o;
		}
	}

	public final QuestScreen questScreen;
	public boolean expanded = false;
	public float position = 0F;
	public float prevPosition = 0F;

	public ChapterPanel(Panel panel) {
		super(panel);
		questScreen = (QuestScreen) panel.getGui();
	}

	@Override
	public void addWidgets() {
		/*
		if (Platform.isModLoaded("ftbmoney")) {
			add(new OpenShopButton(this));
			Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(treeGui.selectedChapter);
			add(new ColorWidget(this, borderColor, null).setPosAndSize(1, 0, width - 2, 1));
		}
		 */

		boolean canEdit = questScreen.file.canEdit();

		for (Chapter chapter : questScreen.file.defaultChapterGroup.getVisibleChapters(questScreen.file.self)) {
			add(new ChapterButton(this, chapter));
		}

		for (ChapterGroup group : questScreen.file.chapterGroups) {
			if (group.isDefaultGroup()) {
				continue;
			}

			ChapterGroupButton b = new ChapterGroupButton(this, group);

			if (!b.visibleChapters.isEmpty()) {
				add(b);

				if (!group.guiCollapsed) {
					for (Chapter chapter : b.visibleChapters) {
						add(new ChapterButton(this, chapter));
					}
				}
			}
		}

		if (canEdit) {
			//add(new ExpandChaptersButton(this));
		}
	}

	@Override
	public void alignWidgets() {
		int wd = 100;

		for (Widget w : widgets) {
			wd = Math.min(Math.max(wd, ((ActualWidth) w).getActualWidth(questScreen)), 800);
		}

		setPosAndSize(expanded ? 0 : -wd, 0, wd, questScreen.height);

		for (Widget w : widgets) {
			w.setWidth(wd);
		}

		align(WidgetLayout.VERTICAL);
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY) {
		super.updateMouseOver(mouseX, mouseY);

		if (expanded && !isMouseOver()) {
			setExpanded(false);
		}
	}

	@Override
	public void tick() {
		super.tick();

		prevPosition = position;

		if (expanded) {
			position += 0.1F;
		} else {
			position -= 0.1F;
		}

		position = Mth.clamp(position, 0F, 1F);
	}

	public static float smoothstep(float edge0, float edge1, float s) {
		// Scale, bias and saturate x to 0..1 range
		float x = Mth.clamp((s - edge0) / (edge1 - edge0), 0F, 1F);
		// Evaluate polynomial
		return x * x * (3F - 2F * x);
	}

	@Override
	public int getX() {
		//return (int) (width * smoothstep(0F, 1F, Mth.lerp(questScreen.getPartialTicks(), prevPosition, position)) - width);
		return expanded ? 0 : -width;
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		theme.drawContextMenuBackground(matrixStack, x, y, w, h);
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		matrixStack.pushPose();
		matrixStack.translate(0D, 0D, 850D);
		super.draw(matrixStack, theme, x, y, w, h);
		matrixStack.popPose();
	}

	public void setExpanded(boolean b) {
		expanded = b;
	}
}