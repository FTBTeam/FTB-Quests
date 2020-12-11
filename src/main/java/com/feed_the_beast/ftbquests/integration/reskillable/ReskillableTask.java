package com.feed_the_beast.ftbquests.integration.reskillable;

import java.util.Collection;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.task.BooleanTaskData;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.quest.task.TaskType;

import codersafterdark.reskillable.api.data.PlayerDataHandler;
import codersafterdark.reskillable.api.data.PlayerSkillInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author RagnarDragus
 */
public class ReskillableTask extends Task 
{

	public static final ResourceLocation RESKILLABLE_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/reskillable.png");
	
	public String skill = "";
	public int skill_level = 0;
	
	public ReskillableTask(Quest quest) 
	{
		super(quest);
	}
	
	@Override
	public TaskType getType() 
	{
		return ReskillableItegration.RESKILLABLE_TASK;
	}
	
	@Override
	public void writeData(NBTTagCompound nbt) 
	{
		super.writeData(nbt);
		nbt.setString("skill", skill);
		nbt.setInteger("skill_level", skill_level);
	}
	
	@Override
	public void readData(NBTTagCompound nbt) 
	{
		super.readData(nbt);
		skill = nbt.getString("skill");
		skill_level = nbt.getInteger("skill_level");
	}
	
	@Override
	public void writeNetData(DataOut data) 
	{
		super.writeNetData(data);
		data.writeString(skill);
		data.writeVarInt(skill_level);
	}
	
	@Override
	public void readNetData(DataIn data) 
	{
		super.readNetData(data);
		skill = data.readString();
		skill_level = data.readVarInt();
	}
	
	@Override
	public void getConfig(ConfigGroup config) 
	{
		super.getConfig(config);
		config.addString("skill", () -> skill, v -> skill = v, "");
		config.addInt("skill_level", () -> skill_level, v -> skill_level = v, 0, 1, Integer.MAX_VALUE);
	}
	
	@Override
	public String getAltTitle() {
		return I18n.format("ftbquests.task.ftbquests.reskillable.skillyouneed") + ": " + TextFormatting.YELLOW + skill + " (" + skill_level + ")";
	}
	
	@Override
	public TaskData createData(QuestData data) 
	{
		return new Data(this, data);
	}
	
	public static class Data extends BooleanTaskData<ReskillableTask> 
	{

		public Data(ReskillableTask task, QuestData data) 
		{
			super(task, data);
		}
		
		@Override
		public boolean canSubmit(EntityPlayerMP player) 
		{
			return playerHasSkillLevel(player, task.skill, task.skill_level);
		}
		
		private boolean playerHasSkillLevel(EntityPlayerMP player, String skill, int lvl) 
		{
			Collection<PlayerSkillInfo> skills = PlayerDataHandler.get(player).getAllSkillInfo();
			
			for (PlayerSkillInfo playerSkillInfo : skills) 
			{
				if(playerSkillInfo.skill.getName().equals(skill) && playerSkillInfo.getLevel() == lvl) return true;
			}
			return false;
		}
	}
}
