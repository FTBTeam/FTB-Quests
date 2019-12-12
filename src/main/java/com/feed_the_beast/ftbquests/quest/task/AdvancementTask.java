package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public class AdvancementTask extends Task
{
	public String advancement = "";
	public String criterion = "";

	public AdvancementTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.ADVANCEMENT;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("advancement", advancement);
		nbt.putString("criterion", criterion);
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		advancement = nbt.getString("advancement");
		criterion = nbt.getString("criterion");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeString(advancement);
		buffer.writeString(criterion);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		advancement = buffer.readString();
		criterion = buffer.readString();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("advancement", advancement, v -> advancement = v, "").setNameKey("ftbquests.task.ftbquests.advancement");
		config.addString("criterion", criterion, v -> criterion = v, "");
	}

	@Override
	public String getAltTitle()
	{
		Advancement a = Minecraft.getInstance().player.connection.getAdvancementManager().getAdvancementList().getAdvancement(new ResourceLocation(advancement));

		if (a != null && a.getDisplay() != null)
		{
			return I18n.format("ftbquests.task.ftbquests.advancement") + ": " + TextFormatting.YELLOW + a.getDisplay().getTitle().getFormattedText();
		}

		return super.getAltTitle();
	}

	@Override
	public Icon getAltIcon()
	{
		Advancement a = Minecraft.getInstance().player.connection.getAdvancementManager().getAdvancementList().getAdvancement(new ResourceLocation(advancement));
		return a == null || a.getDisplay() == null ? super.getAltIcon() : ItemIcon.getItemIcon(a.getDisplay().getIcon());
	}

	@Override
	public int autoSubmitOnPlayerTick()
	{
		return 5;
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<AdvancementTask>
	{
		private Data(AdvancementTask task, PlayerData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(ServerPlayerEntity player)
		{
			if (task.advancement.isEmpty())
			{
				return false;
			}

			Advancement a = player.server.getAdvancementManager().getAdvancement(new ResourceLocation(task.advancement));

			if (a == null)
			{
				return false;
			}

			AdvancementProgress progress = player.getAdvancements().getProgress(a);

			if (task.criterion.isEmpty())
			{
				return progress.isDone();
			}
			else
			{
				CriterionProgress criterionProgress = progress.getCriterionProgress(task.criterion);
				return criterionProgress != null && criterionProgress.isObtained();
			}
		}
	}
}