package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.BlockSpecialDrop;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.tile.TileLootCrateStorage;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class BlockLootCrateStorage extends BlockSpecialDrop
{
	public BlockLootCrateStorage()
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
		return new TileLootCrateStorage(world);
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if (tileEntity instanceof TileLootCrateStorage)
			{
				TileLootCrateStorage tile = (TileLootCrateStorage) tileEntity;

				for (RewardTable table : ServerQuestFile.INSTANCE.rewardTables)
				{
					if (table.lootCrate != null && !table.lootCrate.stringID.isEmpty())
					{
						//FIXME: Send message to client with table contents
						ITextComponent component = new TextComponentString("");
						//component.appendSibling(table.getTitle().createCopy());
						component.appendSibling(new TextComponentString(table.lootCrate.stringID));
						component.appendText(": ");
						component.appendSibling(StringUtils.color(new TextComponentString(Integer.toString(tile.crates.getInt(table.lootCrate.stringID))), TextFormatting.GOLD));
						player.sendMessage(component);
					}
				}
			}
		}

		return true;
	}
}