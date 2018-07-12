package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class UnknownTask extends QuestTask
{
	public static final String ID = "unknown";

	private final NBTTagCompound nbt;
	private String hover;

	public UnknownTask(Quest parent, int id, NBTTagCompound n)
	{
		super(parent, id);
		nbt = n;
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
		NBTUtils.copyTags(nbt, n);
	}

	@Override
	public Icon getIcon()
	{
		return GuiIcons.CANCEL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return I18n.format("ftbquests.gui.task.unknown");
	}

	public String getHover()
	{
		if (hover == null)
		{
			hover = NBTUtils.getColoredNBTString(nbt);
		}

		return hover;
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