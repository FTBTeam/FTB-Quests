package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class AdvancementTask extends QuestTask
{
	public String advancement = "";

	public AdvancementTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.ADVANCEMENT;
	}

	@Override
	public long getMaxProgress()
	{
		return 1L;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setString("advancement", advancement);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		advancement = nbt.getString("advancement");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(advancement);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		advancement = data.readString();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("advancement", () -> advancement, v -> advancement = v, "").setDisplayName(new TextComponentTranslation("ftbquests.task.ftbquests.advancement"));
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		Advancement a = FTBQuests.PROXY.getAdvancement(null, advancement);
		return a == null || a.getDisplay() == null ? super.getAltDisplayName() : new TextComponentTranslation("ftbquests.task.ftbquests.advancement.title", a.getDisplay().getTitle().createCopy());
	}

	@Override
	public Icon getAltIcon()
	{
		Advancement a = FTBQuests.PROXY.getAdvancement(null, advancement);
		return a == null || a.getDisplay() == null ? super.getAltIcon() : ItemIcon.getItemIcon(a.getDisplay().getIcon());
	}

	@Override
	public boolean consumesResources()
	{
		return true;
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<AdvancementTask>
	{
		private Data(AdvancementTask task, ITeamData data)
		{
			super(task, data);
		}

		@Override
		public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
		{
			if (progress >= 1L || task.advancement.isEmpty())
			{
				return false;
			}

			Advancement a = FTBQuests.PROXY.getAdvancement(player.server, task.advancement);

			if (a != null && player.getAdvancements().getProgress(a).isDone())
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