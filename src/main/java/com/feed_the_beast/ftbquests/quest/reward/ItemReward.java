package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.gui.WrappedIngredient;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.net.MessageDisplayItemRewardToast;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.latmod.mods.itemfilters.item.ItemMissing;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setTag("item", ItemMissing.write(item, false));

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
		item = ItemMissing.read(nbt.getTag("item"));
		randomBonus = nbt.getInteger("random_bonus");
		onlyOne = nbt.getBoolean("only_one");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeItemStack(item);
		data.writeVarInt(randomBonus);
		data.writeBoolean(onlyOne);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		item = data.readItemStack();
		randomBonus = data.readVarInt();
		onlyOne = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.add("item", new ConfigItemStack.SimpleStack(() -> item, v -> item = v), new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.item"));
		config.addInt("random_bonus", () -> randomBonus, v -> randomBonus = v, 0, 0, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.reward.random_bonus"));
		config.addBool("only_one", () -> onlyOne, v -> onlyOne = v, false);
	}

	@Override
	public void claim(EntityPlayerMP player, boolean notify)
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
	public ItemStack claimAutomated(TileEntity tileEntity, @Nullable EntityPlayerMP player)
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