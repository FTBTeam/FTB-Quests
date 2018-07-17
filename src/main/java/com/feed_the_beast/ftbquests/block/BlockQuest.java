package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.BlockBase;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.net.MessageOpenTask;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	public BlockRenderLayer getRenderLayer()
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

		TileEntity tileEntity = world.getTileEntity(pos);

		if (!(tileEntity instanceof TileQuest))
		{
			return true;
		}

		TileQuest tile = (TileQuest) tileEntity;

		if (tile.data == null)
		{
			return true;
		}

		EntityPlayerMP player = (EntityPlayerMP) ep;
		QuestTaskData taskData = tile.data.getTaskData();

		if (taskData == null)
		{
			player.sendMessage(StringUtils.color(new TextComponentTranslation("tile.ftbquests.quest_block.missing_data"), TextFormatting.RED));
		}
		else if (Universe.get().getPlayer(player).team.getName().equals(taskData.data.getTeamID()))
		{
			MessageOpenTask.openGUI(taskData, player, tile);
		}
		else
		{
			player.sendMessage(StringUtils.color(new TextComponentTranslation("tile.ftbquests.quest_block.no_perm"), TextFormatting.RED));
		}

		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		QuestBlockData itemData = QuestBlockData.get(stack);

		if (itemData.getTaskData() == null)
		{
			return;
		}

		TileQuest tile = itemData.getTaskData().task.createCustomTileEntity(world);

		if (tile != null)
		{
			if (tileEntity != null)
			{
				tileEntity.invalidate();
			}

			tile.readFromItem(stack);
			world.removeTileEntity(pos);
			world.setTileEntity(pos, tile);
			tile.validate();
		}
		else if (tileEntity instanceof TileQuest)
		{
			((TileQuest) tileEntity).readFromItem(stack);
			tileEntity.onLoad();
		}
	}
}