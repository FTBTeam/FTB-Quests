package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemMissing extends Item
{
	public static ItemStack getContainedStack(ItemStack stack)
	{
		return stack.hasTagCompound() ? ItemStackSerializer.read(stack.getTagCompound().getCompoundTag("item")) : ItemStack.EMPTY;
	}

	public static void setContainedStack(ItemStack stack, NBTTagCompound nbt)
	{
		stack.setTagInfo("item", nbt);
	}

	public ItemMissing()
	{
		setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		ItemStack stack1 = getContainedStack(stack);

		if (!stack1.isEmpty())
		{
			if (!world.isRemote)
			{
				ItemHandlerHelper.giveItemToPlayer(player, stack1);
			}

			stack.shrink(1);
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public boolean hasEffect(ItemStack stack)
	{
		return !getContainedStack(stack).isEmpty();
	}

	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		return EnumRarity.EPIC;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		if (!stack.hasTagCompound())
		{
			return;
		}

		NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("item");
		ItemStack stack1 = ItemStackSerializer.read(nbt);
		ResourceLocation name;
		int meta = 0, count = 1;

		if (!stack1.isEmpty())
		{
			name = stack1.getItem().getRegistryName();
			count = stack1.getCount();
			meta = 0;
		}
		else
		{
			if (nbt.hasKey("item", Constants.NBT.TAG_STRING))
			{
				String[] sa = nbt.getString("item").split(" ", 4);
				name = new ResourceLocation(sa[0]);

				if (sa.length >= 2)
				{
					count = MathHelper.getInt(sa[1], 1);
				}

				if (sa.length >= 3)
				{
					meta = (sa[2].charAt(0) == '*') ? OreDictionary.WILDCARD_VALUE : MathHelper.getInt(sa[2], 0);
				}
			}
			else
			{
				name = new ResourceLocation(nbt.getString("id"));
				count = nbt.getByte("Count");
				meta = nbt.getShort("Damage");
			}
		}

		StringBuilder out = new StringBuilder();

		if (count > 1)
		{
			out.append(TextFormatting.YELLOW);
			out.append(count);
			out.append(TextFormatting.DARK_GRAY);
			out.append('x');
		}

		out.append(TextFormatting.AQUA);
		out.append(name.getNamespace());
		out.append(TextFormatting.DARK_GRAY);
		out.append(':');
		out.append(TextFormatting.GOLD);
		out.append(name.getPath());

		if (meta > 0)
		{
			out.append(TextFormatting.DARK_GRAY);
			out.append('@');
			out.append(TextFormatting.GRAY);
			out.append(meta);
		}

		tooltip.add(out.toString());
	}
}