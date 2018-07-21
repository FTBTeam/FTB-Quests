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
	public static final String ID_LEVELS = "xp_levels";

	public ExperienceLevelsReward(Quest quest, NBTTagCompound nbt)
	{
		super(quest, nbt);
	}

	@Override
	public String getName()
	{
		return ID_LEVELS;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("ftbquests.gui.reward.xp_levels", TextFormatting.GREEN + "+" + value.getInt());
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
		player.addExperienceLevel(value.getInt());
	}
}