package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.SelectChoiceRewardScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.server.level.ServerPlayer;

public class ChoiceReward extends RandomReward {
	public ChoiceReward(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public RewardType getType() {
		return RewardTypes.CHOICE;
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		if (getTable() != null) {
			getTable().addMouseOverText(list, false, false);
		}
	}

	@Override
	public void onButtonClicked(Button button, boolean canClick) {
		if (canClick) {
			button.playClickSound();
			new SelectChoiceRewardScreen(this).openGui();
		}
	}

	@Override
	public boolean getExcludeFromClaimAll() {
		return true;
	}
}
