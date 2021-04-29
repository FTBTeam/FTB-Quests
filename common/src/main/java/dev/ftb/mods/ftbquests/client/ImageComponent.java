package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.network.chat.TextComponent;

public class ImageComponent extends TextComponent {
	public Icon image;
	public int width;
	public int height;

	public ImageComponent() {
		super("[Image]");
	}
}
