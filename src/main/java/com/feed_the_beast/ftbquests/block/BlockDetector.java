package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.tile.IHasConfig;
import com.feed_the_beast.ftbquests.tile.TilePlayerDetector;
import com.feed_the_beast.ftbquests.tile.TileRedstoneDetector;
import com.feed_the_beast.ftbquests.tile.TileWithTeam;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class BlockDetector extends Block
{
	public enum Variant implements IStringSerializable
	{
		REDSTONE("redstone", TileRedstoneDetector.class, TileRedstoneDetector::new),
		PLAYER("player", TilePlayerDetector.class, TilePlayerDetector::new);

		public static final Variant[] VALUES = values();

		private final String name;
		public final Class<? extends TileBase> clazz;
		private final Supplier<TileBase> tileEntitySupplier;

		Variant(String n, Class<? extends TileBase> c, Supplier<TileBase> t)
		{
			name = n;
			clazz = c;
			tileEntitySupplier = t;
		}

		@Override
		public String getName()
		{
			return name;
		}
	}

	public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

	public BlockDetector()
	{
		super(Material.IRON);
		setHardness(1F);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, Variant.REDSTONE));
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, VARIANT);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(VARIANT).ordinal();
	}

	@Override
	@Deprecated
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(VARIANT, Variant.VALUES[meta]);
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return state.getValue(VARIANT).tileEntitySupplier.get();
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return state.getValue(VARIANT).ordinal();
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		for (Variant variant : Variant.VALUES)
		{
			items.add(new ItemStack(this, 1, variant.ordinal()));
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote && FTBQuests.canEdit(player))
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if (tileEntity instanceof IHasConfig)
			{
				((IHasConfig) tileEntity).editConfig((EntityPlayerMP) player, true);
			}
		}

		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileBase)
		{
			((TileBase) tileEntity).readFromItem(stack);

			if (tileEntity instanceof TileWithTeam)
			{
				((TileWithTeam) tileEntity).setIDFromPlacer(placer);
			}
		}
	}

	@Override
	@Deprecated
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
	{
		if (state.getValue(VARIANT) == Variant.REDSTONE)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if (tileEntity instanceof TileRedstoneDetector)
			{
				((TileRedstoneDetector) tileEntity).checkRedstone();
			}
		}
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side)
	{
		return state.getValue(VARIANT) == Variant.REDSTONE;
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
		QuestObject object = nbt == null ? null : ClientQuestFile.INSTANCE.get(nbt.getInteger("object"));

		if (object != null)
		{
			tooltip.add(object.getYellowDisplayName());
		}
	}
}