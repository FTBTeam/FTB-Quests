package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class ItemBlockTaskScreen extends ItemBlock
{
	public ItemBlockTaskScreen(Block block)
	{
		super(block);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		TileTaskScreenCore t = BlockTaskScreen.getStatic();
		t.resetData();
		t.readFromItem(stack);

		if (t.size > 0)
		{
			boolean xaxis = newState.getValue(BlockHorizontal.FACING).getAxis() == EnumFacing.Axis.X;

			for (int y = 0; y < t.size * 2 + 1; y++)
			{
				for (int x = -t.size; x <= t.size; x++)
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

		BlockTaskScreen.currentTask = null;
		QuestFile file = FTBQuests.PROXY.getQuestFile(world);

		if (file != null)
		{
			BlockTaskScreen.currentTask = file.getTask(t.task);
		}

		return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
	}
}