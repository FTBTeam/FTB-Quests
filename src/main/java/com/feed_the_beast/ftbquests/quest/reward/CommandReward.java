package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class CommandReward extends QuestReward
{
	public String command;

	public CommandReward(Quest quest)
	{
		super(quest);
		command = "/say Hi, @team!";
	}

	@Override
	public QuestRewardType getType()
	{
		return FTBQuestsRewards.COMMAND;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setString("command", command);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		command = nbt.getString("command");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(command);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		command = data.readString();
	}

	@Override
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addString("command", () -> command, v -> command = v, "/say Hi, @team!").setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.command"));
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		Map<String, Object> overrides = new HashMap<>();
		overrides.put("p", player.getName());

		BlockPos pos = player.getPosition();
		overrides.put("x", pos.getX());
		overrides.put("y", pos.getY());
		overrides.put("z", pos.getZ());

		Chapter chapter = getQuestChapter();

		if (chapter != null)
		{
			overrides.put("chapter", chapter);
		}

		overrides.put("quest", quest);
		overrides.put("team", FTBLibAPI.getTeam(player.getUniqueID()));

		String s = command;

		for (Map.Entry<String, Object> entry : overrides.entrySet())
		{
			if (entry.getValue() != null)
			{
				s = s.replace("@" + entry.getKey(), entry.getValue().toString());
			}
		}

		player.server.getCommandManager().executeCommand(player.server, s);
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.reward.ftbquests.command") + ": " + TextFormatting.RED + command;
	}
}