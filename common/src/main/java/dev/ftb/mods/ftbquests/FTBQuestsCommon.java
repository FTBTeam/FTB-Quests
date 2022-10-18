package dev.ftb.mods.ftbquests;

import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FTBQuestsCommon {
	public void init() {
	}

	@Nullable
	public QuestFile getClientQuestFile() {
		return null;
	}

	public QuestFile getQuestFile(boolean isClient) {
		return ServerQuestFile.INSTANCE;
	}

	public void setTaskGuiProviders() {
	}

	public void setRewardGuiProviders() {
	}

	public boolean isClientDataLoaded() {
		return false;
	}

	public Player getClientPlayer() {
		throw new IllegalStateException("Can't access client player from server side!");
	}

	public TeamData getClientPlayerData() {
		throw new IllegalStateException("Can't access client data from server side!");
	}

	public QuestFile createClientQuestFile() {
		throw new IllegalStateException("Can't create client quest file on server side!");
	}

	public void openGui() {
	}

	public void openCustomIconGui(Player player, InteractionHand hand) {
	}

	public void openScreenConfigGui(BlockPos pos) {
	}

	public float[] getTextureUV(BlockState state, Direction face) {
		return null;
	}
}