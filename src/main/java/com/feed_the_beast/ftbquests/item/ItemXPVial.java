package com.feed_the_beast.ftbquests.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemXPVial extends ItemReward
{
	@Override
	public void reward(EntityPlayer player, ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();

		int size = player.isSneaking() ? stack.getCount() : 1;

		if (nbt != null)
		{
			int xpLevels = nbt.getInteger("xp_levels");

			if (xpLevels > 0)
			{
				player.addExperienceLevel(xpLevels * size);
			}
			else
			{
				player.addExperience(nbt.getInteger("xp") * size);
			}
		}
	}

	@Override
	public SoundEvent getSoundEvent()
	{
		return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt != null)
		{
			int xpLevels = nbt.getInteger("xp_levels");

			if (xpLevels > 0)
			{
				tooltip.add(I18n.format("ftbquests.reward.ftbquests.xp_levels.text", TextFormatting.GREEN + "+" + xpLevels));
			}
			else
			{
				tooltip.add(I18n.format("ftbquests.reward.ftbquests.xp.text", TextFormatting.GREEN + "+" + nbt.getInteger("xp")));
			}
		}
	}
}