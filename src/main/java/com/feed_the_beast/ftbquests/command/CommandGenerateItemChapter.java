package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftblib.lib.config.EnumTristate;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObjectResponse;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;

/**
 * @author LatvianModder
 */
public class CommandGenerateItemChapter extends CommandFTBQuestsBase
{
	@Override
	public String getName()
	{
		return "generate_chapter_with_all_items_in_game";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
	{
		NonNullList<ItemStack> list = NonNullList.create();

		for (Item item : Item.REGISTRY)
		{
			try
			{
				item.getSubItems(CreativeTabs.SEARCH, list);
			}
			catch (Throwable ex)
			{
				FTBQuests.LOGGER.warn("Failed to get items from " + item.getRegistryName() + ": " + ex);
			}
		}

		Chapter chapter = new Chapter(ServerQuestFile.INSTANCE);
		chapter.id = chapter.file.newID();
		chapter.onCreated();

		chapter.title = "Generated chapter of all items in search creative tab [" + list.size() + "]";
		chapter.icon = new ItemStack(Items.COMPASS);

		new MessageCreateObjectResponse(chapter, null).sendToAll();

		FTBQuests.LOGGER.info("Found " + list.size() + " items in total, chapter ID: " + chapter);

		for (int i = 0; i < list.size(); i++)
		{
			Quest quest = new Quest(chapter);
			quest.id = chapter.file.newID();
			quest.onCreated();
			quest.x = i % 40;
			quest.y = i / 40;
			quest.subtitle = list.get(i).serializeNBT().toString();

			new MessageCreateObjectResponse(quest, null).sendToAll();

			ItemTask task = new ItemTask(quest);
			task.id = chapter.file.newID();
			task.onCreated();

			task.consumeItems = EnumTristate.TRUE;
			task.items.add(list.get(i));

			NBTTagCompound extra = new NBTTagCompound();
			extra.setString("type", task.getType().getTypeForNBT());
			new MessageCreateObjectResponse(task, extra).sendToAll();
		}

		ServerQuestFile.INSTANCE.save();
		ServerQuestFile.INSTANCE.saveNow();
	}
}
