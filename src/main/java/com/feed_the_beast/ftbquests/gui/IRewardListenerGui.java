package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;

/**
 * @author LatvianModder
 */
public interface IRewardListenerGui
{
	void rewardReceived(RewardKey key, int count);

	static boolean add(RewardKey key, int count)
	{
		IRewardListenerGui gui = ClientUtils.getCurrentGuiAs(IRewardListenerGui.class);

		if (gui != null)
		{
			gui.rewardReceived(key, count);
			return true;
		}

		return false;
	}
}