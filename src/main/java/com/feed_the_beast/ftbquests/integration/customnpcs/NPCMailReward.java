package com.feed_the_beast.ftbquests.integration.customnpcs;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerMail;

/**
 * @author LatvianModder
 */
public class NPCMailReward extends Reward
{
	public final PlayerMail mail = new PlayerMail();

	public NPCMailReward(Quest quest)
	{
		super(quest);
		mail.sender = "Unknown Sender";
		mail.subject = "Unknown Subject";
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagString("No text!"));
		mail.message.setTag("pages", list);
	}

	@Override
	public RewardType getType()
	{
		return CustomNPCsIntegration.MAIL_REWARD;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setTag("mail", mail.writeNBT());
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		mail.readNBT(nbt.getCompoundTag("mail"));
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeNBT(mail.writeNBT());
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		mail.readNBT(data.readNBT());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("subject", () -> mail.subject, v -> mail.subject = v, "");
		config.addString("sender", () -> mail.sender, v -> mail.sender = v, "");

		config.add("message", new ConfigList<ConfigString>(new ConfigString(""))
		{
			@Override
			public void readFromList()
			{
				NBTTagList pages = new NBTTagList();

				for (ConfigString string : list)
				{
					pages.appendTag(new NBTTagString(string.getString()));
				}

				mail.message.setTag("pages", pages);
			}

			@Override
			public void writeToList()
			{
				list.clear();
				NBTTagList pages = mail.message.getTagList("pages", Constants.NBT.TAG_STRING);

				for (int i = 0; i < pages.tagCount(); i++)
				{
					list.add(new ConfigString(pages.getStringTagAt(i)));
				}
			}
		}, new ConfigList<>(new ConfigString("")));

		config.addInt("quest_id", () -> mail.questId, v -> mail.questId = v, -1, -1, Integer.MAX_VALUE);
		config.addString("quest_title", () -> mail.questTitle, v -> mail.questTitle = v, "");
		config.addList("items", mail.items, new ConfigItemStack(ItemStack.EMPTY), ConfigItemStack::new, ConfigItemStack::getStack);
	}

	@Override
	public void claim(EntityPlayerMP player, boolean notify)
	{
		if (mail.isValid())
		{
			PlayerDataController.instance.addPlayerMessage(player.server, player.getName(), mail.copy());
		}
	}
}