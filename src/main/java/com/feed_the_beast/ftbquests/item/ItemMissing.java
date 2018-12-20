package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
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
	public ItemMissing()
	{
		setMaxStackSize(1);
	}

	private static ItemStack getContainedStack(ItemStack stack)
	{
		return stack.hasTagCompound() ? ItemStackSerializer.read(stack.getTagCompound().getTag("item")) : ItemStack.EMPTY;
	}

	public static ItemStack read(@Nullable NBTBase nbt)
	{
		if (nbt == null || nbt.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		ItemStack stack = ItemStackSerializer.read(nbt);

		if (stack.getItem() == FTBQuestsItems.MISSING)
		{
			ItemStack stack1 = getContainedStack(stack);

			if (!stack1.isEmpty())
			{
				return stack1;
			}
		}
		else if (stack.isEmpty())
		{
			ItemStack stack1 = new ItemStack(FTBQuestsItems.MISSING);
			stack1.setTagInfo("item", nbt.copy());
			return stack1;
		}

		return stack;
	}

	public static NBTBase write(ItemStack stack, boolean forceCompound)
	{
		if (stack.getItem() == FTBQuestsItems.MISSING)
		{
			NBTBase base = stack.hasTagCompound() ? stack.getTagCompound().getTag("item") : null;

			if (forceCompound)
			{
				NBTTagCompound nbt = new NBTTagCompound();

				if (base != null && !base.isEmpty())
				{
					nbt.setTag("item", base);
				}

				return nbt;
			}

			return base == null || base.isEmpty() ? new NBTTagString("") : base;
		}

		return ItemStackSerializer.write(stack, forceCompound);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected)
	{
		if (!(entity instanceof EntityPlayer) || world.getTotalWorldTime() % 100L != 65L)
		{
			return;
		}

		ItemStack stack1 = getContainedStack(stack);

		if (!stack1.isEmpty())
		{
			if (!world.isRemote)
			{
				ItemHandlerHelper.giveItemToPlayer((EntityPlayer) entity, stack1, slot);
			}

			stack.shrink(1);
		}
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
	public String getItemStackDisplayName(ItemStack stack)
	{
		if (!stack.hasTagCompound())
		{
			return super.getItemStackDisplayName(stack);
		}

		NBTBase nbt = stack.getTagCompound().getTag("item");

		if (nbt == null || nbt.isEmpty())
		{
			return super.getItemStackDisplayName(stack);
		}

		ItemStack stack1 = ItemStackSerializer.read(nbt);
		ResourceLocation name;
		int meta = 0, count = 1;

		if (!stack1.isEmpty())
		{
			name = stack1.getItem().getRegistryName();
			count = stack1.getCount();
			meta = stack1.getMetadata();
		}
		else
		{
			NBTTagCompound nbt1;

			if (nbt instanceof NBTTagString)
			{
				nbt1 = new NBTTagCompound();
				nbt1.setTag("item", nbt);
			}
			else
			{
				nbt1 = (NBTTagCompound) nbt;
			}

			if (nbt1.hasKey("item", Constants.NBT.TAG_STRING))
			{
				String[] sa = nbt1.getString("item").split(" ", 4);
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
				name = new ResourceLocation(nbt1.getString("id"));
				count = nbt1.getByte("Count");
				meta = nbt1.getShort("Damage");
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

		return out.toString();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		if (stack.hasTagCompound() && stack.getTagCompound().getTag("item") != null)
		{
			tooltip.add(TextFormatting.LIGHT_PURPLE + super.getItemStackDisplayName(stack));
		}
	}
}