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
import net.minecraft.util.text.TextComponentTranslation;
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
		QuestBlockData data = QuestBlockData.get(world.getTileEntity(pos));

		if (data == null)
		{
			return true;
		}
		else if (data.getOwner() == null)
		{
			data.setOwner(Universe.get().getPlayer(player).team.getName());
		}

		if (data.getOwner() != null && Universe.get().getPlayer(player).team.getName().equals(data.getOwner().getTeamID()))
		{
			if (player.isSneaking())
			{
				if (data.canEdit())
				{
					data.setTask(0);
				}
				else
				{
					player.sendMessage(new TextComponentTranslation("tile.ftbquests.quest_block.cant_edit"));
				}
			}
			else
			{
				QuestTaskData taskData = data.getTaskData();

				if (taskData == null)
				{
					if (data.canEdit())
					{
						new MessageSelectTaskGui(pos).sendTo(player);
					}
					else
					{
						player.sendMessage(new TextComponentTranslation("tile.ftbquests.quest_block.cant_edit"));
					}
				}
				else
				{
					MessageOpenTask.openGUI(taskData, player);
				}
			}
		}

		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack)
	{
		super.onBlockPlacedBy(world, pos, state, player, stack);
		QuestBlockData data = QuestBlockData.get(world.getTileEntity(pos));

		if (data != null)
		{
			data.copyFrom(QuestBlockData.get(stack));

			if (player instanceof EntityPlayerMP)
			{
				if (data.getOwner() == null)
				{
					data.setOwner(Universe.get().getPlayer(player).team.getName());
				}

				if (data.canEdit() && data.getTaskData() == null && Universe.get().getPlayer(player).team.getName().equals(data.getOwner().getTeamID()))
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
		QuestBlockData data = QuestBlockData.get(tile);

		if (data != null)
		{
			QuestBlockData.get(stack).copyFrom(data);
		}

		return stack;
	}
}