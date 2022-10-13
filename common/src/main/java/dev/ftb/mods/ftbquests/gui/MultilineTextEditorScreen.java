package dev.ftb.mods.ftbquests.gui;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class MultilineTextEditorScreen extends BaseScreen {
	public final ListConfig<String, StringConfig> config;
	public final ConfigCallback callback;
	public List<EntryTextBox> textBoxes;
	public int active = 0;

	public MultilineTextEditorScreen(ListConfig<String, StringConfig> c, ConfigCallback ca) {
		config = c;
		callback = ca;
		textBoxes = new ArrayList<>();

		for (String s : c.value) {
			EntryTextBox box = new EntryTextBox(this);
			box.setText(s);
			textBoxes.add(box);
		}

		if (textBoxes.isEmpty()) {
			textBoxes.add(new EntryTextBox(this));
		}

		textBoxes.get(0).setFocused(true);

		reIndexTextBoxes();
	}

	@Override
	public boolean onInit() {
		setWidth(getScreen().getGuiScaledWidth() / 5 * 4);
		return true;
	}

	@Override
	public void addWidgets() {
		add(new TextField(this).setText(Component.literal("This is a lame solution but there will be a better one eventually")));
		widgets.get(0).setPos((width - widgets.get(0).width) / 2, -15);
		add(new SimpleButton(this, Component.translatable("gui.accept"), Icons.ACCEPT, (simpleButton, mouseButton) -> saveAndExit()).setPosAndSize(width + 6, 6, 16, 16));
		add(new SimpleButton(this, Component.translatable("gui.cancel"), Icons.CANCEL, (simpleButton, mouseButton) -> cancel()).setPosAndSize(width + 6, 24, 16, 16));

		for (int i = 0; i < textBoxes.size(); i++) {
			textBoxes.get(i).setPosAndSize(2, 2 + i * 12, width - 4, 12);
		}

		addAll(textBoxes);
	}

	private void cancel() {
		callback.save(false);
	}

	private void saveAndExit() {
		config.value.clear();

		for (TextBox box : textBoxes) {
			config.value.add(box.getText());
		}

		closeGui();
		callback.save(true);
	}

	private void reIndexTextBoxes() {
		for (int i = 0; i < textBoxes.size(); i++) {
			textBoxes.get(i).index = i;
		}
	}
	private void unfocusAll() {
		for (TextBox box : textBoxes) {
			box.setFocused(false);
		}
	}

	@Override
	public boolean keyPressed(Key key) {
		if (key.is(GLFW.GLFW_KEY_ENTER)) {
			if (isShiftKeyDown()) {
				saveAndExit();
			} else {
				unfocusAll();
				active++;
				textBoxes.add(active, new EntryTextBox(this));
				textBoxes.get(active).setFocused(true);
				reIndexTextBoxes();
				refreshWidgets();
				return true;
			}
		} else if (key.is(GLFW.GLFW_KEY_BACKSPACE)) {
			if (active > 0 && textBoxes.get(active).getText().isEmpty()) {
				unfocusAll();
				textBoxes.remove(active);
				active--;
				textBoxes.get(active).setFocused(true);
				reIndexTextBoxes();
				refreshWidgets();
				return true;
			}
		} else if (key.is(GLFW.GLFW_KEY_UP)) {
			if (active > 0) {
				unfocusAll();
				active--;
				textBoxes.get(active).setFocused(true);
				textBoxes.get(active).setCursorPosition(textBoxes.get(active + 1).getCursorPosition());
				return true;
			}
		} else if (key.is(GLFW.GLFW_KEY_DOWN)) {
			if (active < textBoxes.size() - 1) {
				unfocusAll();
				active++;
				textBoxes.get(active).setFocused(true);
				textBoxes.get(active).setCursorPosition(textBoxes.get(active - 1).getCursorPosition());
				return true;
			}
		} else if (key.esc()) {
			callback.save(false);
		}

		return super.keyPressed(key);
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	private class EntryTextBox extends TextBox {
		private int index;

		public EntryTextBox(Panel panel) {
			super(panel);
		}

		@Override
		public boolean mousePressed(MouseButton button) {
			for (TextBox box : MultilineTextEditorScreen.this.textBoxes) {
				if (box != this) box.setFocused(false);
			}

			MultilineTextEditorScreen.this.active = this.index;

			return super.mousePressed(button);
		}
	}
}
