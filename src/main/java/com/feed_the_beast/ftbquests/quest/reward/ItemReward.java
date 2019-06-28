package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.gui.WrappedIngredient;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.net.MessageDisplayItemRewardToast;
import com.feed_the_beast.ftbquests.net.MessageDisplayRewardToast;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.latmod.mods.itemfilters.item.ItemMissing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ItemReward extends QuestReward
{
	public ItemStack stack;
	public int randomBonus;
	public boolean onlyOne;

	public ItemReward(Quest quest)
	{
		super(quest);
		stack = new ItemStack(Items.APPLE);
		randomBonus = 0;
		onlyOne = false;
	}

	@Override
	public QuestRewardType getType()
	{
		return FTBQuestsRewards.ITEM;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setTag("item", ItemMissing.write(stack, false));

		if (randomBonus > 0)
		{
			nbt.setInteger("random_bonus", randomBonus);
		}

		if (onlyOne)
		{
			nbt.setBoolean("only_one", true);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		stack = ItemMissing.read(nbt.getTag("item"));
		randomBonus = nbt.getInteger("random_bonus");
		onlyOne = nbt.getBoolean("only_one");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeItemStack(stack);
		data.writeVarInt(randomBonus);
		data.writeBoolean(onlyOne);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		stack = data.readItemStack();
		randomBonus = data.readVarInt();
		onlyOne = data.readBoolean();
	}

	@Override
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.add("item", new ConfigItemStack.SimpleStack(() -> stack, v -> stack = v), new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.item"));
		config.addInt("random_bonus", () -> randomBonus, v -> randomBonus = v, 0, 0, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.reward.random_bonus"));
		config.addBool("only_one", () -> onlyOne, v -> onlyOne = v, false);
	}

	@Override
	public void claim(EntityPlayerMP player)
	{
		if (onlyOne && player.inventory.hasItemStack(stack))
		{
			return;
		}

		ItemStack stack1 = stack.copy();
		stack1.grow(player.world.rand.nextInt(randomBonus + 1));
		ItemHandlerHelper.giveItemToPlayer(player, stack1);

		if (MessageDisplayRewardToast.ENABLED)
		{
			new MessageDisplayItemRewardToast(stack1).sendTo(player);
		}
	}

	@Override
	public ItemStack claimAutomated(TileEntity tileEntity, @Nullable EntityPlayerMP player)
	{
		ItemStack stack1 = stack.copy();
		stack1.grow(tileEntity.getWorld().rand.nextInt(randomBonus + 1));
		return stack1;
	}

	@Override
	public Icon getAltIcon()
	{
		if (stack.isEmpty())
		{
			return super.getAltIcon();
		}

		return ItemIcon.getItemIcon(ItemHandlerHelper.copyStackWithSize(stack, 1));
	}

	@Override
	public String getAltTitle()
	{
		return (stack.getCount() > 1 ? (randomBonus > 0 ? (stack.getCount() + "-" + (stack.getCount() + randomBonus) + "x ") : (stack.getCount() + "x ")) : "") + stack.getDisplayName();
	}

	@Override
	public boolean addTitleInMouseOverText()
	{
		return false;
	}

	@Nullable
	@Override
	public Object getIngredient()
	{
		return new WrappedIngredient(stack).tooltip();
	}

	@Override
	public String getButtonText()
	{
		if (randomBonus > 0)
		{
			return stack.getCount() + "-" + (stack.getCount() + randomBonus);
		}

		return Integer.toString(stack.getCount());
	}
}