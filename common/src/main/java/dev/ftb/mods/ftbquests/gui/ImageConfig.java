package dev.ftb.mods.ftbquests.gui;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigCallback;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;

/**
 * @author LatvianModder
 */
public class ImageConfig extends ConfigString {
	public ImageConfig() {
		super(null);
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		new SelectImageScreen(this, callback).openGui();
	}
}
