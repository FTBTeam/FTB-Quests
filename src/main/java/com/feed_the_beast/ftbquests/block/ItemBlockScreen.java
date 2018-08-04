package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.ItemBlockBase;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemBlockScreen extends ItemBlockBase
{
	public ItemBlockScreen(Block block)
	{
		super(block);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		TileScreen screen = new TileScreen();
		screen.readFromItem(stack);

		if (screen.size > 0)
		{
			boolean xaxis = newState.getValue(BlockHorizontal.FACING).getAxis() == EnumFacing.Axis.X;

			for (int y = 0; y < screen.size * 2 + 1; y++)
			{
				for (int x = -screen.size; x <= screen.size; x++)
				{
					if (x != 0 || y != 0)
					{
						int offX = xaxis ? 0 : x;
						int offZ = xaxis ? x : 0;
						BlockPos pos1 = new BlockPos(pos.getX() + offX, pos.getY() + y, pos.getZ() + offZ);
						IBlockState state1 = world.getBlockState(pos1);

						if (!state1.getBlock().isReplaceable(world, pos1))
						{
							return false;
						}
					}
				}
			}
		}

		return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		if (world == null || !ClientQuestFile.existsWithTeam())
		{
			return;
		}

		TileScreen screen = new TileScreen();
		screen.setWorld(world);
		screen.readFromItem(stack);
		QuestTaskData data = screen.getTaskData();

		if (data == null)
		{
			return;
		}

		tooltip.add(TextFormatting.GOLD.toString() + (1 + screen.size * 2) + " x " + (1 + screen.size * 2));

		if (!ClientQuestFile.INSTANCE.teamId.equals(screen.owner))
		{
			tooltip.add(I18n.format("ftbquests.owner") + ": " + TextFormatting.DARK_GREEN + screen.owner);
		}

		tooltip.add(I18n.format("ftbquests.task") + ": " + TextFormatting.YELLOW + data.task.getDisplayName().getFormattedText());
	}
}