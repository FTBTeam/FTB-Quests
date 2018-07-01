package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.ItemBlockBase;
import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ItemBlockQuest extends ItemBlockBase
{
	private static class CapabilityProvider implements ICapabilityProvider
	{
		private final ItemStack stack;
		private QuestTaskData cachedTaskData;

		private CapabilityProvider(ItemStack is)
		{
			stack = is;
		}

		@Nullable
		private QuestTaskData getTaskData()
		{
			if (cachedTaskData == null)
			{
				NBTTagCompound nbt = CommonUtils.getBlockData(stack);

				if (nbt.hasNoTags() || !nbt.hasKey("Owner") || !nbt.hasKey("Task"))
				{
					return null;
				}

				IProgressData owner = FTBQuests.PROXY.getOwner(nbt.getString("Owner"), FMLCommonHandler.instance().getEffectiveSide().isClient());

				if (owner == null)
				{
					return null;
				}

				cachedTaskData = owner.getQuestTaskData(nbt.getInteger("Task"));
			}

			return cachedTaskData;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			cachedTaskData = getTaskData();
			return cachedTaskData != null && cachedTaskData.hasCapability(capability, facing);
		}

		@Override
		@Nullable
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			cachedTaskData = getTaskData();
			return cachedTaskData != null ? cachedTaskData.getCapability(capability, facing) : null;
		}
	}

	public ItemBlockQuest(Block block)
	{
		super(block, false);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
	{
		return new CapabilityProvider(stack);
	}
}