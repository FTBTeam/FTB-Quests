package dev.ftb.mods.ftbquests.gui;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import dev.ftb.mods.ftblibrary.ui.TextBox;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class MultilineTextEditorScreen extends BaseScreen {
	public final ListConfig<String, StringConfig> config;
	public final ConfigCallback callback;
	public List<TextBox> textBoxes;
	public int active = 0;

	public MultilineTextEditorScreen(ListConfig<String, StringConfig> c, ConfigCallback ca) {
		config = c;
		callback = ca;
		textBoxes = new ArrayList<>();

		for (String s : c.value) {
			TextBox box = new TextBox(this);
			box.setText(s);
			textBoxes.add(box);
		}

		if (textBoxes.isEmpty()) {
			textBoxes.add(new TextBox(this));
		}

		textBoxes.get(0).setFocused(true);
	}

	@Override
	public void addWidgets() {
		add(new TextField(this).setText("This is a lame solution but there will be a better one eventually"));
		widgets.get(0).setPos((width - widgets.get(0).width) / 2, -15);
		add(new SimpleButton(this, new TranslatableComponent("gui.accept"), Icons.ACCEPT, (simpleButton, mouseButton) -> saveAndExit()).setPosAndSize(width + 6, 6, 16, 16));

		for (int i = 0; i < textBoxes.size(); i++) {
			textBoxes.get(i).setPosAndSize(2, 2 + i * 12, width - 4, 12);
		}

		addAll(textBoxes);
	}

	private void saveAndExit() {
		config.value.clear();

		for (TextBox box : textBoxes) {
			config.value.add(box.getText());
		}

		closeGui();
		callback.save(true);
	}

	private void unfocusAll() {
		for (TextBox box : textBoxes) {
			box.setFocused(false);
		}
	}

	@Override
	public boolean keyPressed(Key key) {
		if (key.is(GLFW.GLFW_KEY_ENTER)) {
			unfocusAll();
			active++;
			textBoxes.add(active, new TextBox(this));
			textBoxes.get(active).setFocused(true);
			refreshWidgets();
			return true;
		} else if (key.is(GLFW.GLFW_KEY_BACKSPACE)) {
			if (active > 0 && textBoxes.get(active).getText().isEmpty()) {
				unfocusAll();
				textBoxes.remove(active);
				active--;
				textBoxes.get(active).setFocused(true);
				refreshWidgets();
				return true;
			}
		} else if (key.is(GLFW.GLFW_KEY_UP)) {
			if (active > 0) {
				unfocusAll();
				active--;
				textBoxes.get(active).setFocused(true);
				return true;
			}
		} else if (key.is(GLFW.GLFW_KEY_DOWN)) {
			if (active < textBoxes.size() - 1) {
				unfocusAll();
				active++;
				textBoxes.get(active).setFocused(true);
				return true;
			}
		}

		return super.keyPressed(key);
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		unfocusAll();
		return super.mousePressed(button);
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}
}
