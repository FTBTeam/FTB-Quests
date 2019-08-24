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
import noppes.npcs.controllers.data.PlayerData;

/**
 * @author LatvianModder
 */
public class NPCFactionReward extends Reward
{
	public int npcFaction = 0;
	public int points = 1;
	public boolean add = true;

	public NPCFactionReward(Quest quest)
	{
		super(quest);
	}

	@Override
	public RewardType getType()
	{
		return CustomNPCsIntegration.FACTION_REWARD;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setInteger("npc_faction", npcFaction);
		nbt.setInteger("points", points);
		nbt.setBoolean("add", add);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		npcFaction = nbt.getInteger("npc_faction");
		points = nbt.getInteger("required_points");
		add = nbt.getBoolean("add");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(npcFaction);
		data.writeVarInt(points);
		data.writeBoolean(add);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		npcFaction = data.readVarInt();
		points = data.readVarInt();
		add = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("id", () -> npcFaction, v -> npcFaction = v, 0, 0, Integer.MAX_VALUE);
		config.addInt("points", () -> points, v -> points = v, 1, 0, Integer.MAX_VALUE);
		config.addBool("add", () -> add, v -> add = v, true);
	}

	@Override
	public void claim(EntityPlayerMP player, boolean notify)
	{
		PlayerData data = PlayerData.get(player);

		if (data != null && data.factionData.factionData.containsKey(npcFaction))
		{
			if (add)
			{
				data.factionData.increasePoints(player, npcFaction, points);
			}
			else
			{
				data.factionData.factionData.put(npcFaction, points);
			}
		}
	}
}