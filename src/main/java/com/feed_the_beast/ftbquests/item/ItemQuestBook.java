package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemQuestBook extends Item
{
	public ItemQuestBook()
	{
		super(new Properties().maxStackSize(1).group(FTBQuests.ITEM_GROUP));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		if (world.isRemote)
		{
			openGui();
		}

		return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
	}

	private void openGui()
	{
		ClientQuestFile.INSTANCE.openQuestGui();
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		return stack.copy();
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.disableGui && !ClientQuestFile.INSTANCE.self.getCanEdit())
		{
			tooltip.add(new TranslationTextComponent("item.ftbquests.book.disabled").mergeStyle(TextFormatting.RED));
		}
		else
		{
			tooltip.add(new TranslationTextComponent("item.ftbquests.book.tooltip").mergeStyle(TextFormatting.GRAY));
		}
	}
}