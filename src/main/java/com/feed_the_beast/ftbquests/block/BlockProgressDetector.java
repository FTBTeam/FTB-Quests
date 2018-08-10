package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.tile.TileProgressDetector;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class BlockProgressDetector extends Block
{
	public BlockProgressDetector()
	{
		super(Material.IRON);
		setCreativeTab(FTBQuests.TAB);
		setHardness(1F);
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileProgressDetector();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if (tileEntity instanceof TileProgressDetector)
			{
				((TileProgressDetector) tileEntity).editConfig((EntityPlayerMP) player);
			}
		}

		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if (placer instanceof EntityPlayerMP)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if (tileEntity instanceof TileProgressDetector)
			{
				((TileProgressDetector) tileEntity).owner.setString(FTBLibAPI.getTeam(placer.getUniqueID()));
			}
		}
	}

	@Override
	@Deprecated
	public boolean canProvidePower(IBlockState state)
	{
		return true;
	}

	@Override
	@Deprecated
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressDetector)
		{
			return ((TileProgressDetector) tileEntity).redstoneOutput;
		}

		return 0;
	}

	@Override
	@Deprecated
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return state.getWeakPower(world, pos, side);
	}
}