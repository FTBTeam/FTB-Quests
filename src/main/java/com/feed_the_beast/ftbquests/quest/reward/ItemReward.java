package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.item.ItemMissing;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
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

	public ItemReward(Quest q, int id, NBTTagCompound nbt)
	{
		super(q, id);
		stack = ItemMissing.read(nbt.getTag("item"));
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setTag("item", ItemMissing.write(stack, false));
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		config.add("item", new ConfigItemStack.SimpleStack(() -> stack, v -> stack = v), new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.item"));
	}

	@Override
	public void claim(EntityPlayer player)
	{
		ItemHandlerHelper.giveItemToPlayer(player, stack.copy());
	}

	@Override
	public Icon getAltIcon()
	{
		return Icon.getIcon("minecraft:items/diamond");
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentString((stack.getCount() == 1 ? "" : (stack.getCount() + "x ")) + stack.getDisplayName());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list)
	{
		GuiHelper.addStackTooltip(stack, list, "");
	}
}