package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.net.MessageDisplayItemRewardToast;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.widget.WrappedIngredient;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ItemReward extends Reward
{
	public ItemStack item;
	public int randomBonus;
	public boolean onlyOne;

	public ItemReward(Quest quest, ItemStack is)
	{
		super(quest);
		item = is;
		randomBonus = 0;
		onlyOne = false;
	}

	public ItemReward(Quest quest)
	{
		this(quest, new ItemStack(Items.APPLE));
	}

	@Override
	public RewardType getType()
	{
		return FTBQuestsRewards.ITEM;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		NBTUtils.write(nbt, "item", item);

		if (randomBonus > 0)
		{
			nbt.putInt("random_bonus", randomBonus);
		}

		if (onlyOne)
		{
			nbt.putBoolean("only_one", true);
		}
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		item = NBTUtils.read(nbt, "item");
		randomBonus = nbt.getInt("random_bonus");
		onlyOne = nbt.getBoolean("only_one");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeItemStack(item);
		buffer.writeVarInt(randomBonus);
		buffer.writeBoolean(onlyOne);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		item = buffer.readItemStack();
		randomBonus = buffer.readVarInt();
		onlyOne = buffer.readBoolean();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addItemStack("item", item, v -> item = v, ItemStack.EMPTY, false, false).setNameKey("ftbquests.reward.ftbquests.item");
		config.addInt("random_bonus", randomBonus, v -> randomBonus = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.reward.random_bonus");
		config.addBool("only_one", onlyOne, v -> onlyOne = v, false);
	}

	@Override
	public void claim(ServerPlayerEntity player, boolean notify)
	{
		if (onlyOne && player.inventory.hasItemStack(item))
		{
			return;
		}

		ItemStack stack1 = item.copy();
		stack1.grow(player.world.rand.nextInt(randomBonus + 1));
		ItemHandlerHelper.giveItemToPlayer(player, stack1);

		if (notify)
		{
			new MessageDisplayItemRewardToast(stack1).sendTo(player);
		}
	}

	@Override
	public ItemStack claimAutomated(TileEntity tileEntity, @Nullable ServerPlayerEntity player)
	{
		ItemStack stack1 = item.copy();
		stack1.grow(tileEntity.getWorld().rand.nextInt(randomBonus + 1));
		return stack1;
	}

	@Override
	public Icon getAltIcon()
	{
		if (item.isEmpty())
		{
			return super.getAltIcon();
		}

		return ItemIcon.getItemIcon(ItemHandlerHelper.copyStackWithSize(item, 1));
	}

	@Override
	public String getAltTitle()
	{
		return (item.getCount() > 1 ? (randomBonus > 0 ? (item.getCount() + "-" + (item.getCount() + randomBonus) + "x ") : (item.getCount() + "x ")) : "") + item.getDisplayName();
	}

	@Override
	public boolean addTitleInMouseOverText()
	{
		return !getTitle().equals(getAltTitle());
	}

	@Nullable
	@Override
	public Object getIngredient()
	{
		return new WrappedIngredient(item).tooltip();
	}

	@Override
	public String getButtonText()
	{
		if (randomBonus > 0)
		{
			return item.getCount() + "-" + (item.getCount() + randomBonus);
		}

		return Integer.toString(item.getCount());
	}
}