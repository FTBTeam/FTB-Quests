package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.ItemBlockBase;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
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
		if (!ClientQuestFile.existsWithTeam())
		{
			return;
		}

		QuestTaskData data = QuestBlockData.get(stack).getTaskData();

		if (data == null)
		{
			tooltip.add(TextFormatting.RED + I18n.format("tile.ftbquests.quest_block.missing_data"));
			return;
		}

		if (!ClientQuestFile.INSTANCE.teamId.equals(data.data.getTeamID()))
		{
			tooltip.add(I18n.format("ftbquests.owner") + ": " + TextFormatting.DARK_GREEN + data.data.getTeamID());
		}

		tooltip.add(I18n.format("ftbquests.task") + ": " + TextFormatting.YELLOW + data.task.getDisplayName().getFormattedText());
		int max = data.task.getMaxProgress();

		if (max <= 0)
		{
			tooltip.add(I18n.format("ftbquests.progress") + ": " + TextFormatting.BLUE + "0/0 [0%]");
		}
		else
		{
			int progress = data.getProgress();

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