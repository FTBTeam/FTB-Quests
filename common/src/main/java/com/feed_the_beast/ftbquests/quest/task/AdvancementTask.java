package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class AdvancementTask extends Task
{
	public String advancement = "";
	public String criterion = "";

	private Component advTitle = TextComponent.EMPTY;
	private ItemStack advIcon = ItemStack.EMPTY;

	public AdvancementTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return TaskTypes.ADVANCEMENT;
	}

	@Override
	public void writeData(CompoundTag nbt)
	{
		super.writeData(nbt);
		nbt.putString("advancement", advancement);
		nbt.putString("criterion", criterion);
	}

	@Override
	public void readData(CompoundTag nbt)
	{
		super.readData(nbt);
		advancement = nbt.getString("advancement");
		criterion = nbt.getString("criterion");
		advTitle = TextComponent.EMPTY;
		advIcon = ItemStack.EMPTY;

		try
		{
			if (!advancement.isEmpty() && getQuestFile() == ServerQuestFile.INSTANCE)
			{
				Advancement a = ServerQuestFile.INSTANCE.server.getAdvancements().getAdvancement(new ResourceLocation(advancement));

				if (a != null && a.getDisplay() != null)
				{
					advTitle = a.getDisplay().getTitle().copy();
					advIcon = a.getDisplay().getIcon().copy();
				}
			}
		}
		catch (Exception ex)
		{
			FTBQuests.LOGGER.warn("Failed to load advancement '" + advancement + "' task icon and title: " + ex);
		}
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer)
	{
		super.writeNetData(buffer);
		buffer.writeUtf(advancement, Short.MAX_VALUE);
		buffer.writeUtf(criterion, Short.MAX_VALUE);
		buffer.writeComponent(advTitle);
		FTBQuestsNetHandler.writeItemType(buffer, advIcon);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer)
	{
		super.readNetData(buffer);
		advancement = buffer.readUtf(Short.MAX_VALUE);
		criterion = buffer.readUtf(Short.MAX_VALUE);
		advTitle = buffer.readComponent();
		advIcon = FTBQuestsNetHandler.readItemType(buffer);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("advancement", advancement, v -> advancement = v, "").setNameKey("ftbquests.task.ftbquests.advancement");
		config.addString("criterion", criterion, v -> criterion = v, "");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle()
	{
		if (advTitle != TextComponent.EMPTY)
		{
			return new TranslatableComponent("ftbquests.task.ftbquests.advancement").append(": ").append(new TextComponent("").append(advTitle).withStyle(ChatFormatting.YELLOW));
		}

		return super.getAltTitle();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon()
	{
		return advIcon.isEmpty() ? super.getAltIcon() : ItemIcon.getItemIcon(advIcon);
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
		public boolean canSubmit(ServerPlayer player)
		{
			if (task.advancement.isEmpty())
			{
				return false;
			}

			Advancement a = player.server.getAdvancements().getAdvancement(new ResourceLocation(task.advancement));

			if (a == null)
			{
				return false;
			}

			AdvancementProgress progress = player.getAdvancements().getOrStartProgress(a);

			if (task.criterion.isEmpty())
			{
				return progress.isDone();
			}
			else
			{
				CriterionProgress criterionProgress = progress.getCriterion(task.criterion);
				return criterionProgress != null && criterionProgress.isDone();
			}
		}
	}
}