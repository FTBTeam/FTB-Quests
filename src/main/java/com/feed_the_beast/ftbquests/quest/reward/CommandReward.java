package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class CommandReward extends QuestReward
{
	private String command;

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
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("command", () -> command, v -> command = v, "").setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.command"));
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		BlockPos pos = player.getPosition();
		player.getServer().getCommandManager().executeCommand(player.getServer(), command
				.replace("@p", player.getName())
				.replace("@x", Integer.toString(pos.getX()))
				.replace("@y", Integer.toString(pos.getY()))
				.replace("@z", Integer.toString(pos.getZ()))
				.replace("@chapter", quest.chapter.toString())
				.replace("@quest", quest.toString())
				.replace("@team", FTBLibAPI.getTeam(player.getUniqueID())));
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent text = new TextComponentString(command);
		text.getStyle().setColor(TextFormatting.RED);
		return new TextComponentTranslation("ftbquests.reward.ftbquests.command.text", text);
	}
}