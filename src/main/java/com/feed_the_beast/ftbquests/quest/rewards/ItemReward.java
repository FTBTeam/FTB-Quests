package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * @author LatvianModder
 */
public class ItemReward extends QuestReward
{
	private ItemStack item;

	public ItemReward(Quest quest, NBTTagCompound nbt)
	{
		super(quest);
		NBTTagCompound nbt1 = nbt.getCompoundTag("item");

		if (nbt1.isEmpty())
		{
			item = new ItemStack(Items.APPLE);
		}
		else
		{
			item = new ItemStack(nbt1);

			if (item.isEmpty())
			{
				item = ItemStack.EMPTY;
			}
		}
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setTag("item", item.serializeNBT());
	}

	@Override
	public Icon getAltIcon()
	{
		return ItemIcon.getItemIcon(item);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		if (item.getCount() > 1)
		{
			return new TextComponentString(item.getCount() + "x " + item.getDisplayName());
		}

		return new TextComponentString(item.getDisplayName());
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
		ItemHandlerHelper.giveItemToPlayer(player, item.copy());
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		super.getConfig(group);

		group.add("item", new ConfigItemStack(ItemStack.EMPTY)
		{
			@Override
			public ItemStack getStack()
			{
				return item;
			}

			@Override
			public void setStack(ItemStack v)
			{
				item = v;
			}
		}, new ConfigItemStack(new ItemStack(Items.APPLE)));
	}
}