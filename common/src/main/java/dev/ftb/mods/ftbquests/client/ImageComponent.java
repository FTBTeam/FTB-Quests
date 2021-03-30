package dev.ftb.mods.ftbquests.client;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.minecraft.network.chat.TextComponent;

public class ImageComponent extends TextComponent {
	public Icon image;
	public int width;
	public int height;

	public ImageComponent() {
		super("[Image]");
	}
}
