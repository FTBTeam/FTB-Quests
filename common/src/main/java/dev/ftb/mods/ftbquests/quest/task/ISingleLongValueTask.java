package dev.ftb.mods.ftbquests.quest.task;

/**
 * @author LatvianModder
 */
public interface ISingleLongValueTask {
	default long getDefaultConfigValue() {
		return 1L;
	}

	default long getMaxConfigValue() {
		return Long.MAX_VALUE;
	}

	void setValue(long value);
}