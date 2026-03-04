package dev.ftb.mods.ftbquests.quest.theme.property;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;

public class IconProperty extends ThemeProperty<Icon<?>> {
	public final Icon<?> builtin;

	public IconProperty(String name, Icon<?> builtin) {
		super(name, Color4I.empty());

		this.builtin = builtin;
	}

	public IconProperty(String name) {
		this(name, Icon.empty());
	}

	@Override
	public Icon<?> parse(String string) {
        return string.equals("builtin") ? builtin : Icon.getIcon(string);
    }
}
