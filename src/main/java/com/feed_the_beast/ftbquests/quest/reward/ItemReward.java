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
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ItemReward extends Reward
{
	public ItemStack item;
	public int count;
	public int randomBonus;
	public boolean onlyOne;

	public ItemReward(Quest quest, ItemStack is)
	{
		super(quest);
		item = is;
		count = 1;
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

		if (count > 1)
		{
			nbt.setInteger("count", count);
		}

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
		count = nbt.getInteger("count");

		if (count == 0)
		{
			count = item.getCount();
			item.setCount(1);
		}

		randomBonus = nbt.getInteger("random_bonus");
		onlyOne = nbt.getBoolean("only_one");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeItemStack(item);
		data.writeVarInt(count);
		data.writeVarInt(randomBonus);
		data.writeBoolean(onlyOne);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		item = data.readItemStack();
		count = data.readVarInt();
		randomBonus = data.readVarInt();
		onlyOne = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.add("item", new ConfigItemStack.SimpleStack(true, () -> item, v -> item = v), new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.item"));
		config.addInt("count", () -> count, v -> count = v, 1, 1, 8192);
		config.addInt("random_bonus", () -> randomBonus, v -> randomBonus = v, 0, 0, 8192).setDisplayName(new TextComponentTranslation("ftbquests.reward.random_bonus"));
		config.addBool("only_one", () -> onlyOne, v -> onlyOne = v, false);
	}

	@Override
	public void claim(EntityPlayerMP player, boolean notify)
	{
		if (onlyOne && player.inventory.hasItemStack(item))
		{
			return;
		}

		int size = count + player.world.rand.nextInt(randomBonus + 1);

		while (size > 0)
		{
			int s = Math.min(size, item.getMaxStackSize());
			ItemHandlerHelper.giveItemToPlayer(player, ItemHandlerHelper.copyStackWithSize(item, s));
			size -= s;
		}

		if (notify)
		{
			new MessageDisplayItemRewardToast(item, size).sendTo(player);
		}
	}

	@Override
	public Optional<ItemStack> claimAutomated(TileEntity tileEntity, UUID playerId, @Nullable EntityPlayerMP player, boolean simulate)
	{
		if (count + randomBonus > item.getMaxStackSize())
		{
			return Optional.of(ItemStack.EMPTY);
		}

		Random random = new Random(longHashCode(
				playerId.getMostSignificantBits(),
				playerId.getLeastSignificantBits(),
				tileEntity.getPos().getX(),
				tileEntity.getPos().getY(),
				tileEntity.getPos().getZ(),
				tileEntity.getWorld().getTotalWorldTime()
		));

		return Optional.of(ItemHandlerHelper.copyStackWithSize(item, count + random.nextInt(randomBonus + 1)));
	}

	private static long longHashCode(Object... objects)
	{
		long result = 1L;

		for (Object element : objects)
		{
			result = 31L * result + (element == null ? 0L : element instanceof Number ? ((Number) element).longValue() : element.hashCode());
		}

		return result;
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
			return count + "-" + (count + randomBonus);
		}

		return Integer.toString(count);
	}
}