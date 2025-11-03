package dev.ftb.mods.ftbquests.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftblibrary.config.*;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.RainbowIcon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.MultilineTextBox.StringExtents;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.NordColors;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.ImageComponent;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

public class MultilineTextEditorScreen extends BaseScreen {
    public static final Icon LINK_ICON = Icon.getIcon(FTBQuestsAPI.rl("textures/gui/chain_link.png")).withPadding(2);
    public static final Icon CLEAR_FORMATTING_ICON = Icon.getIcon(FTBQuestsAPI.rl("textures/gui/eraser.png")).withPadding(2);

	private static final Pattern STRIP_FORMATTING_PATTERN = Pattern.compile("(?i)([&\\u00A7])([0-9A-FK-ORZ]|#[0-9A-Fa-f]{6})");
	private static final int MAX_UNDO = 10;
	protected static final String LINK_TEXT_TEMPLATE = "{ \"text\": \"%s\", \"underlined\": true, \"clickEvent\": { \"action\": \"change_page\", \"value\": \"%016X\" } }";

	private final Component title;
	private final ListConfig<String, StringConfig> config;
	private final ConfigCallback callback;
	private final Panel outerPanel;
	private final Panel toolbarPanel;
	private final Panel textBoxPanel;
	private final MultilineTextBox textBox;
	private final PanelScrollBar scrollBar;
	private long ticksOpen = 0;  // ticks since opening (not creation), reset when gui returned to from image selector
	private final String initialText;

	private long lastChange = 0L;
	private final Deque<HistoryElement> redoStack = new ArrayDeque<>();

	private final Map<Integer,Runnable> hotKeys = Map.of(
			InputConstants.KEY_B, () -> insertFormatting(ChatFormatting.BOLD),
			InputConstants.KEY_I, () -> insertFormatting(ChatFormatting.ITALIC),
			InputConstants.KEY_U, () -> insertFormatting(ChatFormatting.UNDERLINE),
			InputConstants.KEY_S, () -> insertFormatting(ChatFormatting.STRIKETHROUGH),
			InputConstants.KEY_R, this::resetFormatting,
			InputConstants.KEY_P, () -> insertAtEndOfLine("\n" + Quest.PAGEBREAK_CODE),
			InputConstants.KEY_M, this::openImageSelector,
			InputConstants.KEY_Z, this::undoLast,
			InputConstants.KEY_L, this::openLinkInsert
	);

	public MultilineTextEditorScreen(Component title, ListConfig<String, StringConfig> config, ConfigCallback callback) {
		this.title = title;
		this.config = config;
		this.callback = callback;

		outerPanel = new OuterPanel(this);
		toolbarPanel = new ToolbarPanel(outerPanel);
		textBoxPanel = new TextBoxPanel(outerPanel);

		textBox = new MultilineTextBox(textBoxPanel);
		textBox.setText(String.join("\n", config.getValue()));
		textBox.setFocused(true);
		textBox.setValueListener(this::onValueChanged);
		textBox.seekCursor(Whence.ABSOLUTE, 0);

		redoStack.addLast(new HistoryElement(textBox.getText(), textBox.cursorPos()));

		scrollBar = new PanelScrollBar(outerPanel, ScrollBar.Plane.VERTICAL, textBoxPanel);
		scrollBar.setScrollStep(getTheme().getFontHeight());

		initialText = textBox.getText();
	}

	private void onValueChanged(String newValue) {
		// don't snapshot the text box state immediately, but note the change
		// when no changes have happened for a few ticks, then do a snapshot (see tick() below)
		lastChange = Minecraft.getInstance().level.getGameTime();
	}

	@Override
	public void tick() {
		super.tick();

		ticksOpen++;

		if (lastChange > 0 && Minecraft.getInstance().level.getGameTime() - lastChange > 5) {
			redoStack.addLast(new HistoryElement(textBox.getText(), textBox.cursorPos()));
			while (redoStack.size() > MAX_UNDO) {
				redoStack.removeFirst();
			}
			lastChange = 0L;
		}
	}

	@Override
	public boolean onInit() {
		setWidth(getScreen().getGuiScaledWidth() / 5 * 4);
		setHeight(getScreen().getGuiScaledHeight() / 5 * 4);
		ticksOpen = 0L;
		return true;
	}

	@Override
	public void addWidgets() {
		add(outerPanel);
	}

