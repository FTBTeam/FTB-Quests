package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ExperienceReward extends QuestReward
{
	public static final Icon ICON = ItemIcon.getItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE));

	public final int xp;

	public ExperienceReward(int _xp)
	{
		xp = _xp;
	}

	@Override
	public boolean reward(EntityPlayerMP player)
	{
		player.addExperience(xp);
		return true;
	}

	@Override
	public QuestReward copy()
	{
		return new ExperienceReward(xp);
	}

	@Override
	public Icon getIcon()
	{
		return ICON;
	}

	@Override
	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("xp", xp);
		return json;
	}

	public String toString()
	{
		return "XP: +" + xp;
	}
}