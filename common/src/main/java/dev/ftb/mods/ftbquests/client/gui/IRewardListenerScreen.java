package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.client.util.ClientUtils;

@FunctionalInterface
public interface IRewardListenerScreen {
	void rewardReceived(RewardKey key, int count);

	static boolean add(RewardKey key, int count) {
		IRewardListenerScreen gui = ClientUtils.getCurrentGuiAs(IRewardListenerScreen.class);
		if (gui != null) {
			gui.rewardReceived(key, count);
			return true;
		}

		return false;
	}
}