	@Override
	public void alignWidgets() {
		outerPanel.setPosAndSize(0, 0, width, height);

		toolbarPanel.setPosAndSize( 2, 2, width - 4, 18);
		toolbarPanel.alignWidgets();

		textBoxPanel.setPosAndSize(2, toolbarPanel.height + 4, width - 18, height - toolbarPanel.height - 6);
		textBoxPanel.alignWidgets();

		scrollBar.setPosAndSize(width - 14, textBoxPanel.posY, 12, height - textBoxPanel.posY - 4);
	}

	@Override
	public void drawBackground(GuiGraphics matrixStack, Theme theme, int x, int y, int w, int h) {
		super.drawBackground(matrixStack, theme, x, y, w, h);

		theme.drawString(matrixStack, title, x + (width - theme.getStringWidth(title)) / 2, y - theme.getFontHeight() - 2, Theme.SHADOW);
	}

	@Override
	public boolean keyPressed(Key key) {
		if (key.esc()) {
			cancel();
			return true;
		} else if (key.enter() && Screen.hasShiftDown()) {
			saveAndExit();
			return true;
		} else if (textBox.isFocused()) {
			textBox.keyPressed(key);
			return true;
		}

		return false;
	}

	@Override
	public void keyReleased(Key key) {
		// need to do this on keyReleased() so keypress doesn't pass through to any opened sub-screen
		executeHotkey(key.keyCode, true);
	}

	private void executeHotkey(int keycode, boolean checkModifier) {
		if (hotKeys.containsKey(keycode) && (!checkModifier || isHotKeyModifierPressed(keycode))) {
			hotKeys.get(keycode).run();
			textBox.setFocused(true);
		}
	}

	@Override
	public boolean charTyped(char c, KeyModifiers modifiers) {
		if (ticksOpen < 2) {
			// small kludge to avoid 'e' being inserted if image select screen is exited by pressing E
			return true;
		}

		// need to intercept this, or the character is sent on to the text box
		int keyCode = Character.toUpperCase(c);
		if (isHotKeyModifierPressed(keyCode) && hotKeys.containsKey(keyCode)) {
			return false;
		}
		return super.charTyped(c, modifiers);
	}

	private static boolean isHotKeyModifierPressed(int keycode) {
		return keycode == InputConstants.KEY_Z || Util.getPlatform() == Util.OS.OSX ? Screen.hasControlDown() : Screen.hasAltDown();
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	private void openLinkInsert() {
		ConfigQuestObject<QuestObject> config = new ConfigQuestObject<>(QuestObjectType.QUEST.or(QuestObjectType.QUEST_LINK));
		new SelectQuestObjectScreen<>(config, accepted -> {
			int pos = textBox.cursorPos();
			if (accepted) {
				doLinkInsertion(config.getValue().id);
			}
			run();
			textBox.seekCursor(Whence.ABSOLUTE, pos);
		}).openGui();
	}

	private void doLinkInsertion(long questID) {
		String text = textBox.getSelectedText();
		if (!text.contains("\n")) {
			// a selection which doesn't extend over multiple physical lines; replace the selection in a smart way
			StringExtents lineExtents = getPhysicalLineExtents();
			StringExtents selectionExtents = textBox.getSelected();

			if (lineExtents.start() == lineExtents.end() || selectionExtents.start() == selectionExtents.end()) {
				return;
			}

			String thisLine = textBox.getText().substring(lineExtents.start(), lineExtents.end());
			if (thisLine.startsWith("[") && thisLine.endsWith("]")) {
				errorToPlayer("ftbquests.gui.error.already_component");
				return;
			}

			List<String> parts = List.of(
					textBox.getText().substring(lineExtents.start(), selectionExtents.start()),
					textBox.getText().substring(selectionExtents.start(), selectionExtents.end()),
					textBox.getText().substring(selectionExtents.end(), lineExtents.end())
			);

			StringBuilder builder = new StringBuilder("[ ");
			if (!parts.get(0).isEmpty()) builder.append("\"").append(parts.get(0)).append("\", ");
			builder.append(String.format(LINK_TEXT_TEMPLATE, parts.get(1).isEmpty() ? "EDIT HERE" : parts.get(1), questID));
			if (!parts.get(2).isEmpty()) builder.append(", ").append("\"").append(parts.get(2)).append("\"");
			builder.append(" ]");

			textBox.setSelecting(false);
			textBox.seekCursor(Whence.ABSOLUTE, lineExtents.start());
			textBox.setSelecting(true);
			textBox.seekCursor(Whence.ABSOLUTE, lineExtents.end());
			textBox.insertText(builder.toString());
		} else {
			errorToPlayer("ftbquests.gui.error.selection_multiple_lines");
		}
	}

	private StringExtents getPhysicalLineExtents() {
		int pos = 0;
		String thisLine = "";
		for (String line : textBox.getText().split("\\n")) {
			if (textBox.cursorPos() >= pos && textBox.cursorPos() < pos + line.length()) {
				thisLine = line;
				break;
			}
			pos += line.length() + 1;
		}

		return new StringExtents(pos, pos + thisLine.length());
	}

	private void errorToPlayer(String msg, Object... args) {
		QuestScreen.displayError(Component.translatable(String.format(msg, args)).withStyle(ChatFormatting.RED));
	}

	private void openImageSelector() {
		int cursor = textBox.cursorPos();

		ImageComponent component = new ImageComponent();
		ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID + ".gui.image", accepted -> {
			openGui();
			if (accepted) {
				textBox.seekCursor(Whence.ABSOLUTE, cursor);
				insertAtEndOfLine("\n" + component);
			}
		});

		group.add("image", new ImageResourceConfig(), ImageResourceConfig.getResourceLocation(component.getImage()),
				v -> component.setImage(Icon.getIcon(v)), ImageResourceConfig.NONE).setOrder(-127);
		group.addInt("width", component.getWidth(), component::setWidth, 0, 1, 1000);
		group.addInt("height", component.getHeight(), component::setHeight, 0, 1, 1000);
		group.addEnum("align", component.getAlign(), component::setAlign, ImageComponent.ImageAlign.NAME_MAP, ImageComponent.ImageAlign.CENTER);
		group.addBool("fit", component.isFit(), component::setFit, false);

		new EditConfigScreen(group).openGui();
	}

