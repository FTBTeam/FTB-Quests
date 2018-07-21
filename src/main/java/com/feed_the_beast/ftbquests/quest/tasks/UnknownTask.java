package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class UnknownTask extends QuestTask
{
	public static final String ID = "unknown";

	private NBTTagCompound nbt;
	private String hover;

	public UnknownTask(Quest parent, NBTTagCompound n)
	{
		super(parent, n);
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
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("ftbquests.gui.task.unknown");
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
	public void getConfig(ConfigGroup group)
	{
		group.add(FTBQuests.MOD_ID, "nbt", new ConfigString()
		{
			@Override
			public String getString()
			{
				return nbt.toString();
			}

			@Override
			public void setString(String v)
			{
				try
				{
					nbt = JsonToNBT.getTagFromJson(v);
				}
				catch (NBTException e)
				{
					e.printStackTrace();
				}
			}
		});
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