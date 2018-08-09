package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.ItemBlockBase;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
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
public class ItemBlockScreen extends ItemBlockBase
{
	public ItemBlockScreen(Block block)
	{
		super(block);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		Quest quest = nbt == null ? null : FTBQuests.PROXY.getQuestList(world.isRemote).getQuest(nbt.getShort("Quest"));

		if (quest == null || quest.invalid || quest.tasks.isEmpty())
		{
			return false;
		}

		int size = nbt.getByte("Size");

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

		BlockScreen.currentTask = quest.getTask(nbt.getByte("TaskIndex"));
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
		Quest quest = nbt == null ? null : ClientQuestFile.INSTANCE.getQuest(nbt.getShort("Quest"));

		if (quest == null || quest.invalid || quest.tasks.isEmpty())
		{
			tooltip.add(TextFormatting.RED + I18n.format("tile.ftbquests.screen.missing_data"));
			return;
		}

		int size = nbt.getByte("Size");
		String owner = nbt.getString("Owner");

		tooltip.add(I18n.format("tile.ftbquests.screen.size") + ": " + TextFormatting.GOLD.toString() + (1 + size * 2) + " x " + (1 + size * 2));
		tooltip.add(I18n.format("ftbquests.owner") + ": " + TextFormatting.DARK_GREEN + owner);
		tooltip.add(I18n.format("ftbquests.chapter") + ": " + StringUtils.color(quest.chapter.getDisplayName(), TextFormatting.YELLOW).getFormattedText());
		tooltip.add(I18n.format("ftbquests.quest") + ": " + StringUtils.color(quest.getDisplayName(), TextFormatting.YELLOW).getFormattedText());

		QuestTask task = quest.getTask(nbt.getByte("TaskIndex"));

		tooltip.add(I18n.format("ftbquests.task") + ": " + StringUtils.color(task.getDisplayName(), TextFormatting.YELLOW).getFormattedText());

		IProgressData data = ClientQuestFile.INSTANCE.getData(owner);

		if (data == null)
		{
			return;
		}

		long max = task.getMaxProgress();

		if (max <= 0)
		{
			tooltip.add(I18n.format("ftbquests.progress") + ": " + TextFormatting.BLUE + "0/0 [0%]");
		}
		else
		{
			long progress = data.getQuestTaskData(task.id).getProgress();

			if (progress >= max)
			{
				tooltip.add(I18n.format("ftbquests.progress") + ": " + TextFormatting.BLUE + max + "/" + max + " [100%]");
			}
			else
			{
				tooltip.add(I18n.format("ftbquests.progress") + ": " + TextFormatting.BLUE + progress + "/" + max + " [" + (int) (progress * 100D / (double) max) + "%]");
			}
		}
	}
}