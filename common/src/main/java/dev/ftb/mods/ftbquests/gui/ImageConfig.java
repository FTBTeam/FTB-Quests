package dev.ftb.mods.ftbquests.gui;

import dev.ftb.mods.ftbguilibrary.config.ConfigCallback;
import dev.ftb.mods.ftbguilibrary.config.StringConfig;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;

/**
 * @author LatvianModder
 */
public class ImageConfig extends StringConfig {
	public ImageConfig() {
		super(null);
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		new SelectImageScreen(this, callback).openGui();
	}
}
