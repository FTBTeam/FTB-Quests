package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.gui.quests.GuiQuests;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageDeleteObject;
import com.feed_the_beast.ftbquests.quest.Movable;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.theme.QuestTheme;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import me.shedaniel.architectury.utils.Env;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Objects;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ClientQuestFile extends QuestFile
{
	public static ClientQuestFile INSTANCE;

	public static boolean exists()
	{
		return INSTANCE != null && !INSTANCE.invalid;
	}

	public PlayerData self;
	public GuiQuests questTreeGui;
	public GuiBase questGui;

	@Override
	public void load(UUID s)
	{
		if (INSTANCE != null)
		{
			INSTANCE.deleteChildren();
			INSTANCE.deleteSelf();
		}

		self = Objects.requireNonNull(getData(s));
		self.name = Minecraft.getInstance().getUser().getName();
		INSTANCE = this;

		refreshGui();
		FTBQuestsJEIHelper.refresh(this);
	}

	@Override
	public boolean canEdit()
	{
		return self.getCanEdit();
	}

	public void refreshGui()
	{
		clearCachedData();

		boolean hasPrev = false;
		boolean guiOpen = false;
		int zoom = 0;
		double scrollX = 0, scrollY = 0;
		int selectedChapter = 0;
		int[] selectedQuests = new int[0];

		if (questTreeGui != null)
		{
			hasPrev = true;
			zoom = questTreeGui.zoom;
			scrollX = questTreeGui.questPanel.centerQuestX;
			scrollY = questTreeGui.questPanel.centerQuestY;
			selectedChapter = questTreeGui.selectedChapter == null ? 0 : questTreeGui.selectedChapter.id;
			selectedQuests = new int[questTreeGui.selectedObjects.size()];
			int i = 0;

			for (Movable m : questTreeGui.selectedObjects)
			{
				if (m instanceof Quest)
				{
					selectedQuests[i] = ((Quest) m).id;
				}

				i++;
			}

			if (ClientUtils.getCurrentGuiAs(GuiQuests.class) != null)
			{
				guiOpen = true;
			}
		}

		questTreeGui = new GuiQuests(this);
		questGui = questTreeGui;

		if (hasPrev)
		{
			questTreeGui.zoom = zoom;
			questTreeGui.selectChapter(getChapter(selectedChapter));

			for (int i : selectedQuests)
			{
				Quest q = getQuest(i);

				if (q != null)
				{
					questTreeGui.selectedObjects.add(q);
				}
			}

			if (guiOpen)
			{
				questTreeGui.openGui();
			}
		}

		questTreeGui.refreshWidgets();

		if (hasPrev)
		{
			questTreeGui.questPanel.scrollTo(scrollX, scrollY);
		}
	}

	public void openQuestGui()
	{
		if (disableGui && !self.getCanEdit())
		{
			Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("item.ftbquests.book.disabled"), true);
		}
		else if (exists())
		{
			questGui.openGui();
		}
	}

	@Override
	public Env getSide()
	{
		return Env.CLIENT;
	}

	@Override
	public void deleteObject(int id)
	{
		new MessageDeleteObject(id).sendToServer();
	}

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();
		QuestTheme.instance.clearCache();
	}
}