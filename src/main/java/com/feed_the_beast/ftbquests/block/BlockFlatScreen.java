package com.feed_the_beast.ftbquests.block;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * @author LatvianModder
 */
public class BlockFlatScreen extends BlockScreen
{
	private static final double S0 = 1D / 16D;
	private static final double S1 = 1D - S0;

	public static final AxisAlignedBB[] BOXES = {
			new AxisAlignedBB(0, 0, 0, 1, 1, S0), // south
			new AxisAlignedBB(S1, 0, 0, 1, 1, 1), // west
			new AxisAlignedBB(0, 0, S1, 1, 1, 1), // north
			new AxisAlignedBB(0, 0, 0, S0, 1, 1), // east
	};

	public BlockFlatScreen(String mod, String id)
	{
		super(mod, id);
	}

	@Override
	@Deprecated
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		return BOXES[state.getValue(BlockHorizontal.FACING).getHorizontalIndex()];
	}

	@Override
	@Deprecated
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}
}