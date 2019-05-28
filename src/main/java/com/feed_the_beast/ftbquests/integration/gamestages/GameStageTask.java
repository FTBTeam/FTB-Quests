package com.feed_the_beast.ftbquests.integration.gamestages;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import com.feed_the_beast.ftbquests.quest.task.SimpleQuestTaskData;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class GameStageTask extends QuestTask
{
	public String stage = "";

	public GameStageTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.GAMESTAGE;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setString("stage", stage);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		stage = nbt.getString("stage");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(stage);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		stage = data.readString();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("stage", () -> stage, v -> stage = v, "").setDisplayName(new TextComponentTranslation("ftbquests.task.ftbquests.gamestage"));
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent text = new TextComponentString(stage);
		text.getStyle().setColor(TextFormatting.YELLOW);
		return new TextComponentTranslation("ftbquests.task.ftbquests.gamestage").appendText(": ").appendSibling(text);
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<GameStageTask>
	{
		private Data(GameStageTask task, ITeamData data)
		{
			super(task, data);
		}

		@Override
		public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
		{
			if (progress <= 0L && GameStageHelper.hasStage(player, task.stage))
			{
				if (!simulate)
				{
					progress = 1L;
					sync();
				}

				return true;
			}

			return false;
		}
	}
}