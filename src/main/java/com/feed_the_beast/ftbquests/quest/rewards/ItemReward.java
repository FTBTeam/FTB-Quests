package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.InvUtils;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class ItemReward extends QuestReward
{
	private final ItemStack stack;

	public ItemReward(Quest quest, int id, ItemStack is)
	{
		super(quest, id);
		stack = is;
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
		InvUtils.giveItem(player, stack);
	}

	@Override
	public Icon getIcon()
	{
		return ItemIcon.getItemIcon(stack);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("type", "item");
		NBTUtils.copyTags(stack.serializeNBT(), nbt);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		if (stack.getCount() > 1)
		{
			return stack.getCount() + "x " + stack.getDisplayName();
		}

		return stack.getDisplayName();
	}
}