package dev.ftb.mods.ftbquests.quest.task;

@FunctionalInterface
public interface ISingleLongValueTask {
	default long getDefaultConfigValue() {
		return 1L;
	}

	default long getMinConfigValue() {
		return 1L;
	}

	default long getMaxConfigValue() {
		return Long.MAX_VALUE;
	}

	void setValue(long value);
}