	private void cancel() {
		if (!textBox.getText().equals(initialText)) {
			getGui().openYesNo(Component.translatable("ftbquests.gui.confirm_esc"), Component.empty(), () -> callback.save(false));
		} else {
			callback.save(false);
		}
	}

	private void saveAndExit() {
		config.getValue().clear();

		Collections.addAll(config.getValue(), textBox.getText().split("\n"));

		closeGui();
		callback.save(true);
	}

	private void insertFormatting(ChatFormatting c) {
		insertFormatting(String.valueOf(c.getChar()));
	}

	private void insertFormatting(ColorConfig color) {
		insertFormatting(color.getValue().toString());
	}

	private void insertFormatting(String formatting) {
		if (textBox.hasSelection()) {
			textBox.insertText("&" + formatting + textBox.getSelectedText() + "&r");
		} else {
			textBox.insertText("&" + formatting);
		}
		textBox.setFocused(true);
	}

	private void resetFormatting() {
		if (textBox.hasSelection()) {
			textBox.insertText(stripFormatting(textBox.getSelectedText()));
		} else {
			textBox.insertText("&r");
		}
	}

	private static String stripFormatting(@NotNull String selectedText) {
		return STRIP_FORMATTING_PATTERN.matcher(selectedText).replaceAll("");
	}

	private void insertAtEndOfLine(String toInsert) {
		textBox.keyPressed(new Key(InputConstants.KEY_END, -1, 0));
		textBox.insertText(toInsert);
	}

	private void undoLast() {
		if (redoStack.size() > 1) {
			redoStack.removeLast();
			HistoryElement h = redoStack.peekLast();
			textBox.setValueListener(s -> {});
			textBox.setText(Objects.requireNonNull(h).text());
			textBox.setValueListener(this::onValueChanged);
			textBox.setSelecting(false);
			textBox.seekCursor(Whence.ABSOLUTE, h.cursorPos());
		}
	}


	private class OuterPanel extends Panel {
		public OuterPanel(MultilineTextEditorScreen screen) {
			super(screen);
		}

		@Override
		public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			theme.drawPanelBackground(graphics, x, y, w, h);
		}

		@Override
		public void addWidgets() {
			addAll(List.of(toolbarPanel, textBoxPanel, scrollBar));
		}

