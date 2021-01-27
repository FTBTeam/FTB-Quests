package com.feed_the_beast.ftbquests.net.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FTBQuestsNetHandlerImpl
{
	public static void writeItemType(FriendlyByteBuf buffer, ItemStack stack)
	{
		if (stack.isEmpty())
		{
			buffer.writeVarInt(-1);
		}
		else
		{
			buffer.writeVarInt(Item.getId(stack.getItem()));
			buffer.writeNbt(stack.getTag());
			buffer.writeNbt((CompoundTag) stack.save(new CompoundTag()).get("ForgeCaps"));
		}
	}

	public static ItemStack readItemType(FriendlyByteBuf buffer)
	{
		int id = buffer.readVarInt();

		if (id == -1)
		{
			return ItemStack.EMPTY;
		}
		else
		{
			CompoundTag tag = buffer.readNbt();
			CompoundTag caps = buffer.readNbt();
			ItemStack item = new ItemStack(Item.byId(id), 1, caps);
			item.setTag(tag);
			return item;
		}
	}
}
