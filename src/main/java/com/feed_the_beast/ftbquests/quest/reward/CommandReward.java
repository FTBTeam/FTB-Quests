package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
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

	public CommandReward(Quest q, int id, NBTTagCompound nbt)
	{
		super(q, id);
		command = nbt.getString("command");
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("command", command);
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		config.addString("command", () -> command, v -> command = v, "").setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.command"));
	}

	@Override
	public void claim(EntityPlayer player)
	{
		if (!player.world.isRemote)
		{
			BlockPos pos = player.getPosition();
			player.getServer().getCommandManager().executeCommand(player.getServer(), command
					.replace("@p", player.getName())
					.replace("@x", Integer.toString(pos.getX()))
					.replace("@y", Integer.toString(pos.getY()))
					.replace("@z", Integer.toString(pos.getZ()))
					.replace("@chapter", quest.chapter.id)
					.replace("@quest", quest.id)
					.replace("@team", FTBLibAPI.getTeam(player.getUniqueID())));
		}
	}

	@Override
	public Icon getAltIcon()
	{
		return Icon.getIcon("minecraft:blocks/command_block_back");
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent text = new TextComponentString(command);
		text.getStyle().setColor(TextFormatting.RED);
		return new TextComponentTranslation("ftbquests.reward.ftbquests.command.text", text);
	}
}