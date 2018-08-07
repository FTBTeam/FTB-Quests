package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.events.universe.UniverseClosedEvent;
import com.feed_the_beast.ftblib.events.universe.UniverseLoadedEvent;
import com.feed_the_beast.ftblib.events.universe.UniverseSavedEvent;
import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.events.ModifyBaseFileLocationEvent;
import com.feed_the_beast.ftbquests.net.MessageSyncEditingMode;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
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
	}

	@SubscribeEvent
	public static void onUniversePostLoaded(UniverseLoadedEvent.Post event)
	{
		if (ServerQuestFile.INSTANCE != null)
		{
			ServerQuestFile.INSTANCE.unload();
		}

		ModifyBaseFileLocationEvent fileEvent = new ModifyBaseFileLocationEvent();
		fileEvent.post();
		File file = fileEvent.getFile();

		if (file == null)
		{
			file = new File(CommonUtils.folderMinecraft, "questpacks/default.nbt");
		}

		FTBQuests.LOGGER.info("Loading quests from " + file.getAbsolutePath());

		ServerQuestFile.INSTANCE = new ServerQuestFile(file);

		if (!ServerQuestFile.INSTANCE.load())
		{
			FTBQuests.LOGGER.error("Failed to load quests!");
		}

		NBTTagCompound nbt = event.getData(FTBQuests.MOD_ID);
		INSTANCE.editingMode.setBoolean(nbt.getBoolean("EditingMode"));
		INSTANCE.extraFiles.clear();

		NBTTagList list = nbt.getTagList("ExtraFiles", Constants.NBT.TAG_STRING);

		for (int i = 0; i < list.tagCount(); i++)
		{
			INSTANCE.extraFiles.add(list.getStringTagAt(i));
		}

		for (ForgeTeam team : event.getUniverse().getTeams())
		{
			FTBQuestsTeamData data = FTBQuestsTeamData.get(team);

			for (QuestChapter chapter : ServerQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (QuestTask task : quest.tasks)
					{
						data.createTaskData(task);
					}
				}
			}
		}
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
		nbt.setBoolean("EditingMode", INSTANCE.editingMode.getBoolean());

		NBTTagList list = new NBTTagList();

		for (String file : INSTANCE.extraFiles)
		{
			list.appendTag(new NBTTagString(file));
		}

		nbt.setTag("ExtraFiles", list);

		event.setData(FTBQuests.MOD_ID, nbt);
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
	public final ConfigBoolean editingMode;
	public final List<String> extraFiles;

	private FTBQuestsWorldData(Universe u)
	{
		universe = u;
		editingMode = new ConfigBoolean(false);
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