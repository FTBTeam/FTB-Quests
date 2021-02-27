package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;

/**
 * @author LatvianModder
 */
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