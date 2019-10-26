package com.feed_the_beast.ftbquests.integration.customnpcs;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.QuestData;

/**
 * @author LatvianModder
 */
public class NPCQuestReward extends Reward
{
	public int npcQuest = 0;

	public NPCQuestReward(Quest quest)
	{
		super(quest);
	}

	@Override
	public RewardType getType()
	{
		return CustomNPCsIntegration.QUEST_REWARD;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setInteger("npc_quest", npcQuest);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		npcQuest = nbt.getInteger("npc_quest");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(npcQuest);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		npcQuest = data.readVarInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("id", () -> npcQuest, v -> npcQuest = v, 0, 0, Integer.MAX_VALUE);
	}

	@Override
	public void claim(EntityPlayerMP player, boolean notify)
	{
		PlayerData data = PlayerData.get(player);
		QuestData qd = data == null ? null : data.questData.activeQuests.get(npcQuest);

		if (qd != null)
		{
			PlayerQuestController.setQuestFinished(qd.quest, player);
		}
	}
}