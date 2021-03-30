package dev.ftb.mods.ftbquests.quest.theme.property;

/**
 * @author LatvianModder
 */
public class StringProperty extends ThemeProperty<String> {
	public StringProperty(String n) {
		super(n, "");
	}

	@Override
	public String parse(String string) {
		return string;
	}
}