package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.item.ItemMissing;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemReward extends QuestReward
{
	public ItemStack stack;

	public ItemReward(Quest quest)
	{
		super(quest);
		stack = new ItemStack(Items.APPLE);
	}

	@Override
	public QuestRewardType getType()
	{
		return FTBQuestsRewards.ITEM;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setTag("item", ItemMissing.write(stack, false));
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		stack = ItemMissing.read(nbt.getTag("item"));
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeItemStack(stack);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		stack = data.readItemStack();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.add("item", new ConfigItemStack.SimpleStack(() -> stack, v -> stack = v), new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.item"));
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		ItemHandlerHelper.giveItemToPlayer(player, stack.copy());
	}

	@Override
	public Icon getAltIcon()
	{
		if (stack.isEmpty())
		{
			return Icon.getIcon("minecraft:items/diamond");
		}

		return ItemIcon.getItemIcon(stack);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentString(stack.getDisplayName());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list)
	{
		GuiHelper.addStackTooltip(stack, list, "");
	}
}