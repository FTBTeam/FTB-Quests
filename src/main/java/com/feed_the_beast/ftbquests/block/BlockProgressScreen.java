package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenBase;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenPart;
import com.feed_the_beast.ftbquests.tile.TileScreenBase;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * @author LatvianModder
 */
public class BlockProgressScreen extends BlockHorizontal
{
	public BlockProgressScreen()
	{
		super(Material.IRON);
		setCreativeTab(FTBQuests.TAB);
		setHardness(0.3F);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileProgressScreenCore();
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		items.add(new ItemStack(this));

		if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.editingMode)
		{
			for (int i = 1; i <= 4; i++)
			{
				ItemStack stack = new ItemStack(this);
				stack.setTagInfo("Size", new NBTTagByte((byte) i));
				items.add(stack);
			}
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return Item.getItemFromBlock(FTBQuestsItems.SCREEN);
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 0;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		ItemStack stack = new ItemStack(FTBQuestsItems.PROGRESS_SCREEN);

		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileScreenBase)
		{
			TileScreenCore screen = ((TileScreenBase) tileEntity).getScreen();

			if (screen != null)
			{
				screen.writeToItem(stack);
			}
		}

		return stack;
	}

	@Override
	@Deprecated
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	@Deprecated
	public IBlockState withRotation(IBlockState state, Rotation rotation)
	{
		return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	@Deprecated
	public IBlockState withMirror(IBlockState state, Mirror mirror)
	{
		return state.withRotation(mirror.toRotation(state.getValue(FACING)));
	}

	@Override
	@Deprecated
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (facing != state.getValue(FACING))
		{
			return false;
		}

		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileScreenBase)
		{
			TileScreenBase base = (TileScreenBase) tileEntity;
			TileScreenCore screen = base.getScreen();

			if (screen != null)
			{
				if (player instanceof EntityPlayerMP)
				{
					double x = 0.5D; //FIXME: X coordinate
					screen.onClicked((EntityPlayerMP) player, hand, x, 1D - (base.getOffsetY() + hitY) / (screen.size * 2D + 1D));
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileScreenCore)
		{
			TileScreenCore screen = (TileScreenCore) tileEntity;
			screen.readFromItem(stack);
			screen.facing = state.getValue(FACING);

			if (screen.size > 0)
			{
				IBlockState state1 = FTBQuestsItems.PROGRESS_SCREEN_PART.getDefaultState().withProperty(FACING, screen.getFacing());

				boolean xaxis = state.getValue(FACING).getAxis() == EnumFacing.Axis.X;

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

							if (tileEntity1 instanceof TileProgressScreenPart)
							{
								((TileProgressScreenPart) tileEntity1).setOffset(offX, y, offZ);
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

		if (tileEntity instanceof TileProgressScreenCore)
		{
			TileProgressScreenCore screen = (TileProgressScreenCore) tileEntity;

			if (screen.size > 0)
			{
				BlockScreen.BREAKING_SCREEN = true;
				boolean xaxis = state.getValue(FACING).getAxis() == EnumFacing.Axis.X;

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

							if (state1.getBlock() == FTBQuestsItems.PROGRESS_SCREEN_PART)
							{
								world.setBlockToAir(pos1);
							}
						}
					}
				}

				BlockScreen.BREAKING_SCREEN = false;
			}
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	@Deprecated
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenBase)
		{
			TileProgressScreenCore screen = ((TileProgressScreenBase) tileEntity).getScreen();

			if (screen != null)
			{
				return BlockScreen.getScreenAABB(screen.getPos(), screen.getFacing(), screen.size);
			}
		}

		return new AxisAlignedBB(0D, -1D, 0D, 0D, -1D, 0D);
	}

	@Override
	@Deprecated
	public float getBlockHardness(IBlockState state, World world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenBase)
		{
			TileProgressScreenCore core = ((TileProgressScreenBase) tileEntity).getScreen();

			if (core != null && core.indestructible.getBoolean())
			{
				return -1F;
			}
		}

		return super.getBlockHardness(state, world, pos);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenBase)
		{
			TileProgressScreenCore core = ((TileProgressScreenBase) tileEntity).getScreen();

			if (core != null && core.indestructible.getBoolean())
			{
				return Float.MAX_VALUE;
			}
		}

		return super.getExplosionResistance(world, pos, exploder, explosion);
	}

	@Override
	@Deprecated
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenBase)
		{
			TileProgressScreenCore core = ((TileProgressScreenBase) tileEntity).getScreen();

			if (core != null && !core.skin.isEmpty())
			{
				return core.skin.getBlockState();
			}
		}

		return state;
	}
}