		@Override
		public void alignWidgets() {
		}
	}

	private class TextBoxPanel extends Panel {
		public TextBoxPanel(Panel outerPanel) {
			super(outerPanel);
		}
		private int cursorPos;

		@Override
		public void addWidgets() {
			add(textBox);
		}

		@Override
		public void refreshWidgets() {
			cursorPos = textBox.cursorPos();

			super.refreshWidgets();
		}

		@Override
		public void alignWidgets() {
			textBox.setWidth(width - 3);  // also forces height recalculation based on contents
			setScrollY(0);
			textBox.seekCursor(Whence.ABSOLUTE, cursorPos);
		}

		@Override
		public boolean mousePressed(MouseButton button) {
			boolean res = super.mousePressed(button);

			textBox.setFocused(isMouseOver());

			return res;
		}
	}

	private class ToolbarPanel extends Panel {
		private final ToolbarButton acceptButton, cancelButton;
		private final ToolbarButton boldButton, italicButton, underlineButton, strikethroughButton;
		private final ToolbarButton colorButton;
		private final ToolbarButton resetButton;
		private final ToolbarButton pageBreakButton;
		private final ToolbarButton imageButton;
		private final ToolbarButton undoButton;
		private final ToolbarButton linkButton;

		public ToolbarPanel(Panel outerPanel) {
			super(outerPanel);

			acceptButton = new ToolbarButton(this, Component.translatable("gui.accept"), Icons.ACCEPT,
					MultilineTextEditorScreen.this::saveAndExit)
					.withTooltip(hotkey("Enter", "Shift"));
			cancelButton = new ToolbarButton(this, Component.translatable("gui.cancel"), Icons.CANCEL,
					MultilineTextEditorScreen.this::cancel)
					.withTooltip(hotkey("Escape", null));

			boldButton = new ToolbarButton(this, Component.literal("B").withStyle(ChatFormatting.BOLD),
					() -> executeHotkey(InputConstants.KEY_B, false))
					.withTooltip(hotkey("B", "Alt"));
			italicButton = new ToolbarButton(this, Component.literal("I").withStyle(ChatFormatting.ITALIC),
					() -> executeHotkey(InputConstants.KEY_I, false))
					.withTooltip(hotkey("I", "Alt"));
			underlineButton = new ToolbarButton(this, Component.literal("U").withStyle(ChatFormatting.UNDERLINE),
					() -> executeHotkey(InputConstants.KEY_U, false))
					.withTooltip(hotkey("U", "Alt"));
			strikethroughButton = new ToolbarButton(this, Component.literal("S").withStyle(ChatFormatting.STRIKETHROUGH),
					() -> executeHotkey(InputConstants.KEY_S, false))
					.withTooltip(hotkey("S", "Alt"));
			colorButton = new ToolbarButton(this, Component.empty(), Icons.COLOR_RGB.withPadding(2),
					this::openColorContextMenu);
			linkButton = new ToolbarButton(this, Component.empty(), LINK_ICON,
					() -> executeHotkey(InputConstants.KEY_L, false))
					.withTooltip(Component.translatable("ftbquests.gui.insert_link"), hotkey("L", "Alt"))
					.withActivePredicate(this::isOkForLinkInsertion);
			resetButton = new ToolbarButton(this, Component.empty(), CLEAR_FORMATTING_ICON,
					() -> executeHotkey(InputConstants.KEY_R, false))
					.withTooltip(Component.translatable("ftbquests.gui.clear_formatting"), hotkey("R", "Alt"));
			pageBreakButton = new ToolbarButton(this, Component.empty(), ViewQuestPanel.PAGEBREAK_ICON.withPadding(2),
					() -> executeHotkey(InputConstants.KEY_P, false))
					.withTooltip(Component.translatable("ftbquests.gui.page_break"), hotkey("P", "Alt"));
			imageButton = new ToolbarButton(this, Component.empty(), Icons.ART.withPadding(2),
					() -> executeHotkey(InputConstants.KEY_M, false))
					.withTooltip(Component.translatable("ftbquests.chapter.image"), hotkey("M", "Alt"));
			undoButton = new ToolbarButton(this, Component.empty(), Icons.REFRESH,
					() -> executeHotkey(InputConstants.KEY_Z, false))
					.withTooltip(Component.translatable("ftbquests.gui.undo"), hotkey("Z", "Ctrl"));
		}

		private boolean isOkForLinkInsertion() {
            return textBox.hasSelection() && !textBox.getSelectedText().contains("\n");
        }

		private static Component hotkey(String key, @Nullable String modifier) {
            boolean isMac = Util.getPlatform() == Util.OS.OSX;

            String adaptedModifier = modifier != null && modifier.equalsIgnoreCase("alt") ? (isMac ? "Ctrl" : "Alt") : modifier;
            String modifierDisplay = modifier == null ? "" : (adaptedModifier + " + ");

			return Component.literal("[" + modifierDisplay + key + "]").withStyle(ChatFormatting.DARK_GRAY);
		}

		private void openColorContextMenu() {
			List<ContextMenuItem> items = new ArrayList<>();

			for (ChatFormatting cf : ChatFormatting.values()) {
				if (cf.getColor() != null) {
					items.add(new ContextMenuItem(Component.empty(), Color4I.rgb(cf.getColor()), b -> insertFormatting(cf)));
				}
			}

			var colorConfig = new ColorConfig();
			items.add(new ContextMenuItem(Component.empty(), Icons.COLOR_HSB, btn -> ColorSelectorPanel.popupAtMouse(this.parent.getGui(), colorConfig, (b) -> {
				if (b) {
					insertFormatting(colorConfig);
				}
			})));

            items.add(new ContextMenuItem(Component.empty(), new RainbowIcon(), btn -> insertFormatting("z")));

			ContextMenu cMenu = new ContextMenu(MultilineTextEditorScreen.this, items);
			cMenu.setMaxRows(4);
			cMenu.setDrawVerticalSeparators(false);
			MultilineTextEditorScreen.this.openContextMenu(cMenu);
		}

		@Override
		public void tick() {
			undoButton.setVisible(redoStack.size() > 1);
		}

		@Override
		public void addWidgets() {
			addAll(List.of(
					acceptButton,
					cancelButton,
					boldButton,
					italicButton,
					underlineButton,
					strikethroughButton,
					colorButton,
					linkButton,
					resetButton,
					pageBreakButton,
					imageButton,
					undoButton
			));
		}

		@Override
		public void alignWidgets() {
			acceptButton.setPosAndSize(1, 1, 16, 16);

			boldButton.setPosAndSize(27, 1, 16, 16);
			italicButton.setPosAndSize(43, 1, 16, 16);
			underlineButton.setPosAndSize(59, 1, 16, 16);
			strikethroughButton.setPosAndSize(75, 1, 16, 16);
			colorButton.setPosAndSize(91, 1, 16, 16);
			linkButton.setPosAndSize(107, 1, 16, 16);
			resetButton.setPosAndSize(123, 1, 16, 16);
			pageBreakButton.setPosAndSize(149, 1, 16, 16);
			imageButton.setPosAndSize(165, 1, 16, 16);
			undoButton.setPosAndSize(191, 1, 16, 16);

			cancelButton.setPosAndSize(width - 17, 1, 16, 16);
		}

		@Override
		public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			NordColors.POLAR_NIGHT_0.draw(graphics, x, y, w, h);
			theme.drawPanelBackground(graphics, x, y, w, h);
		}
	}

	private static class ToolbarButton extends SimpleTextButton {
		private final Runnable onClick;
		private final List<Component> tooltip = new ArrayList<>();
		private boolean visible = true;
		private BooleanSupplier activePredicate = () -> true;

		public ToolbarButton(Panel panel, Component txt, Icon icon, Runnable onClick) {
			super(panel, txt, icon);
			this.onClick = onClick;
		}

		public ToolbarButton(Panel panel, Component txt, Runnable onClick) {
			this(panel, txt, Color4I.empty(), onClick);
		}

		@Override
		public Component getTitle() {
			return activePredicate.getAsBoolean() ? super.getTitle() : super.getTitle().copy().withStyle(ChatFormatting.DARK_GRAY);
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		@Override
		public void onClicked(MouseButton button) {
			if (visible && activePredicate.getAsBoolean()) {
				onClick.run();
			}
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			if (getGui().getTheme().getStringWidth(title) > 0) {
				super.addMouseOverText(list);
			}
			if (visible) {
				tooltip.forEach(list::add);
			}
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			if (visible) {
				super.draw(graphics, theme, x, y, w, h);
			}
		}

		public ToolbarButton withTooltip(Component... lines) {
			tooltip.addAll(Arrays.asList(lines));
			return this;
		}

		public ToolbarButton withActivePredicate(BooleanSupplier predicate) {
			this.activePredicate = predicate;
			return this;
		}
	}

	private record HistoryElement(@NotNull String text, int cursorPos) {
	}
}
