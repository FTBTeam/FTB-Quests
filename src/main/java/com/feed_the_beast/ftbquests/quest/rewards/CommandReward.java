package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
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

	private final ConfigString command;
	private final ConfigString title;
	private final ConfigItemStack icon;

	public CommandReward(Quest quest, NBTTagCompound nbt)
	{
		super(quest, nbt);
		command = new ConfigString(nbt.getString("command"), CMD_PATTERN);
		title = new ConfigString(nbt.getString("title"));
		icon = new ConfigItemStack(nbt.hasKey("icon") ? new ItemStack(nbt.getCompoundTag("icon")) : new ItemStack(Blocks.COMMAND_BLOCK), true);
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
		nbt.setString("title", title.getString());
		nbt.setTag("icon", icon.getStack().serializeNBT());
	}

	@Override
	public Icon getIcon()
	{
		return ItemIcon.getItemIcon(icon.getStack());
	}

	@Override
	public ITextComponent getDisplayName()
	{
		if (!title.isEmpty())
		{
			return new TextComponentString(title.getString());
		}

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
		group.add("title", title, new ConfigString(""));
		group.add("icon", icon, new ConfigItemStack(new ItemStack(Blocks.COMMAND_BLOCK)));
	}
}