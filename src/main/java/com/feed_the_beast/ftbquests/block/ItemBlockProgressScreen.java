package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
public class ItemBlockProgressScreen extends ItemBlock
{
	public ItemBlockProgressScreen(Block block)
	{
		super(block);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		NBTTagCompound nbt = stack.getTagCompound();

		int size = nbt == null ? 0 : nbt.getByte("Size");

		if (size > 0)
		{
			boolean xaxis = newState.getValue(BlockHorizontal.FACING).getAxis() == EnumFacing.Axis.X;

			for (int y = 0; y < size * 2 + 1; y++)
			{
				for (int x = -size; x <= size; x++)
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

		if (nbt == null || !nbt.hasKey("Owner"))
		{
			if (nbt == null)
			{
				nbt = new NBTTagCompound();
				stack.setTagCompound(nbt);
			}

			nbt.setString("Owner", FTBLibAPI.getTeam(player.getUniqueID()));
		}

		//BlockScreen.currentTask = quest.getTask(nbt.getByte("TaskIndex"));
		return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
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

		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}

		int size = nbt.getByte("Size");
		String owner = nbt.getString("Owner");

		if (owner.isEmpty())
		{
			owner = ClientQuestFile.INSTANCE.teamId;
		}

		tooltip.add(I18n.format("tile.ftbquests.screen.size") + ": " + TextFormatting.GOLD + (1 + size * 2) + " x " + (1 + size * 2));
		tooltip.add(I18n.format("ftbquests.team") + ": " + TextFormatting.DARK_GREEN + owner);

		ProgressingQuestObject object = ClientQuestFile.INSTANCE.getProgressing(nbt.getString("Object"));

		if (object == null)
		{
			tooltip.add(TextFormatting.RED + I18n.format("tile.ftbquests.screen.missing_data"));
			return;
		}

		tooltip.add(StringUtils.color(object.getDisplayName(), TextFormatting.YELLOW).getFormattedText());

		IProgressData data = ClientQuestFile.INSTANCE.getData(owner);

		if (data != null)
		{
			tooltip.add(I18n.format("ftbquests.progress") + ": " + TextFormatting.BLUE + String.format("%s / %s [%d%%]", object.getProgress(data), object.getMaxProgress(), (int) (object.getRelativeProgress(data) * 100D)));
		}
	}
}