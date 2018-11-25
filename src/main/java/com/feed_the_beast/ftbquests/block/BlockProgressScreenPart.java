package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class BlockProgressScreenPart extends BlockProgressScreen
{
	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileProgressScreenPart();
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		if (!BlockTaskScreen.BREAKING_SCREEN)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if (tileEntity instanceof TileProgressScreenPart)
			{
				TileProgressScreenCore screen = ((TileProgressScreenPart) tileEntity).getScreen();

				if (screen != null)
				{
					world.setBlockToAir(screen.getPos());
				}
			}
		}

		super.breakBlock(world, pos, state);
	}
}