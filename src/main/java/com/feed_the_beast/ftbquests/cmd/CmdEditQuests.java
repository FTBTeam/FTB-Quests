package com.feed_the_beast.ftbquests.cmd;

import com.feed_the_beast.ftblib.lib.cmd.CmdBase;
import com.feed_the_beast.ftblib.lib.cmd.CmdTreeBase;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.FinalIDObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestPosition;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class CmdEditQuests extends CmdTreeBase
{
	public CmdEditQuests()
	{
		super("edit_quests");
		addSubcommand(new CmdSync());
		addSubcommand(new CmdAddChapter());
		addSubcommand(new CmdAddQuest());
	}

	public static class CmdSync extends CmdBase
	{
		public CmdSync()
		{
			super("sync", Level.OP);
		}

		@Override
		public boolean isUsernameIndex(String[] args, int i)
		{
			return i == 0;
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
		{
			if (args.length == 1)
			{
				ServerQuestList.INSTANCE.sendTo(getPlayer(server, sender, args[0]));
			}
			else
			{
				ServerQuestList.INSTANCE.sendToAll();
			}
		}
	}

	public static class CmdAddChapter extends CmdBase
	{
		public CmdAddChapter()
		{
			super("add_chapter", Level.OP);
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
		{
			checkArgs(sender, args, 1);
			String s = new FinalIDObject(args[0]).getName();

			if (!ServerQuestList.INSTANCE.chapters.containsKey(s))
			{
				QuestChapter chapter = new QuestChapter(ServerQuestList.INSTANCE, s);
				ServerQuestList.INSTANCE.chapters.put(s, chapter);
				ServerQuestList.INSTANCE.save(chapter);
				ServerQuestList.INSTANCE.saveQuestsFile();
				sender.sendMessage(new TextComponentTranslation("commands.ftb.edit_quests.add_chapter.success"));
			}
			else
			{
				sender.sendMessage(new TextComponentTranslation("commands.ftb.edit_quests.add_chapter.fail_id"));
			}
		}
	}

	public static class CmdAddQuest extends CmdBase
	{
		public CmdAddQuest()
		{
			super("add_quest", Level.OP);
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
		{
			if (args.length == 0)
			{
				return getListOfStringsMatchingLastWord(args, ServerQuestList.INSTANCE.chapters.keySet());
			}

			return super.getTabCompletions(server, sender, args, pos);
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
		{
			checkArgs(sender, args, 4);

			QuestChapter chapter = ServerQuestList.INSTANCE.chapters.get(args[0]);

			if (chapter != null)
			{
				String s = new FinalIDObject(args[1]).getName();

				if (!s.equals("chapter") && !chapter.quests.containsKey(s))
				{
					int x = parseInt(args[2], 0);
					int y = parseInt(args[3], 0);

					for (Quest quest : chapter.quests.values())
					{
						if (quest.pos.x == x && quest.pos.y == y)
						{
							sender.sendMessage(new TextComponentTranslation("commands.ftb.edit_quests.add_quest.fail_xy"));
							return;
						}
					}

					Quest quest = new Quest(chapter, s);
					quest.pos = new QuestPosition(x, y);
					quest.icon = ItemIcon.getItemIcon(new ItemStack(Items.PAPER));
					chapter.quests.put(quest.id.getResourcePath(), quest);

					ServerQuestList.INSTANCE.save(chapter);
					ServerQuestList.INSTANCE.save(quest);
					sender.sendMessage(new TextComponentTranslation("commands.ftb.edit_quests.add_quest.success"));
				}
				else
				{
					sender.sendMessage(new TextComponentTranslation("commands.ftb.edit_quests.add_quest.fail_id"));
				}
			}
			else
			{
				sender.sendMessage(new TextComponentTranslation("commands.ftb.edit_quests.add_quest.fail_chapter"));
			}
		}
	}
}