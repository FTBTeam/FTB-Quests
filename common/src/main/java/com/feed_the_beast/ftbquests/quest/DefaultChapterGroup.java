package com.feed_the_beast.ftbquests.quest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class DefaultChapterGroup extends ChapterGroup
{
	public DefaultChapterGroup(QuestFile f)
	{
		super(f, "");
	}

	@Override
	public String toString()
	{
		return "-";
	}

	@Override
	public String getTitle()
	{
		return file.title;
	}

	@Override
	public ItemStack getIconItem()
	{
		return file.icon;
	}

	@Override
	public void read(CompoundTag tag)
	{
	}

	@Override
	public void write(CompoundTag tag)
	{
	}
}
