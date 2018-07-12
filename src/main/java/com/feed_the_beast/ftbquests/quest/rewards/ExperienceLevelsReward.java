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
public class ExperienceLevelsReward extends QuestReward
{
	public static final String ID = "xp_levels";

	private final int value;

	public ExperienceLevelsReward(Quest quest, int id, NBTTagCompound nbt)
	{
		super(quest, id);
		value = nbt.getInteger("value");
	}

	@Override
	public boolean isInvalid()
	{
		return value <= 0 || super.isInvalid();
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("value", value);
	}

	@Override
	public Icon getIcon()
	{
		return ItemIcon.getItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return I18n.format("ftbquests.gui.reward.xp_levels", TextFormatting.GREEN + "+" + value);
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
		player.addExperienceLevel(value);
	}
}