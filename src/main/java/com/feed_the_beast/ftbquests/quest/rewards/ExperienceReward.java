package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class ExperienceReward extends QuestReward
{
	public static final Icon ICON = ItemIcon.getItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE));

	private final int xp;

	public ExperienceReward(ProgressingQuestObject parent, int id, int _xp)
	{
		super(parent, id);
		xp = _xp;
	}

	@Override
	public void reward(EntityPlayerMP player)
	{
		player.addExperience(xp);
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

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return "XP: " + TextFormatting.GREEN + "+" + xp; //LANG
	}
}