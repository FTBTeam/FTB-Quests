package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class ExperienceLevelReward extends QuestReward
{
	public final int xpLevels;

	public ExperienceLevelReward(int _xp)
	{
		xpLevels = _xp;
	}

	@Override
	public boolean reward(EntityPlayerMP player)
	{
		player.addExperienceLevel(xpLevels);
		return true;
	}

	@Override
	public QuestReward copy()
	{
		return new ExperienceLevelReward(xpLevels);
	}

	@Override
	public Icon getIcon()
	{
		return ExperienceReward.ICON;
	}

	@Override
	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("xp_levels", xpLevels);
		return json;
	}

	public String toString()
	{
		return "XP Levels: +" + xpLevels; //LANG
	}
}