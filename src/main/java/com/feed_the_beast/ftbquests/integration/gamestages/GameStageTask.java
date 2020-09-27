package com.feed_the_beast.ftbquests.integration.gamestages;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.task.BooleanTaskData;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public class GameStageTask extends Task
{
	public String stage = "";

	public GameStageTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return GameStagesIntegration.GAMESTAGE_TASK;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("stage", stage);
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		stage = nbt.getString("stage");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeString(stage);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		stage = buffer.readString();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("stage", stage, v -> stage = v, "").setNameKey("ftbquests.task.ftbquests.gamestage");
	}

	@Override
	public IFormattableTextComponent getAltTitle()
	{
		return new TranslationTextComponent("ftbquests.task.ftbquests.gamestage").appendString(": ").append(new StringTextComponent(stage).mergeStyle(TextFormatting.YELLOW));
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<GameStageTask>
	{
		private Data(GameStageTask task, PlayerData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(ServerPlayerEntity player)
		{
			return GameStageHelper.hasStage(player, task.stage);
		}
	}
}