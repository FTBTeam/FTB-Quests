package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.block.TileScreenCore;
import com.feed_the_beast.ftbquests.block.TileScreenPart;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class QuestTask extends ProgressingQuestObject implements IStringSerializable
{
	public final Quest quest;

	public QuestTask(Quest q, NBTTagCompound nbt)
	{
		super(q.chapter.file.getID(nbt));
		quest = q;
	}

	public abstract QuestTaskData createData(IProgressData data);

	@Override
	public final QuestFile getQuestFile()
	{
		return quest.chapter.file;
	}

	@Override
	public final QuestObjectType getObjectType()
	{
		return QuestObjectType.TASK;
	}

	@Override
	public final int getProgress(IProgressData data)
	{
		return data.getQuestTaskData(id).getProgress();
	}

	@Override
	public final double getRelativeProgress(IProgressData data)
	{
		return data.getQuestTaskData(id).getRelativeProgress();
	}

	@Override
	public int getMaxProgress()
	{
		return 1;
	}

	@Override
	public final void resetProgress(IProgressData data)
	{
		data.getQuestTaskData(id).resetProgress();
	}

	@Override
	public void deleteSelf()
	{
		super.deleteSelf();
		quest.tasks.remove(this);

		for (IProgressData data : quest.chapter.file.getAllData())
		{
			data.removeTask(id);
		}
	}

	@Override
	public void deleteChildren()
	{
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("ftbquests.task." + getName());
	}

	public TileScreenCore createScreenCore(World world)
	{
		return new TileScreenCore();
	}

	public TileScreenPart createScreenPart(World world)
	{
		return new TileScreenPart();
	}

	@SideOnly(Side.CLIENT)
	public void renderOnScreen(World world, @Nullable QuestTaskData data)
	{
		getIcon().draw3D(world, Icon.EMPTY);
	}
}