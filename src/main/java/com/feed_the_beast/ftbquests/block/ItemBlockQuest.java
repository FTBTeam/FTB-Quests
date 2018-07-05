package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.ItemBlockBase;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemBlockQuest extends ItemBlockBase
{
	public static class Data implements ICapabilitySerializable<NBTTagCompound>
	{
		@CapabilityInject(Data.class)
		public static Capability<Data> CAP;

		public static Data get(ItemStack stack)
		{
			return stack.getCapability(CAP, null);
		}

		public String owner = "";
		public int task = 0;
		private QuestTaskData cachedTaskData;

		public Data()
		{
		}

		@Nullable
		public QuestTaskData getTaskData()
		{
			if (cachedTaskData == null)
			{
				IProgressData o = FTBQuests.PROXY.getOwner(owner, FMLCommonHandler.instance().getEffectiveSide().isClient());

				if (o == null)
				{
					return null;
				}

				cachedTaskData = o.getQuestTaskData(task);
			}

			return cachedTaskData;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			if (capability == CAP)
			{
				return true;
			}

			cachedTaskData = getTaskData();
			return cachedTaskData != null && cachedTaskData.hasCapability(capability, facing);
		}

		@Override
		@Nullable
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			if (capability == CAP)
			{
				return (T) this;
			}

			cachedTaskData = getTaskData();
			return cachedTaskData != null ? cachedTaskData.getCapability(capability, facing) : null;
		}

		@Override
		public NBTTagCompound serializeNBT()
		{
			NBTTagCompound nbt = new NBTTagCompound();

			if (!owner.isEmpty())
			{
				nbt.setString("Owner", owner);
			}

			if (task > 0)
			{
				nbt.setInteger("Task", task);
			}

			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt)
		{
			owner = nbt.getString("Owner");
			task = nbt.getInteger("Task");
			cachedTaskData = null;
		}
	}

	public ItemBlockQuest(Block block)
	{
		super(block);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
	{
		return new Data();
	}

	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack)
	{
		return Data.get(stack).serializeNBT();
	}

	@Override
	public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt)
	{
		if (nbt != null)
		{
			Data.get(stack).deserializeNBT(nbt);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		if (!ClientQuestList.exists())
		{
			return;
		}

		QuestTaskData data = Data.get(stack).getTaskData();

		if (data == null)
		{
			return;
		}

		tooltip.add(I18n.format("tile.ftbquests.quest_block.tooltip.task") + ": " + TextFormatting.YELLOW + data.task.getDisplayName()); //LANG
		int max = data.task.getMaxProgress();

		if (max <= 0)
		{
			tooltip.add(I18n.format("tile.ftbquests.quest_block.tooltip.progress") + ": " + TextFormatting.BLUE + "0/0 [0%]"); //LANG
		}
		else
		{
			int progress = data.getProgress();

			if (progress >= max)
			{
				tooltip.add(I18n.format("tile.ftbquests.quest_block.tooltip.progress") + ": " + TextFormatting.BLUE + max + "/" + max + " [100%]");
			}
			else
			{
				tooltip.add(I18n.format("tile.ftbquests.quest_block.tooltip.progress") + ": " + TextFormatting.BLUE + progress + "/" + max + " [" + (int) (progress * 100D / (double) max) + "%]");
			}
		}

		if (!ClientQuestList.INSTANCE.teamId.equals(data.data.getTeamID()))
		{
			tooltip.add(I18n.format("tile.ftbquests.quest_block.tooltip.owner") + ": " + TextFormatting.DARK_GREEN + data.data.getTeamID()); //LANG
		}
	}
}