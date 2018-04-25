package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftbquests.FTBQuestsCommon;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.gui.GuiQuestTree;
import com.google.gson.JsonObject;

public class FTBQuestsClient extends FTBQuestsCommon
{
	public static GuiQuestTree questTreeGui = null;
	public static GuiBase questGui = null;

	@Override
	public void preInit()
	{
		super.preInit();
	}

	@Override
	public void postInit()
	{
		super.postInit();
	}

	public static void loadQuests(JsonObject json)
	{
		ClientQuestList.INSTANCE.fromJson(json);

		boolean oldData = false;
		boolean guiOpen = false;
		int zoom = 0, scrollX = 0, scrollY = 0;
		String selectedChapter = "";

		if (questTreeGui != null)
		{
			oldData = true;
			zoom = questTreeGui.zoom;
			scrollX = questTreeGui.quests.getScrollX();
			scrollY = questTreeGui.quests.getScrollY();
			selectedChapter = questTreeGui.selectedChapter == null ? "" : questTreeGui.selectedChapter.chapter.getName();

			if (ClientUtils.getCurrentGuiAs(GuiQuestTree.class) != null)
			{
				guiOpen = true;
			}
		}

		questTreeGui = new GuiQuestTree();
		questGui = questTreeGui;

		if (oldData)
		{
			questTreeGui.zoom = zoom;
			questTreeGui.quests.setScrollX(scrollX);
			questTreeGui.quests.setScrollY(scrollY);

			for (GuiQuestTree.ButtonChapter b : questTreeGui.chapterButtons)
			{
				if (b.chapter.getName().equals(selectedChapter))
				{
					questTreeGui.selectedChapter = b;
				}
			}

			if (guiOpen)
			{
				questTreeGui.openGui();
			}
		}
	}

	public static void openQuestGui()
	{
		if (questTreeGui != null)
		{
			questGui.openGui();
		}
	}
}