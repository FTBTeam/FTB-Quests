package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftbquests.tile.TileQuestBarrier;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class ItemBlockBarrier extends ItemBlock
{
	public ItemBlockBarrier(Block block)
	{
		super(block);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState))
		{
			TileEntity tileEntity = world.getTileEntity(pos.offset(side.getOpposite()));

			if (tileEntity instanceof TileQuestBarrier)
			{
				TileEntity barrier = world.getTileEntity(pos);

				if (barrier instanceof TileQuestBarrier)
				{
					((TileQuestBarrier) barrier).object = ((TileQuestBarrier) tileEntity).object;
					barrier.markDirty();
					BlockUtils.notifyBlockUpdate(world, pos, newState);
				}
			}

			return true;
		}

		return false;
	}
}