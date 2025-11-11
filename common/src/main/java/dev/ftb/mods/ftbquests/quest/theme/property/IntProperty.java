package dev.ftb.mods.ftbquests.quest.theme.property;

import net.minecraft.util.Mth;

public class IntProperty extends ThemeProperty<Integer> {
	public final int min;
	public final int max;

	public IntProperty(String name, int min, int max) {
		super(name, 0);
		this.min = min;
		this.max = max;
	}

	public IntProperty(String n) {
		this(n, 0, Integer.MAX_VALUE);
	}

	@Override
	public Integer parse(String string) {
		try {
			int i = Integer.parseInt(string);
			return Mth.clamp(i, min, max);
		} catch (Exception ignored) {
		}

		return null;
	}
}