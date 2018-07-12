package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.ItemBlockBase;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemBlockQuest extends ItemBlockBase
{
	public ItemBlockQuest(Block block)
	{
		super(block);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
	{
		return new QuestBlockData(null);
	}

	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack)
	{
		return QuestBlockData.get(stack).serializeNBT();
	}

	@Override
	public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt)
	{
		if (nbt != null)
		{
			QuestBlockData.get(stack).deserializeNBT(nbt);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		if (!ClientQuestList.exists())
		{
			return;
		}

		QuestBlockData blockData = QuestBlockData.get(stack);

		if (!blockData.getOwnerTeam().isEmpty())
		{
			tooltip.add(I18n.format("tile.ftbquests.quest_block.tooltip.owner") + ": " + (ClientQuestList.INSTANCE.teamId.equals(blockData.getOwnerTeam()) ? TextFormatting.DARK_GREEN : TextFormatting.RED) + blockData.getOwnerTeam());
		}

		QuestTaskData data = blockData.getTaskData();

		if (data == null)
		{
			return;
		}

		tooltip.add(I18n.format("tile.ftbquests.quest_block.tooltip.task") + ": " + TextFormatting.YELLOW + data.task.getDisplayName());
		int max = data.task.getMaxProgress();

		if (max <= 0)
		{
			tooltip.add(I18n.format("tile.ftbquests.quest_block.tooltip.progress") + ": " + TextFormatting.BLUE + "0/0 [0%]");
		}
		else
		{
			int progress = data.getProgress();

			if (progress >= max)
			{
				tooltip.add(I18n.format("tile.ftbquests.quest_block.tooltip.progress") + ": " + TextFormatting.BLUE + max + "/" + max + " [100%]");
			}
			else
			{
				tooltip.add(I18n.format("tile.ftbquests.quest_block.tooltip.progress") + ": " + TextFormatting.BLUE + progress + "/" + max + " [" + (int) (progress * 100D / (double) max) + "%]");
			}
		}
	}
}