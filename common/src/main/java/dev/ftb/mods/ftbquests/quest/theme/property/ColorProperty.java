package dev.ftb.mods.ftbquests.quest.theme.property;

import dev.ftb.mods.ftbguilibrary.icon.Color4I;

/**
 * @author LatvianModder
 */
public class ColorProperty extends ThemeProperty<Color4I> {
	public ColorProperty(String n) {
		super(n, Color4I.EMPTY);
	}

	@Override
	public Color4I parse(String string) {
		return Color4I.fromString(string);
	}
}