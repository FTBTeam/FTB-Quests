package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ImageConfig;
import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.CompactGridLayout;
import dev.ftb.mods.ftblibrary.util.ImageComponent;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.gui.ImageComponentWidget;
import dev.ftb.mods.ftbquests.gui.MultilineTextEditorScreen;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.net.TogglePinnedMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

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
	private TextField titleField;

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

		titleField = new QuestDescriptionField(this, canEdit, b -> editTitle())
				.addFlags(Theme.CENTERED)
				.setMinWidth(150).setMaxWidth(500).setSpacing(9)
				.setText(quest.getTitle().copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(ThemeProperties.QUEST_VIEW_TITLE.get().rgb()))));
		int w = Math.max(200, titleField.width + 54);

		if (quest.minWidth > 0) {
			w = Math.max(quest.minWidth, w);
		} else if (questScreen.selectedChapter.defaultMinWidth > 0) {
			w = Math.max(questScreen.selectedChapter.defaultMinWidth, w);
		}

		titleField.setPosAndSize(27, 4, w - 54, titleField.height);
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
			if (canEdit || !questScreen.file.self.isRewardBlocked(reward) && reward.getAutoClaimType() != RewardAutoClaim.INVISIBLE) {
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

		if (ThemeProperties.FULL_SCREEN_QUEST.get(quest) == 1) {
			w = questScreen.width - 1;
		}

		if (w % 2 == 0) {
			w++;
		}

		setWidth(w);
		panelContent.setPosAndSize(0, Math.max(16, titleField.height + 8), w, 0);

		int iconSize = Math.min(16, titleField.height + 2);

		add(buttonClose = new CloseViewQuestButton());
		buttonClose.setPosAndSize(w - iconSize - 2, 4, iconSize, iconSize);

		add(buttonPin = new PinViewQuestButton());
		buttonPin.setPosAndSize(w - iconSize * 2 - 4, 4, iconSize, iconSize);

		if (questScreen.selectedChapter.id != quest.chapter.id) {
			GotoLinkedQuestButton b = new GotoLinkedQuestButton();
			add(b);
			b.setPosAndSize(iconSize + 4, 0, iconSize, iconSize);
		}

		List<QuestLink> links = new ArrayList<>();
		questScreen.file.chapterGroups.forEach(group -> group.chapters
				.forEach(chapter -> chapter.questLinks.stream()
						.filter(link -> chapter != questScreen.selectedChapter && link.linksTo(quest))
						.forEach(links::add)
				)
		);
		var linksButton = new ViewQuestLinksButton(links);
		add(linksButton);
		linksButton.setPosAndSize(w - iconSize * 3 - 4, 0, iconSize, iconSize);

		if (!quest.hasDependencies()) {
			add(buttonOpenDependencies = new SimpleButton(this, new TranslatableComponent("ftbquests.gui.no_dependencies"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_left.png").withTint(borderColor), (widget, button) -> {
			}));
		} else {
			add(buttonOpenDependencies = new SimpleButton(this, new TranslatableComponent("ftbquests.gui.view_dependencies"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_left.png").withTint(ThemeProperties.QUEST_VIEW_TITLE.get()), (widget, button) -> showList(quest.getDependencies().toList(), true)));
		}

		if (quest.getDependants().isEmpty()) {
			add(buttonOpenDependants = new SimpleButton(this, new TranslatableComponent("ftbquests.gui.no_dependants"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_right.png").withTint(borderColor), (widget, button) -> {
			}));
		} else {
			add(buttonOpenDependants = new SimpleButton(this, new TranslatableComponent("ftbquests.gui.view_dependants"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_right.png").withTint(ThemeProperties.QUEST_VIEW_TITLE.get()), (widget, button) -> showList(quest.getDependants(), false)));
		}

		buttonOpenDependencies.setPosAndSize(0, panelContent.posY + 2, 13, 13);
		buttonOpenDependants.setPosAndSize(w - 13, panelContent.posY + 2, 13, 13);

		TextField textFieldTasks = new TextField(panelContent) {
			@Override
			public TextField resize(Theme theme) {
				return this;
			}
		};

		int w2 = w / 2;

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
			panelText.add(new QuestDescriptionField(panelText, canEdit, b -> editSubtitle())
					.addFlags(Theme.CENTERED)
					.setMinWidth(panelText.width).setMaxWidth(panelText.width)
					.setSpacing(9)
					.setText(new TextComponent("").append(subtitle).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY)));
		}

		boolean showText = !quest.hideTextUntilComplete.get(false) || questScreen.file.self != null && questScreen.file.self.isCompleted(quest);

		if (showText && quest.getDescription().length > 0) {
			if (subtitle != TextComponent.EMPTY) {
				panelText.add(new VerticalSpaceWidget(panelText, 7));
			}

			for (int i = 0; i < quest.getDescription().length; i++) {
				Component component = quest.getDescription()[i];

				if (component instanceof ImageComponent) {
					ImageComponentWidget c = new ImageComponentWidget(this, panelText, (ImageComponent) component, i);

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
					final int line = i;
					TextField field = new QuestDescriptionField(panelText, canEdit, context -> editDescLine(line, context, null))
							.setMaxWidth(panelText.width).setSpacing(9).setText(component);
					field.setWidth(panelText.width);
					panelText.add(field);
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
			panelText.add(new VerticalSpaceWidget(panelText, 3));

			SimpleTextButton add = new SimpleTextButton(panelText, new TranslatableComponent("gui.add"), ThemeProperties.ADD_ICON.get()) {
				@Override
				public void onClicked(MouseButton mouseButton) {
					addDescLine();
				}
			};

			add.setX((panelText.width - add.width) / 2);
			add.setHeight(14);
			panelText.add(add);
		}

		if (panelText.widgets.isEmpty()) {
			panelContent.add(new ColorWidget(panelContent, borderColor, null).setPosAndSize(w2, 0, 1, h + 40));
			panelText.setHeight(0);
			setHeight(Math.min(panelContent.getContentHeight(), parent.height - 10));
		} else {
			panelContent.add(new ColorWidget(panelContent, borderColor, null).setPosAndSize(w2, 0, 1, 16 + h + 6));
			panelContent.add(new ColorWidget(panelContent, borderColor, null).setPosAndSize(1, 16 + h + 6, w - 2, 1));
			panelText.setHeight(panelText.align(new WidgetLayout.Vertical(0, 1, 2)));
			setHeight(Math.min(panelContent.getContentHeight() + titleField.height + 12, parent.height - 10));
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

	@Override
	public void tick() {
		super.tick();

		if (quest != null && quest.hasDependencies() && !questScreen.file.self.canStartTasks(quest) && buttonOpenDependencies != null) {
			float red = Mth.sin((System.currentTimeMillis() % 1200) * (3.1415927f / 1200f));
			Color4I col = Color4I.rgb((int) (red * 127 + 63), 0, 0);
			buttonOpenDependencies.setIcon(Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_left.png").withTint(col));
		}
	}

	private void showList(Collection<QuestObject> c, boolean dependencies) {
		int hidden = 0;
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		if (dependencies && quest.minRequiredDependencies > 0) {
			contextMenu.add(new ContextMenuItem(
					new TranslatableComponent("ftbquests.quest.min_required_header", quest.minRequiredDependencies)
							.withStyle(ChatFormatting.UNDERLINE), Icon.EMPTY, null).setEnabled(false)
			);
		}

		for (QuestObject object : c) {
			if (questScreen.file.canEdit() || object.isVisible(questScreen.file.self)) {
				MutableComponent title = object.getMutableTitle();
				if (object.getQuestChapter() != null && object.getQuestChapter() != quest.getQuestChapter()) {
					Component suffix = new TextComponent(" [").append(object.getQuestChapter().getTitle()).append("]").withStyle(ChatFormatting.GRAY);
					title.append(suffix);
				}
				contextMenu.add(new ContextMenuItem(title, Icon.EMPTY, () -> questScreen.open(object, true)));
			} else {
				hidden++;
			}
		}

		if (hidden > 0) {
			MutableComponent prefix = hidden == c.size() ? TextComponent.EMPTY.copy() : new TextComponent("+ ");
			contextMenu.add(new ContextMenuItem(
					prefix.append(new TranslatableComponent("ftbquests.quest.hidden_quests_footer", hidden)), Icon.EMPTY, null).setEnabled(false)
			);
		}

		getGui().openContextMenu(contextMenu);
	}

	@Override
	public void keyReleased(Key key) {
		// released rather than pressed; if we used pressed, keypress would be picked up by the next screen

		if (hidePanel || quest == null) return;

		if (key.is(GLFW.GLFW_KEY_S)) {
			editSubtitle();
		} else if (key.is(GLFW.GLFW_KEY_T)) {
			editTitle();
		} else if (key.is(GLFW.GLFW_KEY_D)) {
			ListConfig<String,StringConfig> lc = new ListConfig<>(new StringConfig());
			lc.value = quest.description;
			new MultilineTextEditorScreen(lc, accepted -> {
				if (accepted) {
					new EditObjectMessage(quest).sendToServer();
				}
				openGui();
			}).openGui();
		}
	}

	private void editTitle() {
		StringConfig c = new StringConfig(null);

		// pressing T while mousing over a task button allows editing the task title
		QuestObject qo = quest;
		String titleKey = "ftbquests.title";
		for (Widget w : panelTasks.widgets) {
			if (w instanceof TaskButton b && b.isMouseOver()) {
				qo = b.task;
				titleKey = "ftbquests.task_title";
				break;
			}
		}

		final var qo1 = qo;
		EditConfigFromStringScreen.open(c, qo1.title, "", new TranslatableComponent(titleKey), accepted -> {
			if (accepted) {
				qo1.title = c.value;
				new EditObjectMessage(qo1).sendToServer();
			}

			openGui();
		});
	}

	private void editSubtitle() {
		StringConfig c = new StringConfig(null);

		EditConfigFromStringScreen.open(c, quest.subtitle, "", new TranslatableComponent("ftbquests.quest.subtitle"), accepted -> {
			if (accepted) {
				quest.subtitle = c.value;
				new EditObjectMessage(quest).sendToServer();
			}

			openGui();
		});
	}

	private void addDescLine() {
		List<ContextMenuItem> contextMenu = new ArrayList<>();
		contextMenu.add(new ContextMenuItem(new TextComponent("Text"), Icons.NOTES, () -> editDescLine0(-1, null)));
		contextMenu.add(new ContextMenuItem(new TextComponent("Image"), Icons.ART, () -> editDescLine0(-1, new ImageComponent())));
		getGui().openContextMenu(contextMenu);
	}

	private void editDescLine0(int line, @Nullable Object type) {
		if (type instanceof ImageComponent) {
			editImage(line, (ImageComponent) type);
			return;
		}

		StringConfig c = new StringConfig(null);

		int l = line + 1;
		int s = quest.description.size();
		if (l == 0) {
			// adding a new line
			l = quest.description.size() + 1;
			s++;
		}
		Component title = new TranslatableComponent("ftbquests.quest.description").append(String.format(": %d/%d", l, s));

		EditConfigFromStringScreen.open(c, line == -1 ? "" : quest.description.get(line), "", title, accepted -> {
			if (accepted) {
				if (line == -1) {
					quest.description.add(c.value);
				} else {
					quest.description.set(line, c.value);
				}

				new EditObjectMessage(quest).sendToServer();
			}

			openGui();
		});
	}

	private void editImage(int line, ImageComponent component) {
		ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
		//task.getConfig(task.createSubGroup(group));

		group.add("image", new ImageConfig(), component.image.toString(), v -> component.image = Icon.getIcon(v), "");
		group.addInt("width", component.width, v -> component.width = v, 0, 1, 1000);
		group.addInt("height", component.height, v -> component.height = v, 0, 1, 1000);
		group.addInt("align", component.align, v -> component.align = v, 0, 1, 2);
		group.addBool("fit", component.fit, v -> component.fit = v, false);

		group.savedCallback = accepted -> {
			openGui();
			if (accepted) {
				if (line == -1) {
					quest.description.add(component.toString());
				} else {
					quest.description.set(line, component.toString());
				}
				new EditObjectMessage(quest).sendToServer();
			}
		};

		new EditConfigScreen(group).openGui();
	}

	public void editDescLine(int line, boolean context, @Nullable Object type) {
		if (context) {
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			//contextMenu.add(new ContextMenuItem(new TranslatableComponent("gui.move"), ThemeProperties.MOVE_UP_ICON.get(), () -> {}).setEnabled(() -> chapter.getIndex() > 0));
			//contextMenu.add(new ContextMenuItem(new TranslatableComponent("gui.move"), ThemeProperties.MOVE_DOWN_ICON.get(), () -> {}).setEnabled(() -> chapter.getIndex() < chapter.group.chapters.size() - 1));

			contextMenu.add(new ContextMenuItem(new TranslatableComponent("selectServer.edit"), ThemeProperties.EDIT_ICON.get(), () -> editDescLine0(line, type)));

			contextMenu.add(new ContextMenuItem(new TranslatableComponent("selectServer.delete"), ThemeProperties.DELETE_ICON.get(), () -> {
				quest.description.remove(line);
				new EditObjectMessage(quest).sendToServer();
			}));

			getGui().openContextMenu(contextMenu);
		} else {
			editDescLine0(line, type);
		}
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
		int iconSize = Math.min(16, titleField.height + 2);
		icon.draw(matrixStack, x + 4, y + 4, iconSize, iconSize);
		borderColor.draw(matrixStack, x + 1, panelContent.getY(), w - 2, 1);
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		return super.mousePressed(button) || isMouseOver();
	}

	private class QuestDescriptionField extends TextField {
		private final boolean canEdit;
		private final Consumer<Boolean> editCallback;

		QuestDescriptionField(Panel panel, boolean canEdit, Consumer<Boolean> editCallback) {
			super(panel);
			this.canEdit = canEdit;
			this.editCallback = editCallback;
		}

		@Override
		public boolean mousePressed(MouseButton button) {
			if (isMouseOver()) {
				if (canEdit && button.isRight()) {
					editCallback.accept(true);
					return true;
				} else if (button.isLeft() && Minecraft.getInstance().screen != null) {
					Style style = getComponentStyleAt(questScreen.getTheme(), getMouseX(), getMouseY());
					if (style != null) {
						return handleCustomClickEvent(style) || Minecraft.getInstance().screen.handleComponentClicked(style);
					}
				}
			}

			return super.mousePressed(button);
		}

		private boolean handleCustomClickEvent(Style style) {
			if (style == null) return false;

			ClickEvent clickEvent = style.getClickEvent();
			if (clickEvent == null) return false;

			if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
				try {
					long questId = Long.valueOf(clickEvent.getValue(), 16);
					QuestObject qo = FTBQuests.PROXY.getQuestFile(true).get(questId);
					if (qo != null) {
						questScreen.open(qo, false);
					} else {
						errorToPlayer("Unknown quest object id: %s", clickEvent.getValue());
					}
				} catch (NumberFormatException e) {
					errorToPlayer("Invalid quest object id: %s (%s)",clickEvent.getValue(), e.getMessage());
				}
				return true;
			} else if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
				try {
					URI uri = new URI(clickEvent.getValue());
					String scheme = uri.getScheme();
					if (scheme == null) {
						throw new URISyntaxException(clickEvent.getValue(), "Missing protocol");
					}
					if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
						throw new URISyntaxException(clickEvent.getValue(), "Unsupported protocol: " + scheme.toLowerCase(Locale.ROOT));
					}

					final Screen curScreen = Minecraft.getInstance().screen;
					Minecraft.getInstance().setScreen(new ConfirmLinkScreen(accepted -> {
						if (accepted) {
							Util.getPlatform().openUri(uri);
						}
						Minecraft.getInstance().setScreen(curScreen);
					}, clickEvent.getValue(), false));
					return true;
				} catch (URISyntaxException e) {
					errorToPlayer("Can't open url for %s (%s)", clickEvent.getValue(), e.getMessage());
				}
				return true;
			}
			return false;
		}

		private void errorToPlayer(String msg, Object... args) {
			QuestScreen.displayError(new TextComponent(String.format(msg, args)).withStyle(ChatFormatting.RED));
		}

		@Override
		public boolean mouseDoubleClicked(MouseButton button) {
			if (isMouseOver() && canEdit) {
				editCallback.accept(false);
				return true;
			}

			return false;
		}

		@Override
		@Nullable
		public CursorType getCursor() {
			return canEdit ? CursorType.IBEAM : null;
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			if (!isMouseOver()) return;

			super.addMouseOverText(list);

			Style style = getComponentStyleAt(questScreen.getTheme(), getMouseX(), getMouseY());
			if (style != null && style.getHoverEvent() != null) {
				HoverEvent hoverevent = style.getHoverEvent();
				HoverEvent.ItemStackInfo stackInfo = hoverevent.getValue(HoverEvent.Action.SHOW_ITEM);
				Minecraft mc = Minecraft.getInstance();
				TooltipFlag flag = mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
				if (stackInfo != null) {
					stackInfo.getItemStack().getTooltipLines(mc.player, flag).forEach(list::add);
				} else {
					HoverEvent.EntityTooltipInfo entityInfo = hoverevent.getValue(HoverEvent.Action.SHOW_ENTITY);
					if (entityInfo != null) {
						if (flag.isAdvanced()) {
							entityInfo.getTooltipLines().forEach(list::add);
						}
					} else {
						Component component = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);
						if (component != null) {
							list.add(component);
						}
					}
				}
			}
		}
	}

	private abstract class AbstractPanelButton extends SimpleTextButton {
		public AbstractPanelButton(Component txt, Icon icon) {
			super(ViewQuestPanel.this, txt, icon);
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			drawIcon(matrixStack, theme, x + 1, y + 1, w - 2, h - 2);
		}
	}

	private class GotoLinkedQuestButton extends AbstractPanelButton {
		public GotoLinkedQuestButton() {
			super(new TranslatableComponent("ftbquests.gui.goto_linked_quest", quest.chapter.getTitle().copy().withStyle(ChatFormatting.YELLOW)), ThemeProperties.LINK_ICON.get());
		}

		@Override
		public void onClicked(MouseButton button) {
			double qx = quest.x + 0.5D;
			double qy = quest.y + 0.5D;
			questScreen.selectChapter(quest.chapter);
			questScreen.questPanel.scrollTo(qx, qy);
		}
	}

	private class ViewQuestLinksButton extends AbstractPanelButton {
		private final List<QuestLink> links;

		public ViewQuestLinksButton(Collection<QuestLink> links) {
			super(new TranslatableComponent("ftbquests.gui.view_quest_links"), ThemeProperties.LINK_ICON.get());
			this.links = List.copyOf(links);
		}

		@Override
		public void onClicked(MouseButton button) {
			List<ContextMenuItem> items = new ArrayList<>();
			for (QuestLink link : links) {
				link.getQuest().ifPresent(quest -> {
					Component title = quest.getTitle().copy().append(": ").append(link.getChapter().getTitle().copy().withStyle(ChatFormatting.YELLOW));
					items.add(new ContextMenuItem(title, quest.getIcon(), () -> gotoLink(link)));
				});
			}
			if (!items.isEmpty()) {
				ViewQuestPanel.this.questScreen.openContextMenu(items);
			}
		}

		private void gotoLink(QuestLink link) {
			questScreen.closeQuest();
			questScreen.selectChapter(link.getChapter());
			questScreen.questPanel.scrollTo(link.getX() + 0.5D, link.getX() + 0.5D);
		}

		@Override
		public boolean isEnabled() {
			return !links.isEmpty();
		}

		@Override
		public boolean shouldDraw() {
			return !links.isEmpty();
		}
	}

	/**
	 * @author LatvianModder
	 */
	private class PinViewQuestButton extends AbstractPanelButton {
		private PinViewQuestButton() {
			super(new TranslatableComponent(questScreen.file.self.pinnedQuests.contains(quest.id) ? "ftbquests.gui.unpin" : "ftbquests.gui.pin"),
					questScreen.file.self.pinnedQuests.contains(quest.id) ? ThemeProperties.PIN_ICON_ON.get() : ThemeProperties.PIN_ICON_OFF.get());
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			new TogglePinnedMessage(quest.id).sendToServer();
		}
	}

	/**
	 * @author LatvianModder
	 */
	private class CloseViewQuestButton extends AbstractPanelButton {
		private CloseViewQuestButton() {
			super(new TranslatableComponent("gui.close"), ThemeProperties.CLOSE_ICON.get(quest));
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			questScreen.closeQuest();
		}
	}
}
