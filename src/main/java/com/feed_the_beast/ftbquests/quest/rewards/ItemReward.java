package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
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
		super(quest, nbt);
		item = new ConfigItemStack(new ItemStack(nbt));
	}

	@Override
	public boolean isInvalid()
	{
		return item.getItem().isEmpty() || super.isInvalid();
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		NBTUtils.copyTags(item.getItem().serializeNBT(), nbt);
	}

	@Override
	public Icon getIcon()
	{
		return ItemIcon.getItemIcon(item.getItem());
	}

	@Override
	public ITextComponent getDisplayName()
	{
		ItemStack stack = item.getItem();

		if (stack.getCount() > 1)
		{
			return new TextComponentString(stack.getCount() + "x " + stack.getDisplayName()); //LANG
		}

		return new TextComponentString(stack.getDisplayName()); //LANG
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
		ItemHandlerHelper.giveItemToPlayer(player, item.getItem().copy());
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add(FTBQuests.MOD_ID, "item", item);
	}
}