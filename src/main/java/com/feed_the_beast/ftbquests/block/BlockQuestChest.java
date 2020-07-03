package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.tile.TileQuestChest;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class BlockQuestChest extends BlockWithHorizontalFacing
{
	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375);

	public BlockQuestChest()
	{
		super(Material.WOOD, MapColor.GRAY);
		setHardness(1F);
	}

	@Override
	public boolean isToolEffective(String type, IBlockState state)
	{
		return "axe".equals(type) || "pickaxe".equals(type);
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileQuestChest();
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state)
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
	@Deprecated
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		return AABB;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		ItemStack stack = new ItemStack(FTBQuestsItems.CHEST);
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileQuestChest)
		{
			((TileQuestChest) tileEntity).writeToItem(stack);
		}

		return stack;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if (tileEntity instanceof TileQuestChest)
			{
				((TileQuestChest) tileEntity).openGui((EntityPlayerMP) player);
			}
		}

		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileQuestChest)
		{
			TileQuestChest tile = (TileQuestChest) tileEntity;
			tile.readFromItem(stack);
			tile.setIDFromPlacer(placer);
		}
	}

	@Override
	@Deprecated
	public float getBlockHardness(IBlockState state, World world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileQuestChest && ((TileQuestChest) tileEntity).indestructible)
		{
			return -1F;
		}

		return super.getBlockHardness(state, world, pos);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileQuestChest && ((TileQuestChest) tileEntity).indestructible)
		{
			return Float.MAX_VALUE;
		}

		return super.getExplosionResistance(world, pos, exploder, explosion);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		tooltip.add(TextFormatting.RED + I18n.format("tile.ftbquests.chest.deprecated"));
	}
}