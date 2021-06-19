package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.BlankPanel;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ColorWidget;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.VerticalSpaceWidget;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.CompactGridLayout;
import dev.ftb.mods.ftblibrary.util.ImageComponent;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.gui.ImageComponentWidget;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ViewQuestPanel extends Panel {
	public final QuestScreen questScreen;
	public Quest quest = null;
	public boolean hidePanel = false;
	private Icon icon = Icon.EMPTY;
	public Button buttonClose;
	public Button buttonPin;
	public Button buttonOpenDependencies;
	public Button buttonOpenDependants;
	public BlankPanel panelContent;
	public BlankPanel panelTasks;
	public BlankPanel panelRewards;
	public BlankPanel panelText;

	public ViewQuestPanel(QuestScreen g) {
		super(g);
		questScreen = g;
		setPosAndSize(-1, -1, 0, 0);
		setOnlyRenderWidgetsInside(true);
		setOnlyInteractWithWidgetsInside(true);
	}

	@Override
	public void addWidgets() {
		setPosAndSize(-1, -1, 1, 1);

		if (quest == null || hidePanel) {
			return;
		}

		QuestObjectBase prev = QuestTheme.currentObject;
		QuestTheme.currentObject = quest;

		setScrollX(0);
		setScrollY(0);

		icon = quest.getIcon();

		boolean canEdit = questScreen.file.canEdit();

		TextField titleField = new TextField(this) {
			@Override
			public boolean mousePressed(MouseButton button) {
				if (canEdit && button.isRight()) {
					editTitle();
					return true;
				}

				return super.mousePressed(button);
			}
		}.addFlags(Theme.CENTERED).setMinWidth(150).setMaxWidth(500).setSpacing(9).setText(new TextComponent("").append(quest.getTitle()).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(ThemeProperties.QUEST_VIEW_TITLE.get().rgb()))));

		int w = Math.max(200, titleField.width + 54);

		titleField.setPosAndSize(27, 4, w - 54, 8);
		add(titleField);

		add(panelContent = new BlankPanel(this, "ContentPanel"));
		panelContent.add(panelTasks = new BlankPanel(panelContent, "TasksPanel"));
		panelContent.add(panelRewards = new BlankPanel(panelContent, "RewardsPanel"));
		panelContent.add(panelText = new BlankPanel(panelContent, "TextPanel"));

		int bsize = 18;

		for (Task task : quest.tasks) {
			TaskButton b = new TaskButton(panelTasks, task);
			panelTasks.add(b);
			b.setSize(bsize, bsize);
		}

		if (!canEdit && panelTasks.widgets.isEmpty()) {
			DisabledButtonTextField noTasks = new DisabledButtonTextField(panelTasks, new TranslatableComponent("ftbquests.gui.no_tasks"));
			noTasks.setSize(noTasks.width + 8, bsize);
			noTasks.setColor(ThemeProperties.DISABLED_TEXT_COLOR.get(quest));
			panelTasks.add(noTasks);
		}

		for (Reward reward : quest.rewards) {
			if (canEdit || reward.getAutoClaimType() != RewardAutoClaim.INVISIBLE) {
				RewardButton b = new RewardButton(panelRewards, reward);
				panelRewards.add(b);
				b.setSize(bsize, bsize);
			}
		}

		if (!canEdit && panelRewards.widgets.isEmpty()) {
			DisabledButtonTextField noRewards = new DisabledButtonTextField(panelRewards, new TranslatableComponent("ftbquests.gui.no_rewards"));
			noRewards.setSize(noRewards.width + 8, bsize);
			noRewards.setColor(ThemeProperties.DISABLED_TEXT_COLOR.get(quest));
			panelRewards.add(noRewards);
		}

		if (questScreen.file.canEdit()) {
			panelTasks.add(new AddTaskButton(panelTasks, quest));
			panelRewards.add(new AddRewardButton(panelRewards, quest));
		}

		int ww = 0;

		for (Widget widget : panelTasks.widgets) {
			ww = Math.max(ww, widget.width);
		}

		for (Widget widget : panelRewards.widgets) {
			ww = Math.max(ww, widget.width);
		}

		Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(questScreen.selectedChapter);

		ww = Mth.clamp(ww, 70, 140);
		w = Math.max(w, ww * 2 + 10);
		w = Math.max(w, quest.minWidth);

		if (ThemeProperties.FULL_SCREEN_QUEST.get(quest) == 1) {
			w = questScreen.width - 1;
		}

		if (w % 2 == 0) {
			w++;
		}

		setWidth(w);
		panelContent.setPosAndSize(0, 16, w, 0);

		int w2 = w / 2;

		add(buttonClose = new CloseViewQuestButton(this));
		buttonClose.setPosAndSize(w - 14, 2, 12, 12);

		add(buttonPin = new PinViewQuestButton(this));
		buttonPin.setPosAndSize(w - 26, 2, 12, 12);

		if (quest.dependencies.isEmpty()) {
			add(buttonOpenDependencies = new SimpleButton(this, new TranslatableComponent("ftbquests.gui.no_dependencies"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_left.png").withTint(borderColor), (widget, button) -> {
			}));
		} else {
			add(buttonOpenDependencies = new SimpleButton(this, new TranslatableComponent("ftbquests.gui.view_dependencies"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_left.png").withTint(borderColor), (widget, button) -> showList(quest.dependencies)));
		}

		if (quest.getDependants().isEmpty()) {
			add(buttonOpenDependants = new SimpleButton(this, new TranslatableComponent("ftbquests.gui.no_dependants"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_right.png").withTint(borderColor), (widget, button) -> {
			}));
		} else {
			add(buttonOpenDependants = new SimpleButton(this, new TranslatableComponent("ftbquests.gui.view_dependants"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_right.png").withTint(borderColor), (widget, button) -> showList(quest.getDependants())));
		}

		buttonOpenDependencies.setPosAndSize(0, 17, 13, 13);
		buttonOpenDependants.setPosAndSize(w - 13, 17, 13, 13);

		TextField textFieldTasks = new TextField(panelContent) {
			@Override
			public TextField resize(Theme theme) {
				return this;
			}
		};

		textFieldTasks.setPosAndSize(2, 2, w2 - 3, 13);
		textFieldTasks.setMaxWidth(w);
		textFieldTasks.addFlags(Theme.CENTERED | Theme.CENTERED_V);
		textFieldTasks.setText(new TranslatableComponent("ftbquests.tasks"));
		textFieldTasks.setColor(ThemeProperties.TASKS_TEXT_COLOR.get(quest));
		panelContent.add(textFieldTasks);

		TextField textFieldRewards = new TextField(panelContent) {
			@Override
			public TextField resize(Theme theme) {
				return this;
			}
		};

		textFieldRewards.setPosAndSize(w2 + 2, 2, w2 - 3, 13);
		textFieldRewards.setMaxWidth(w);
		textFieldRewards.addFlags(Theme.CENTERED | Theme.CENTERED_V);
		textFieldRewards.setText(new TranslatableComponent("ftbquests.rewards"));
		textFieldRewards.setColor(ThemeProperties.REWARDS_TEXT_COLOR.get(quest));
		panelContent.add(textFieldRewards);

		panelTasks.setPosAndSize(2, 16, w2 - 3, 0);
		panelRewards.setPosAndSize(w2 + 2, 16, w2 - 3, 0);

		int at = panelTasks.align(new CompactGridLayout(bsize + 2));
		int ar = panelRewards.align(new CompactGridLayout(bsize + 2));

		int h = Math.max(at, ar);
		panelTasks.setHeight(h);
		panelRewards.setHeight(h);

		int tox = (panelTasks.width - panelTasks.getContentWidth()) / 2;
		int rox = (panelRewards.width - panelRewards.getContentWidth()) / 2;
		int toy = (panelTasks.height - panelTasks.getContentHeight()) / 2;
		int roy = (panelRewards.height - panelRewards.getContentHeight()) / 2;

		for (Widget widget : panelTasks.widgets) {
			widget.setX(widget.posX + tox);
			widget.setY(widget.posY + toy);
		}

		for (Widget widget : panelRewards.widgets) {
			widget.setX(widget.posX + rox);
			widget.setY(widget.posY + roy);
		}

		panelText.setPosAndSize(3, 16 + h + 12, w - 6, 0);

		Component subtitle = quest.getSubtitle();

		if (subtitle == TextComponent.EMPTY && canEdit) {
			subtitle = new TextComponent("[No Subtitle]");
		}

		if (subtitle != TextComponent.EMPTY) {
			panelText.add(new TextField(panelText) {
				@Override
				public boolean mousePressed(MouseButton button) {
					if (canEdit && button.isRight()) {
						editSubtitle();
						return true;
					}

					return super.mousePressed(button);
				}
			}.addFlags(Theme.CENTERED).setMinWidth(panelText.width).setMaxWidth(panelText.width).setSpacing(9).setText(new TextComponent("").append(subtitle).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY)));
		}

		boolean showText = !quest.hideTextUntilComplete.get(false) || questScreen.file.self != null && questScreen.file.self.isCompleted(quest);

		if (showText && quest.getDescription().length > 0) {
			if (subtitle != TextComponent.EMPTY) {
				panelText.add(new VerticalSpaceWidget(panelText, 7));
			}

			// panelText.add(new ComponentTextField(panelText).setMaxWidth(panelText.width).setSpacing(9).setText(quest.getJoinedDescription()));

			for (Component component : quest.getDescription()) {
				if (component instanceof ImageComponent) {
					ImageComponentWidget c = new ImageComponentWidget(panelText, (ImageComponent) component);

					if (c.component.fit) {
						double scale = panelText.width / (double) c.width;
						c.setSize((int) (c.width * scale), (int) (c.height * scale));
					} else if (c.component.align == 1) {
						c.setX((panelText.width - c.width) / 2);
					} else if (c.component.align == 2) {
						c.setX(panelText.width - c.width);
					} else {
						c.setX(0);
					}

					panelText.add(c);
				} else {
					panelText.add(new TextField(panelText).setMaxWidth(panelText.width).setSpacing(9).setText(component));
				}
			}
		}

		if (showText && !quest.guidePage.isEmpty()) {
			if (subtitle != TextComponent.EMPTY) {
				panelText.add(new VerticalSpaceWidget(panelText, 7));
			}

			panelText.add(new OpenInGuideButton(panelText, quest));
		}

		if (canEdit) {
			/*
			SimpleTextButton add = new SimpleTextButton(panelText, new TranslatableComponent("gui.add"), Icons.ADD) {
				@Override
				public void onClicked(MouseButton mouseButton) {
					addDescLine();
				}
			};

			add.setHeight(14);
			panelText.add(add);
			 */
		}

		if (panelText.widgets.isEmpty()) {
			panelContent.add(new ColorWidget(panelContent, borderColor, null).setPosAndSize(w2, 0, 1, h + 40));
			panelText.setHeight(0);
			setHeight(Math.min(panelContent.getContentHeight(), parent.height - 10));
		} else {
			panelContent.add(new ColorWidget(panelContent, borderColor, null).setPosAndSize(w2, 0, 1, 16 + h + 6));
			panelContent.add(new ColorWidget(panelContent, borderColor, null).setPosAndSize(1, 16 + h + 6, w - 2, 1));
			panelText.setHeight(panelText.align(new WidgetLayout.Vertical(0, 1, 1)));
			setHeight(Math.min(panelContent.getContentHeight() + 20, parent.height - 10));
		}

		if (ThemeProperties.FULL_SCREEN_QUEST.get(quest) == 1) {
			height = questScreen.height;
		}

		setPos((parent.width - width) / 2, (parent.height - height) / 2);
		panelContent.setHeight(height - 17);

		QuestTheme.currentObject = prev;
	}

	@Override
	public void alignWidgets() {
	}

	private void showList(Collection<QuestObject> c) {
		int hidden = 0;
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (QuestObject object : c) {
			if (questScreen.file.canEdit() || object.isVisible(questScreen.file.self)) {
				contextMenu.add(new ContextMenuItem(object.getTitle(), Icon.EMPTY, () -> questScreen.open(object, true)));
			} else {
				hidden++;
			}
		}

		if (hidden > 0) {
			if (hidden == c.size()) {
				contextMenu.add(new ContextMenuItem(new TextComponent(hidden + " hidden quests"), Icon.EMPTY, () -> {
				}).setEnabled(false));
			} else {
				contextMenu.add(new ContextMenuItem(new TextComponent("+ " + hidden + " hidden quests"), Icon.EMPTY, () -> {
				}).setEnabled(false));
			}
		}

		getGui().openContextMenu(contextMenu);
	}

	private void editSubtitle() {
	}

	private void editTitle() {
	}

	private void addDescLine() {
		List<ContextMenuItem> contextMenu = new ArrayList<>();
		contextMenu.add(new ContextMenuItem(new TextComponent("Text"), Icon.EMPTY, () -> {
		}));
		contextMenu.add(new ContextMenuItem(new TextComponent("Image"), Icon.EMPTY, () -> {
		}));

		getGui().openContextMenu(contextMenu);
	}

	private void editDescLine(int line) {
		List<ContextMenuItem> contextMenu = new ArrayList<>();
		contextMenu.add(new ContextMenuItem(new TranslatableComponent("gui.edit"), Icon.EMPTY, () -> {
		}));
		contextMenu.add(new ContextMenuItem(new TranslatableComponent("gui.remove"), Icon.EMPTY, () -> {
		}));

		getGui().openContextMenu(contextMenu);
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		if (quest != null && !hidePanel) {
			QuestObjectBase prev = QuestTheme.currentObject;
			QuestTheme.currentObject = quest;
			matrixStack.pushPose();
			matrixStack.translate(0, 0, 500);
			super.draw(matrixStack, theme, x, y, w, h);
			matrixStack.popPose();
			QuestTheme.currentObject = prev;
		}
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		Color4I borderColor = ThemeProperties.QUEST_VIEW_BORDER.get();
		Color4I.DARK_GRAY.withAlpha(120).draw(matrixStack, questScreen.getX(), questScreen.getY(), questScreen.width, questScreen.height);
		Icon background = ThemeProperties.QUEST_VIEW_BACKGROUND.get();
		background.draw(matrixStack, x, y, w, h);
		icon.draw(matrixStack, x + 2, y + 2, 12, 12);
		borderColor.draw(matrixStack, x + 1, y + 15, w - 2, 1);
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		return super.mousePressed(button) || isMouseOver();
	}
}