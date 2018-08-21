package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class ExperienceLevelsReward extends ExperienceReward
{
	public ExperienceLevelsReward(Quest quest, NBTTagCompound nbt)
	{
		super(quest, nbt);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentTranslation("ftbquests.reward.ftbquests.xp_levels.text", TextFormatting.GREEN + "+" + value);
	}

	@Override
	public ItemStack getRewardItem()
	{
		ItemStack stack = new ItemStack(FTBQuestsItems.XP_VIAL);

		if (!title.isEmpty())
		{
			stack.setStackDisplayName(title);
		}

		stack.setTagInfo("xp_levels", new NBTTagInt(value));
		return stack;
	}
}