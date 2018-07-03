package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.BlockBase;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftbquests.net.MessageOpenTask;
import com.feed_the_beast.ftbquests.net.MessageSelectTaskGui;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class BlockQuest extends BlockBase
{
	public BlockQuest(String mod, String id)
	{
		super(mod, id, Material.WOOD, MapColor.WOOD);
		setCreativeTab(CreativeTabs.REDSTONE);
		setHardness(0.3F);
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileQuest();
	}

	@Override
	public boolean dropSpecial(IBlockState state)
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer ep, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
		{
			return true;
		}

		EntityPlayerMP player = (EntityPlayerMP) ep;
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileQuest)
		{
			TileQuest tile = (TileQuest) tileEntity;

			if (tile.getOwner() == null)
			{
				tile.setOwner(Universe.get().getPlayer(player).team.getName());
			}

			if (player.isSneaking())
			{
				if (tile.canEdit())
				{
					tile.setTask(0);
				}
			}
			else
			{
				QuestTaskData data = tile.getTaskData();

				if (data == null && Universe.get().getPlayer(player).team.getName().equals(tile.getOwner().getTeamID()))
				{
					if (tile.canEdit())
					{
						new MessageSelectTaskGui(pos).sendTo(player);
					}
				}
				else if (data != null)
				{
					MessageOpenTask.openGUI(data, player);
				}
			}
		}

		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack)
	{
		super.onBlockPlacedBy(world, pos, state, player, stack);
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileQuest)
		{
			TileQuest tile = (TileQuest) tileEntity;
			ItemBlockQuest.Data data = stack.getCapability(ItemBlockQuest.Data.CAP, null);

			if (data != null)
			{
				tile.setOwner(data.owner);
				tile.setTask(data.task);
			}

			if (player instanceof EntityPlayerMP)
			{
				if (tile.getOwner() == null)
				{
					tile.setOwner(Universe.get().getPlayer(player).team.getName());
				}

				if (tile.canEdit() && tile.getTaskData() == null && Universe.get().getPlayer(player).team.getName().equals(tile.getOwner().getTeamID()))
				{
					new MessageSelectTaskGui(pos).sendTo((EntityPlayerMP) player);
				}
			}
		}
	}

	@Override
	public ItemStack createStack(IBlockState state, @Nullable TileEntity tile)
	{
		ItemStack stack = new ItemStack(this);

		if (tile instanceof TileQuest)
		{
			TileQuest t = (TileQuest) tile;
			ItemBlockQuest.Data data = stack.getCapability(ItemBlockQuest.Data.CAP, null);

			if (data != null && t.getTaskData() != null)
			{
				data.owner = t.getOwnerTeam();
				data.task = t.getTaskID();
			}
		}

		return stack;
	}
}