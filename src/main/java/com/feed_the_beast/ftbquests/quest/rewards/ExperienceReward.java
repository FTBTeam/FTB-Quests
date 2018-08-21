package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class ExperienceReward extends QuestReward
{
	protected int value;

	public ExperienceReward(Quest quest, NBTTagCompound nbt)
	{
		super(quest);
		value = MathHelper.clamp(nbt.getInteger("value"), 1, Integer.MAX_VALUE);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("value", value);
	}

	@Override
	public Icon getAltIcon()
	{
		return ItemIcon.getItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE));
	}

	@Override
	public ItemStack getRewardItem()
	{
		ItemStack stack = new ItemStack(FTBQuestsItems.XP_VIAL);

		if (!title.isEmpty())
		{
			stack.setStackDisplayName(title);
		}

		stack.setTagInfo("xp", new NBTTagInt(value));
		return stack;
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentTranslation("ftbquests.reward.ftbquests.xp.text", TextFormatting.GREEN + "+" + value);
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		super.getConfig(group);

		group.add("value", new ConfigInt(value, 1, Integer.MAX_VALUE)
		{
			@Override
			public int getInt()
			{
				return value;
			}

			@Override
			public void setInt(int v)
			{
				value = v;
			}
		}, new ConfigInt(1));
	}
}