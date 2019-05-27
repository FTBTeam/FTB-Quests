package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.tile.TileProgressDetector;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class BlockProgressDetector extends Block
{
	public BlockProgressDetector()
	{
		super(Material.IRON);
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
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		ItemStack stack = new ItemStack(this);

		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressDetector)
		{
			((TileProgressDetector) tileEntity).writeToItem(stack);
		}

		return stack;
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
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressDetector)
		{
			TileProgressDetector tile = (TileProgressDetector) tileEntity;
			tile.readFromItem(stack);
			tile.setIDFromPlacer(placer);

			if (tile.object == 0)
			{
				tile.object = 1;
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
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		if (world == null || !ClientQuestFile.exists())
		{
			return;
		}

		NBTTagCompound nbt = stack.getTagCompound();
		QuestObject object = nbt == null ? null : ClientQuestFile.INSTANCE.get(nbt.getInteger("Object"));

		if (object != null)
		{
			tooltip.add(object.getYellowDisplayName().getFormattedText());
		}
	}
}