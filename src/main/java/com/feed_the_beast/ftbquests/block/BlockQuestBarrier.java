package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.tile.TileQuestBarrier;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class BlockQuestBarrier extends Block
{
	public static final PropertyBool COMPLETED = PropertyBool.create("completed");
	private static final Predicate<Entity> PREDICATE = entity -> entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode;

	public BlockQuestBarrier()
	{
		super(Material.BARRIER, MapColor.LIGHT_BLUE);
		setDefaultState(blockState.getBaseState().withProperty(COMPLETED, false));
		translucent = true;
		setBlockUnbreakable();
		setResistance(6000000F);
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, COMPLETED);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return 0;
	}

	@Override
	@Deprecated
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState();
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	@Nullable
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileQuestBarrier();
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	@Deprecated
	public boolean isFullBlock(IBlockState state)
	{
		return false;
	}

	@Override
	@Deprecated
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	@Deprecated
	public float getAmbientOcclusionLightValue(IBlockState state)
	{
		return 1F;
	}

	@Override
	public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune)
	{
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity)
	{
		return false;
	}

	@Override
	@Deprecated
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState)
	{
		if (entity instanceof EntityPlayer)
		{
			if (((EntityPlayer) entity).capabilities.isCreativeMode)
			{
				return;
			}

			TileEntity tileEntity = world.getTileEntity(pos);

			if (tileEntity instanceof TileQuestBarrier)
			{
				QuestFile file = FTBQuests.PROXY.getQuestFile(world);

				if (file != null)
				{
					QuestObject object = ((TileQuestBarrier) tileEntity).getObject(file);
					QuestData data = file.getData(entity);

					if (object != null && data != null && object.isComplete(data))
					{
						return;
					}
				}
			}
		}

		super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entity, isActualState);
	}

	@Override
	@Nullable
	@Deprecated
	public RayTraceResult collisionRayTrace(IBlockState state, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
	{
		EntityPlayer player = worldIn.getClosestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5D, PREDICATE);

		if (player == null)
		{
			return null;
		}

		return rayTrace(pos, start, end, state.getBoundingBox(worldIn, pos));
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (player.getHeldItem(hand).getItem() == FTBQuestsItems.BARRIER)
		{
			return false;
		}

		if (!world.isRemote && FTBQuests.canEdit(player))
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if (tileEntity instanceof TileQuestBarrier)
			{
				((TileQuestBarrier) tileEntity).editConfig((EntityPlayerMP) player, true);
			}
		}

		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileQuestBarrier)
		{
			((TileQuestBarrier) tileEntity).readFromItem(stack);
		}
	}

	@Override
	@Deprecated
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileQuestBarrier && ((TileQuestBarrier) tileEntity).completed)
		{
			return state.withProperty(COMPLETED, true);
		}

		return state;
	}

	@Override
	@Deprecated
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing)
	{
		return world.getBlockState(pos.offset(facing)).getBlock() != this && super.shouldSideBeRendered(state, world, pos, facing);
	}
}