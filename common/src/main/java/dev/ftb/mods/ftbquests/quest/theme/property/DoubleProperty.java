package dev.ftb.mods.ftbquests.quest.theme.property;

import net.minecraft.util.Mth;

import org.jspecify.annotations.Nullable;

public class DoubleProperty extends ThemeProperty<Double> {
	private final double min;
	private final double max;

	public DoubleProperty(String name, double min, double max) {
		super(name, 0D);

		this.min = min;
		this.max = max;
	}

	public DoubleProperty(String n) {
		this(n, 0D, 1D);
	}

	@Override
	@Nullable
	public Double parse(String string) {
		try {
			double i = Double.parseDouble(string);
			return Mth.clamp(i, min, max);
		} catch (Exception ignored) {
			return null;
		}
	}
}