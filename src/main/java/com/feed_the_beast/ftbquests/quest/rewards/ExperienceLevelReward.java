package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class ExperienceLevelReward extends QuestReward
{
	private final int xpLevels;

	public ExperienceLevelReward(Quest quest, int id, int _xp)
	{
		super(quest, id);
		xpLevels = _xp;
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
		player.addExperienceLevel(xpLevels);
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

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return "XP Levels: " + TextFormatting.GREEN + "+" + xpLevels; //LANG
	}
}