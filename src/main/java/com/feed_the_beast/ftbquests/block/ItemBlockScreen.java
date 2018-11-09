package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class ItemBlockScreen extends ItemBlock
{
	public ItemBlockScreen(Block block)
	{
		super(block);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		NBTTagCompound nbt = stack.getTagCompound();

		int size = nbt == null ? 0 : nbt.getByte("Size");

		if (size > 0)
		{
			boolean xaxis = newState.getValue(BlockHorizontal.FACING).getAxis() == EnumFacing.Axis.X;

			for (int y = 0; y < size * 2 + 1; y++)
			{
				for (int x = -size; x <= size; x++)
				{
					if (x != 0 || y != 0)
					{
						int offX = xaxis ? 0 : x;
						int offZ = xaxis ? x : 0;
						BlockPos pos1 = new BlockPos(pos.getX() + offX, pos.getY() + y, pos.getZ() + offZ);
						IBlockState state1 = world.getBlockState(pos1);

						if (!state1.getBlock().isReplaceable(world, pos1))
						{
							return false;
						}
					}
				}
			}
		}

		BlockScreen.currentTask = null;
		QuestFile file = FTBQuests.PROXY.getQuestFile(world);
		Quest quest = file.getQuest(file.getID(nbt == null ? null : nbt.getTag("Quest")));

		if (quest != null && !quest.tasks.isEmpty())
		{
			NBTBase task = nbt == null ? null : nbt.getTag("Task");

			if (task == null || task.isEmpty())
			{
				BlockScreen.currentTask = quest.tasks.get(0);
			}
			else if (task instanceof NBTTagString)
			{
				BlockScreen.currentTask = file.getTask(file.getID(quest.getID() + ':' + task));
			}
			else if (task instanceof NBTPrimitive)
			{
				BlockScreen.currentTask = file.getTask(((NBTPrimitive) task).getInt());
			}
		}

		return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
	}
}