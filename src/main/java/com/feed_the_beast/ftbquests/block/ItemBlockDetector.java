package com.feed_the_beast.ftbquests.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ItemBlockDetector extends ItemBlock
{
	public ItemBlockDetector(Block block)
	{
		super(block);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getTranslationKey(ItemStack stack)
	{
		return super.getTranslationKey(stack) + "." + BlockDetector.Variant.VALUES[stack.getMetadata()].getName();
	}
}