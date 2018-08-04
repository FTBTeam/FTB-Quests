package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class CommandReward extends QuestReward
{
	public static final String ID = "command";
	private static final Pattern CMD_PATTERN = Pattern.compile("^/.*$");

	protected final ConfigString command;

	public CommandReward(Quest quest, NBTTagCompound nbt)
	{
		super(quest, nbt);
		command = new ConfigString(nbt.getString("command"), CMD_PATTERN);
	}

	@Override
	public boolean isInvalid()
	{
		return command.isEmpty() || super.isInvalid();
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("command", command.getString());
	}

	@Override
	public Icon getIcon()
	{
		return ItemIcon.getItemIcon(new ItemStack(Blocks.COMMAND_BLOCK));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("ftbquests.reward.command.text", TextFormatting.GREEN + command.getString());
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
		player.server.getCommandManager().executeCommand(player.server, command.getString().replace("@p", player.getName()));
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		super.getConfig(group);
		group.add("command", command, new ConfigString("", CMD_PATTERN));
	}
}