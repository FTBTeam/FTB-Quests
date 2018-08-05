package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.BlockBase;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class BlockScreen extends BlockBase
{
	public static boolean BREAKING_SCREEN = false;

	public BlockScreen(String mod, String id)
	{
		super(mod, id, Material.GLASS, MapColor.BLACK);
		setTranslationKey("ftbquests.screen");
		setCreativeTab(CreativeTabs.DECORATIONS);
		setHardness(0.3F);
		setDefaultState(blockState.getBaseState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH));
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileScreen();
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, BlockHorizontal.FACING);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items)
	{
	}

	@Override
	@Deprecated
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.byHorizontalIndex(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
	}

	@Override
	@Deprecated
	public IBlockState withRotation(IBlockState state, Rotation rotation)
	{
		return state.withProperty(BlockHorizontal.FACING, rotation.rotate(state.getValue(BlockHorizontal.FACING)));
	}

	@Override
	@Deprecated
	public IBlockState withMirror(IBlockState state, Mirror mirror)
	{
		return state.withRotation(mirror.toRotation(state.getValue(BlockHorizontal.FACING)));
	}

	@Override
	@Deprecated
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return this.getDefaultState().withProperty(BlockHorizontal.FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (facing != state.getValue(BlockHorizontal.FACING))
		{
			return false;
		}

		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileScreenBase)
		{
			TileScreenBase base = (TileScreenBase) tileEntity;
			TileScreen screen = base.getScreen();

			if (screen != null)
			{
				double x = 0.5D; //FIXME: X coordinate
				screen.onClicked(player, x, 1D - (base.getOffsetY() + hitY) / (screen.size * 2D + 1D));
				return true;
			}
		}

		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(world, pos, state, placer, stack);

		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileScreen)
		{
			TileScreen screen = (TileScreen) tileEntity;
			screen.cachedFacing = state.getValue(BlockHorizontal.FACING);

			if (screen.size > 0)
			{
				IBlockState state1 = (this instanceof BlockFlatScreen ? FTBQuestsItems.FLAT_SCREEN_PART : FTBQuestsItems.SCREEN_PART).getDefaultState().withProperty(BlockHorizontal.FACING, screen.cachedFacing);

				boolean xaxis = state.getValue(BlockHorizontal.FACING).getAxis() == EnumFacing.Axis.X;

				for (int y = 0; y < screen.size * 2 + 1; y++)
				{
					for (int x = -screen.size; x <= screen.size; x++)
					{
						if (x != 0 || y != 0)
						{
							int offX = xaxis ? 0 : x;
							int offZ = xaxis ? x : 0;
							world.setBlockToAir(new BlockPos(pos.getX() + offX, pos.getY() + y, pos.getZ() + offZ));
						}
					}
				}

				for (int y = 0; y < screen.size * 2 + 1; y++)
				{
					for (int x = -screen.size; x <= screen.size; x++)
					{
						if (x != 0 || y != 0)
						{
							int offX = xaxis ? 0 : x;
							int offZ = xaxis ? x : 0;
							BlockPos pos1 = new BlockPos(pos.getX() + offX, pos.getY() + y, pos.getZ() + offZ);
							world.setBlockState(pos1, state1);

							TileEntity tileEntity1 = world.getTileEntity(pos1);

							if (tileEntity1 instanceof TileScreenPart)
							{
								((TileScreenPart) tileEntity1).setOffset(offX, y, offZ);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileScreen)
		{
			TileScreen screen = (TileScreen) tileEntity;

			if (screen.size > 0)
			{
				BREAKING_SCREEN = true;
				boolean xaxis = state.getValue(BlockHorizontal.FACING).getAxis() == EnumFacing.Axis.X;

				for (int y = 0; y < screen.size * 2 + 1; y++)
				{
					for (int x = -screen.size; x <= screen.size; x++)
					{
						if (x != 0 || y != 0)
						{
							int offX = xaxis ? 0 : x;
							int offZ = xaxis ? x : 0;
							BlockPos pos1 = new BlockPos(pos.getX() + offX, pos.getY() + y, pos.getZ() + offZ);
							IBlockState state1 = world.getBlockState(pos1);

							if (state1.getBlock() instanceof BlockScreenPart)
							{
								world.setBlockToAir(pos1);
							}
						}
					}
				}

				BREAKING_SCREEN = false;
			}
		}

		super.breakBlock(world, pos, state);
	}
}