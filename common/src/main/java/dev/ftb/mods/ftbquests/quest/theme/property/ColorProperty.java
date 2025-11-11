package dev.ftb.mods.ftbquests.quest.theme.property;

import dev.ftb.mods.ftblibrary.icon.Color4I;

public class ColorProperty extends ThemeProperty<Color4I> {
	public ColorProperty(String n) {
		super(n, Color4I.empty());
	}

	@Override
	public Color4I parse(String string) {
		return Color4I.fromString(string);
	}
}