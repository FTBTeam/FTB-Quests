package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.quest.tasks.FluidTask;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.SlotItemHandler;

/**
 * @author LatvianModder
 */
public class ContainerFluidTask extends ContainerTaskBase
{
	private final Icon iconBucketEmpty, iconBucketFilled;

	public ContainerFluidTask(EntityPlayer player, FluidTask.Data d)
	{
		super(player, d);
		iconBucketEmpty = ItemIcon.getItemIcon(new ItemStack(Items.BUCKET));
		iconBucketFilled = ItemIcon.getItemIcon(FluidUtil.getFilledBucket(d.task.createFluidStack(1000)));
	}

	@Override
	public void addTaskSlots()
	{
		addSlotToContainer(new SlotItemHandler((FluidTask.Data) data, 0, 80 - 27, 34)
		{
			@Override
			public void putStack(ItemStack stack)
			{
				getItemHandler().insertItem(0, stack, false);
				onSlotChanged();
			}
		});

		addSlotToContainer(new SlotItemHandler((FluidTask.Data) data, 1, 80 + 27, 34)
		{
			@Override
			public void putStack(ItemStack stack)
			{
			}

			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return false;
			}
		});
	}

	@Override
	public int getNonPlayerSlots()
	{
		return 2;
	}

	@Override
	public Icon getEmptySlotIcon(int slot)
	{
		if (slot == 0)
		{
			return iconBucketFilled;
		}
		else if (slot == 1)
		{
			return iconBucketEmpty;
		}

		return Icon.EMPTY;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getEmptySlotText(int slot)
	{
		if (slot == 0)
		{
			return I18n.format("ftbquests.task.fluid.slot.fluid", ((FluidTask.Data) data).task.createFluidStack(1000).getLocalizedName());
		}
		else if (slot == 1)
		{
			return I18n.format("ftbquests.task.fluid.slot.container");
		}

		return "";
	}
}