package dev.ftb.mods.ftbquests.gui;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigCallback;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigList;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.TextBox;
import com.feed_the_beast.mods.ftbguilibrary.widget.TextField;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class MultilineTextEditorScreen extends GuiBase {
	public final ConfigList<String, ConfigString> config;
	public final ConfigCallback callback;
	public List<TextBox> textBoxes;
	public int active = 0;

	public MultilineTextEditorScreen(ConfigList<String, ConfigString> c, ConfigCallback ca) {
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
		add(new SimpleButton(this, new TranslatableComponent("gui.accept"), GuiIcons.ACCEPT, (simpleButton, mouseButton) -> saveAndExit()).setPosAndSize(width + 6, 6, 16, 16));

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
