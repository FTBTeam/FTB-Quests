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
	public static final String ID = "item";
	private final ConfigItemStack item;

	public ItemReward(Quest quest, NBTTagCompound nbt)
	{
		super(quest);
		NBTTagCompound nbt1 = nbt.getCompoundTag("item");

		if (nbt1.isEmpty())
		{
			item = new ConfigItemStack(new ItemStack(Items.APPLE));
		}
		else
		{
			item = new ConfigItemStack(new ItemStack(nbt1));
		}
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setTag("item", item.getStack().serializeNBT());
	}

	@Override
	public Icon getIcon()
	{
		return ItemIcon.getItemIcon(item.getStack());
	}

	@Override
	public ITextComponent getDisplayName()
	{
		ItemStack stack = item.getStack();

		if (stack.getCount() > 1)
		{
			return new TextComponentString(stack.getCount() + "x " + stack.getDisplayName()); //LANG
		}

		return new TextComponentString(stack.getDisplayName()); //LANG
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
		ItemHandlerHelper.giveItemToPlayer(player, item.getStack().copy());
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		super.getConfig(group);
		group.add("item", item, new ConfigItemStack(new ItemStack(Items.APPLE)));
	}
}