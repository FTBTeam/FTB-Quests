package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.gui.ChangeChapterGroupScreen;
import dev.ftb.mods.ftbquests.net.CreateObjectPacket;
import dev.ftb.mods.ftbquests.net.MoveChapterGroupPacket;
import dev.ftb.mods.ftbquests.net.MoveChapterPacket;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class ChapterPanel extends Panel {
	public static final Icon ARROW_COLLAPSED = Icon.getIcon("ftbquests:textures/gui/arrow_collapsed.png");
	public static final Icon ARROW_EXPANDED = Icon.getIcon("ftbquests:textures/gui/arrow_expanded.png");

	public static abstract class ListButton extends Button {
		public final ChapterPanel chapterPanel;

		public ListButton(ChapterPanel panel, Component t, Icon i) {
			super(panel, t, i);
			setSize(100, 14);
			chapterPanel = panel;
		}

		public int getActualWidth(QuestScreen screen) {
			return screen.getTheme().getStringWidth(title) + 20;
		}

		@Override
		public void addMouseOverText(TooltipList list) {
		}

		@Nullable
		public Object getIngredientUnderMouse() {
			return icon.getIngredient();
		}
	}

	public static class ModpackButton extends ListButton {
		public ModpackButton(ChapterPanel panel, ClientQuestFile f) {
			super(panel, f.getTitle(), f.getIcon());
			setSize(100, 18);
		}

		@Override
		public void onClicked(MouseButton button) {
			if (chapterPanel.questScreen.file.canEdit() && getMouseX() > getX() + width - 15) {
				playClickSound();

				List<ContextMenuItem> contextMenu = new ArrayList<>();
				contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.chapter"), ThemeProperties.ADD_ICON.get(), () -> {
					StringConfig c = new StringConfig(Pattern.compile("^.+$"));
					EditConfigFromStringScreen.open(c, "", "", accepted -> {
						chapterPanel.questScreen.openGui();

						if (accepted && !c.value.isEmpty()) {
							Chapter chapter = new Chapter(chapterPanel.questScreen.file, chapterPanel.questScreen.file.defaultChapterGroup);
							chapter.title = c.value;
							CompoundTag extra = new CompoundTag();
							extra.putLong("group", 0L);
							new CreateObjectPacket(chapter, extra).sendToServer();
						}

						run();
					});
				}));

				contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.chapter_group"), ThemeProperties.ADD_ICON.get(), () -> {
					playClickSound();
					StringConfig c = new StringConfig(Pattern.compile("^.+$"));
					EditConfigFromStringScreen.open(c, "", "", accepted -> {
						chapterPanel.questScreen.openGui();

						if (accepted) {
							ChapterGroup group = new ChapterGroup(ClientQuestFile.INSTANCE);
							group.title = c.value;
							new CreateObjectPacket(group, null).sendToServer();
						}
					});
				}));

				chapterPanel.questScreen.openContextMenu(contextMenu);
			}
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();

			if (isMouseOver()) {
				Color4I.WHITE.withAlpha(40).draw(matrixStack, x + 1, y + 1, w - 2, h - 2);
			}

			ChatFormatting f = isMouseOver() ? ChatFormatting.WHITE : ChatFormatting.GRAY;

			icon.draw(matrixStack, x + 2, y + 3, 12, 12);
			theme.drawString(matrixStack, new TextComponent("").append(title).withStyle(f), x + 16, y + 5);

			ThemeProperties.WIDGET_BORDER.get(ClientQuestFile.INSTANCE).draw(matrixStack, x, y + h - 1, w, 1);

			boolean canEdit = chapterPanel.questScreen.file.canEdit();

			if (canEdit) {
				ThemeProperties.ADD_ICON.get().draw(matrixStack, x + w - 16, y + 3, 12, 12);
			}
		}

		@Override
		public int getActualWidth(QuestScreen screen) {
			boolean canEdit = chapterPanel.questScreen.file.canEdit();
			return screen.getTheme().getStringWidth(title) + 20 + (canEdit ? 16 : 0);
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			chapterPanel.questScreen.addInfoTooltip(list, chapterPanel.questScreen.file);
		}
	}

	public static class ChapterGroupButton extends ListButton {
		public final ChapterGroup group;
		public final List<Chapter> visibleChapters;

		public ChapterGroupButton(ChapterPanel panel, ChapterGroup g) {
			super(panel, g.getTitle(), g.getIcon());
			setSize(100, 18);
			group = g;
			visibleChapters = g.getVisibleChapters(panel.questScreen.file.self);
		}

		@Override
		public void onClicked(MouseButton button) {
			if (chapterPanel.questScreen.file.canEdit() && getMouseX() > getX() + width - 15) {
				playClickSound();

				StringConfig c = new StringConfig(Pattern.compile("^.+$"));
				EditConfigFromStringScreen.open(c, "", "", accepted -> {
					chapterPanel.questScreen.openGui();

					if (accepted && !c.value.isEmpty()) {
						Chapter chapter = new Chapter(chapterPanel.questScreen.file, chapterPanel.questScreen.file.defaultChapterGroup);
						chapter.title = c.value;
						CompoundTag extra = new CompoundTag();
						extra.putLong("group", group.id);
						new CreateObjectPacket(chapter, extra).sendToServer();
					}

					run();
				});

				return;
			}

			if (chapterPanel.questScreen.file.canEdit() && button.isRight() && !group.isDefaultGroup()) {
				List<ContextMenuItem> contextMenu = new ArrayList<>();
				contextMenu.add(new ContextMenuItem(new TranslatableComponent("gui.move"), ThemeProperties.MOVE_UP_ICON.get(), () -> new MoveChapterGroupPacket(group.id, true).sendToServer()).setEnabled(() -> group.getIndex() > 1).setCloseMenu(false));
				contextMenu.add(new ContextMenuItem(new TranslatableComponent("gui.move"), ThemeProperties.MOVE_DOWN_ICON.get(), () -> new MoveChapterGroupPacket(group.id, false).sendToServer()).setEnabled(() -> group.getIndex() < group.file.chapterGroups.size() - 1).setCloseMenu(false));
				contextMenu.add(ContextMenuItem.SEPARATOR);
				chapterPanel.questScreen.addObjectMenuItems(contextMenu, chapterPanel.questScreen, group);
				chapterPanel.questScreen.openContextMenu(contextMenu);
				return;
			}

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

			boolean canEdit = chapterPanel.questScreen.file.canEdit();

			if (canEdit) {
				ThemeProperties.ADD_ICON.get().draw(matrixStack, x + w - 14, y + 3, 12, 12);
			}
		}

		@Override
		public int getActualWidth(QuestScreen screen) {
			boolean canEdit = chapterPanel.questScreen.file.canEdit();
			return screen.getTheme().getStringWidth(title) + 20 + (canEdit ? 16 : 0);
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			chapterPanel.questScreen.addInfoTooltip(list, group);
		}
	}

	public static class ChapterButton extends ListButton {
		public final Chapter chapter;
		public List<Component> description;

		public ChapterButton(ChapterPanel panel, Chapter c) {
			super(panel, c.getTitle(), c.getIcon());
			chapter = c;

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
				description.add(new TextComponent("").append(FTBQuestsClient.parse(v)).withStyle(ChatFormatting.GRAY));
			}
		}

		@Override
		public void onClicked(MouseButton button) {
			if (chapterPanel.questScreen.file.canEdit() || !chapter.quests.isEmpty()) {
				playClickSound();

				if (chapterPanel.questScreen.selectedChapter != chapter) {
					chapterPanel.questScreen.open(chapter, false);
				}
			}

			if (chapterPanel.questScreen.file.canEdit() && button.isRight()) {
				List<ContextMenuItem> contextMenu = new ArrayList<>();
				contextMenu.add(new ContextMenuItem(new TranslatableComponent("gui.move"), ThemeProperties.MOVE_UP_ICON.get(), () -> new MoveChapterPacket(chapter.id, true).sendToServer()).setEnabled(() -> chapter.getIndex() > 0).setCloseMenu(false));
				contextMenu.add(new ContextMenuItem(new TranslatableComponent("gui.move"), ThemeProperties.MOVE_DOWN_ICON.get(), () -> new MoveChapterPacket(chapter.id, false).sendToServer()).setEnabled(() -> chapter.getIndex() < chapter.group.chapters.size() - 1).setCloseMenu(false));
				contextMenu.add(new ContextMenuItem(new TranslatableComponent("ftbquests.gui.change_group"), Icons.COLOR_RGB, () -> new ChangeChapterGroupScreen(chapter).openGui()));
				contextMenu.add(ContextMenuItem.SEPARATOR);
				chapterPanel.questScreen.addObjectMenuItems(contextMenu, chapterPanel.questScreen, chapter);
				chapterPanel.questScreen.openContextMenu(contextMenu);
			}
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();

			if (isMouseOver()) {
				Color4I.WHITE.withAlpha(40).draw(matrixStack, x + 1, y, w - 2, h);
			}

			Color4I c = chapter.getProgressColor(chapterPanel.questScreen.file.self, !isMouseOver());
			int o = chapter.group.isDefaultGroup() ? 0 : 7;

			icon.draw(matrixStack, x + 2 + o, y + 1, 12, 12);
			theme.drawString(matrixStack, new TextComponent("").append(title).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(c.rgb()))), x + 16 + o, y + 3);

			GuiHelper.setupDrawing();

			if (chapter.quests.isEmpty()) {
				ThemeProperties.CLOSE_ICON.get().draw(matrixStack, x + w - 12, y + 3, 8, 8);
			} else if (chapterPanel.questScreen.file.self.hasUnclaimedRewards(Minecraft.getInstance().player.getUUID(), chapter)) {
				ThemeProperties.ALERT_ICON.get().draw(matrixStack, x + w - 12, y + 3, 8, 8);
			}
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			chapterPanel.questScreen.addInfoTooltip(list, chapter);

			for (Component s : description) {
				list.add(s);
			}
		}

		@Override
		public int getActualWidth(QuestScreen screen) {
			int o = chapter.group.isDefaultGroup() ? 0 : 7;

			if (chapter.quests.isEmpty() || chapterPanel.questScreen.file.self.hasUnclaimedRewards(Minecraft.getInstance().player.getUUID(), chapter)) {
				o += 16;
			}

			return screen.getTheme().getStringWidth(title) + 20 + o;
		}
	}

	public final QuestScreen questScreen;
	public boolean expanded = false;

	public ChapterPanel(Panel panel) {
		super(panel);
		questScreen = (QuestScreen) panel.getGui();
	}

	@Override
	public void addWidgets() {
		add(new ModpackButton(this, questScreen.file));

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

		if (canEdit) {
			//add(new AddChapterButton(this, questScreen.file.defaultChapterGroup));
		}

		for (ChapterGroup group : questScreen.file.chapterGroups) {
			if (group.isDefaultGroup()) {
				continue;
			}

			ChapterGroupButton b = new ChapterGroupButton(this, group);

			if (canEdit || !b.visibleChapters.isEmpty()) {
				add(b);

				if (!group.guiCollapsed) {
					for (Chapter chapter : b.visibleChapters) {
						add(new ChapterButton(this, chapter));
					}
				}
			}

			if (canEdit) {
				//add(new AddChapterButton(this, group));
			}
		}
	}

	@Override
	public void alignWidgets() {
		int wd = 100;

		for (Widget w : widgets) {
			wd = Math.min(Math.max(wd, ((ListButton) w).getActualWidth(questScreen)), 800);
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
	public int getX() {
		return expanded ? 0 : -width;
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		theme.drawContextMenuBackground(matrixStack, x, y, w, h);
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		matrixStack.pushPose();
		matrixStack.translate(0, 0, 600);
		RenderSystem.enableDepthTest();
		super.draw(matrixStack, theme, x, y, w, h);
		matrixStack.popPose();
	}

	public void setExpanded(boolean b) {
		expanded = b;
	}
}