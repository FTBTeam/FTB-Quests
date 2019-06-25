package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.events.universe.UniverseClosedEvent;
import com.feed_the_beast.ftblib.events.universe.UniverseLoadedEvent;
import com.feed_the_beast.ftblib.events.universe.UniverseSavedEvent;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageSyncEditingMode;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import io.sommers.packmode.api.PackModeAPI;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
public class FTBQuestsWorldData implements IConfigCallback
{
	public static FTBQuestsWorldData INSTANCE;

	@SubscribeEvent
	public static void onUniversePreLoaded(UniverseLoadedEvent.Pre event)
	{
		INSTANCE = new FTBQuestsWorldData(event.getUniverse());

		if (ServerQuestFile.INSTANCE != null)
		{
			ServerQuestFile.INSTANCE.unload();
		}

		ServerQuestFile.INSTANCE = new ServerQuestFile(event.getUniverse());
		ServerQuestFile.INSTANCE.load();

		int c = ServerQuestFile.INSTANCE.chapters.size();
		int q = 0;
		int t = 0;
		int r = 0;

		for (QuestChapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			q += chapter.quests.size();

			for (Quest quest : chapter.quests)
			{
				t += quest.tasks.size();
				r += quest.rewards.size();
			}
		}

		FTBQuests.LOGGER.info(String.format("Loaded %d chapters, %d quests, %d tasks and %d rewards. In total, %d objects", c, q, t, r, ServerQuestFile.INSTANCE.getAllObjects().size()));

		NBTTagCompound nbt = event.getData(FTBQuests.MOD_ID);
		INSTANCE.extraFiles.clear();

		NBTTagList list = nbt.getTagList("ExtraFiles", Constants.NBT.TAG_STRING);

		for (int i = 0; i < list.tagCount(); i++)
		{
			INSTANCE.extraFiles.add(list.getStringTagAt(i));
		}

		if (ServerQuestFile.INSTANCE.fileVersion != ServerQuestFile.VERSION)
		{
			FTBQuests.LOGGER.info("File version changed " + ServerQuestFile.INSTANCE.fileVersion + " -> " + ServerQuestFile.VERSION);
			ServerQuestFile.INSTANCE.saveNow();
		}
	}

	private static String getFolderName()
	{
		if (Loader.isModLoaded("packmode"))
		{
			return getPackmodeFolderName();
		}

		return "normal";
	}

	private static String getPackmodeFolderName()
	{
		return PackModeAPI.getInstance().getCurrentPackMode();
	}

	@SubscribeEvent
	public static void onUniverseSaved(UniverseSavedEvent event)
	{
		if (ServerQuestFile.INSTANCE != null && ServerQuestFile.INSTANCE.shouldSave)
		{
			ServerQuestFile.INSTANCE.saveNow();
			ServerQuestFile.INSTANCE.shouldSave = false;
		}

		NBTTagCompound nbt = new NBTTagCompound();

		if (!INSTANCE.extraFiles.isEmpty())
		{
			NBTTagList list = new NBTTagList();

			for (String file : INSTANCE.extraFiles)
			{
				list.appendTag(new NBTTagString(file));
			}

			nbt.setTag("ExtraFiles", list);
		}

		if (!nbt.isEmpty())
		{
			event.setData(FTBQuests.MOD_ID, nbt);
		}
	}

	@SubscribeEvent
	public static void onUniverseClosed(UniverseClosedEvent event)
	{
		if (ServerQuestFile.INSTANCE != null)
		{
			ServerQuestFile.INSTANCE.unload();
			ServerQuestFile.INSTANCE = null;
		}

		INSTANCE = null;
	}

	public final Universe universe;
	public final List<String> extraFiles;

	private FTBQuestsWorldData(Universe u)
	{
		universe = u;
		extraFiles = new ArrayList<>();
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		for (EntityPlayerMP player : universe.server.getPlayerList().getPlayers())
		{
			new MessageSyncEditingMode(FTBQuests.canEdit(player)).sendTo(player);
		}
	}
}