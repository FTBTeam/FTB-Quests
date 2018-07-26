package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNBT;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class UnknownTask extends QuestTask
{
	public static final String ID = "unknown";

	private ConfigNBT nbt;
	private String hover;

	public UnknownTask(Quest parent, NBTTagCompound n)
	{
		super(parent, n);
		nbt = new ConfigNBT(n);
	}

	@Override
	public boolean isInvalid()
	{
		return true;
	}

	@Override
	public int getMaxProgress()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound n)
	{
		NBTUtils.copyTags(nbt.getNBT(), n);
	}

	@Override
	public Icon getIcon()
	{
		return GuiIcons.BARRIER;
	}

	public String getHover()
	{
		if (hover == null)
		{
			hover = NBTUtils.getColoredNBTString(nbt.getNBT());
		}

		return hover;
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("nbt", nbt, new ConfigNBT(new NBTTagCompound()));
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	public static class Data extends QuestTaskData<UnknownTask>
	{
		private Data(UnknownTask t, IProgressData data)
		{
			super(t, data);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			return false;
		}

		@Nullable
		@Override
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			return null;
		}
	}
}