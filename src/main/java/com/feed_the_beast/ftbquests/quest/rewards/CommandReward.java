package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class CommandReward extends QuestReward
{
	private static final Pattern CMD_PATTERN = Pattern.compile("^/.+$");

	private String command;

	public CommandReward(Quest quest, NBTTagCompound nbt)
	{
		super(quest);
		command = nbt.getString("command");
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("command", command);
	}

	@Override
	public Icon getAltIcon()
	{
		return ItemIcon.getItemIcon(new ItemStack(Blocks.COMMAND_BLOCK));
	}

	@Override
	public ItemStack getRewardItem()
	{
		ItemStack stack = new ItemStack(FTBQuestsItems.SCRIPT);

		if (!title.isEmpty())
		{
			stack.setStackDisplayName(title);
		}

		stack.setTagInfo("command", new NBTTagString(command));
		return stack;
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentTranslation("ftbquests.reward.ftbquests.command.text", TextFormatting.GREEN + command);
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("command", new ConfigString("", CMD_PATTERN)
		{
			@Override
			public String getString()
			{
				return command;
			}

			@Override
			public void setString(String v)
			{
				command = v;
			}
		}, new ConfigString(""));
	}
}