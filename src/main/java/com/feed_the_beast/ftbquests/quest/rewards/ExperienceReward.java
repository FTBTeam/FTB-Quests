package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class ExperienceReward extends QuestReward
{
	public static final Icon ICON = ItemIcon.getItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE));

	private final int xp;

	public ExperienceReward(Quest quest, int id, int _xp)
	{
		super(quest, id);
		xp = _xp;
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
		player.addExperience(xp);
	}

	@Override
	public Icon getIcon()
	{
		return ICON;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("xp", xp);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return I18n.format("ftbquests.reward.xp", TextFormatting.GREEN + "+" + xp);
	}
}