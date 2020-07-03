package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.BlockSpecialDrop;
import com.feed_the_beast.ftbquests.tile.TileRewardCollector;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class BlockRewardCollector extends BlockSpecialDrop
{
	public BlockRewardCollector()
	{
		super(Material.WOOD, MapColor.WOOD);
		setHardness(1.8F);
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileRewardCollector();
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileRewardCollector)
		{
			TileRewardCollector tile = (TileRewardCollector) tileEntity;
			tile.owner = placer.getUniqueID();
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileRewardCollector && player instanceof EntityPlayerMP)
		{
			((TileRewardCollector) tileEntity).onRightClick((EntityPlayerMP) player);
		}

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		tooltip.add(I18n.format("tile.ftbquests.reward_collector.tooltip"));
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileRewardCollector)
		{
			for (int i = 0; i < ((TileRewardCollector) tileEntity).inventory.getSlots(); i++)
			{
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), ((TileRewardCollector) tileEntity).inventory.getStackInSlot(i));
			}

			world.updateComparatorOutputLevel(pos, this);
		}

		super.breakBlock(world, pos, state);
	}
}