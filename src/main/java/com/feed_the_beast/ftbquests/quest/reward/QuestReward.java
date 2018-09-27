package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestReward
{
	public final Quest quest;
	public final int uid;

	public String title = "";
	public ItemStack icon = ItemStack.EMPTY;
	public boolean team = false;
	public boolean emergency = false;

	private Icon cachedIcon = null;

	public QuestReward(Quest q, int id)
	{
		quest = q;
		uid = id;
	}

	public abstract void writeData(NBTTagCompound nbt);

	public abstract void getConfig(ConfigGroup config);

	public abstract void claim(EntityPlayer player);

	public final String toString()
	{
		return String.format("%s#%08x", quest.getID(), uid);
	}

	public final boolean equals(Object object)
	{
		return object == this || object != null && uid == object.hashCode();
	}

	public final int hashCode()
	{
		return uid;
	}

	public final void getExtraConfig(ConfigGroup config)
	{
		config.addString("title", () -> title, v -> title = v, "").setDisplayName(new TextComponentTranslation("ftbquests.title")).setOrder((byte) -127);
		config.add("icon", new ConfigItemStack.SimpleStack(() -> icon, v -> icon = v), new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.icon")).setOrder((byte) -126);
	}

	public final void writeCommonData(NBTTagCompound nbt)
	{
		if (team)
		{
			nbt.setBoolean("team_reward", true);
		}

		if (emergency)
		{
			nbt.setBoolean("emergency", true);
		}
	}

	public final void readCommonData(NBTTagCompound nbt)
	{
		team = nbt.getBoolean("team_reward");
		emergency = nbt.getBoolean("emergency");
	}

	public final Icon getIcon()
	{
		if (cachedIcon == null)
		{
			if (!icon.isEmpty())
			{
				cachedIcon = ItemIcon.getItemIcon(icon);
			}
			else
			{
				cachedIcon = getAltIcon();
			}
		}

		return cachedIcon;
	}

	public final ITextComponent getDisplayName()
	{
		if (!title.isEmpty())
		{
			return new TextComponentString(title.equals("-") ? "" : title);
		}

		return getAltDisplayName();
	}

	public Icon getAltIcon()
	{
		return GuiIcons.MONEY_BAG;
	}

	public ITextComponent getAltDisplayName()
	{
		return QuestRewardType.getType(getClass()).getDisplayName();
	}

	public void clearCachedData()
	{
		cachedIcon = null;
	}

	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list)
	{
	}
}