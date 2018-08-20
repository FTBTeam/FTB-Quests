package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
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
	public void reward(EntityPlayerMP player)
	{
		player.addExperienceLevel(value);
	}
}