package com.feed_the_beast.ftbquests.quest.reward;

/**
 * @author LatvianModder
 */
public enum RewardClaimType {
	CAN_CLAIM,
	CANT_CLAIM,
	CLAIMED;

	public boolean canClaim() {
		return this == CAN_CLAIM;
	}

	public boolean cantClaim() {
		return this == CANT_CLAIM;
	}

	public boolean isClaimed() {
		return this == CLAIMED;
	}
}