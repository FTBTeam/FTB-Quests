package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import com.feed_the_beast.ftbquests.tile.TileScreenPart;
import net.minecraft.client.renderer.texture.TextureMap;
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
	public int index;

	public QuestTask(Quest q)
	{
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
	public final String getID()
	{
		return quest.chapter.id + ':' + quest.id + ':' + id;
	}

	@Override
	public final long getProgress(IProgressData data)
	{
		return data.getQuestTaskData(this).getProgress();
	}

	@Override
	public final double getRelativeProgress(IProgressData data)
	{
		return data.getQuestTaskData(this).getRelativeProgress();
	}

	@Override
	public final boolean isComplete(IProgressData data)
	{
		long max = getMaxProgress();
		return max > 0L && getProgress(data) >= max;
	}

	@Override
	public long getMaxProgress()
	{
		return 1;
	}

	public String getMaxProgressString()
	{
		return Long.toString(getMaxProgress());
	}

	@Override
	public final void resetProgress(IProgressData data)
	{
		data.getQuestTaskData(this).resetProgress();
	}

	@Override
	public void deleteSelf()
	{
		super.deleteSelf();
		quest.tasks.remove(this);

		for (IProgressData data : quest.chapter.file.getAllData())
		{
			data.removeTask(this);
		}

		quest.chapter.file.refreshTaskList();
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

	public Class<? extends TileScreenCore> getScreenCoreClass()
	{
		return TileScreenCore.class;
	}

	public Class<? extends TileScreenPart> getScreenPartClass()
	{
		return TileScreenPart.class;
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
	public void renderOnScreen(@Nullable QuestTaskData data)
	{
		ClientUtils.MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		ClientUtils.MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		getIcon().draw3D(Icon.EMPTY);
		ClientUtils.MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		ClientUtils.MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
	}

	public boolean canInsertItem()
	{
		return false;
	}
}