package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class ExperienceLevelsReward extends QuestReward
{
	private final int xpLevels;

	public ExperienceLevelsReward(Quest quest, int id, int _xp)
	{
		super(quest, id);
		xpLevels = _xp;
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
		player.addExperienceLevel(xpLevels);
	}

	@Override
	public Icon getIcon()
	{
		return ExperienceReward.ICON;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("type", "xp_levels");
		nbt.setInteger("value", xpLevels);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return I18n.format("ftbquests.reward.xp_levels", TextFormatting.GREEN + "+" + xpLevels);
	}
}