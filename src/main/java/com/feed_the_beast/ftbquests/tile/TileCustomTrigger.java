package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.EnumChangeProgress;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class TileCustomTrigger extends TileWithTeam implements IConfigCallback
{
	private static final Predicate<QuestObjectBase> PREDICATE = object -> object instanceof CustomTask;

	public int object = 0;
	public int requiredRedstone = 1;
	public int currentRedstone = 0;
	public boolean notifications = true;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		super.writeData(nbt, type);
		nbt.setInteger("object", object);
		nbt.setByte("required_redstone", (byte) requiredRedstone);
		nbt.setByte("current_redstone", (byte) currentRedstone);
		nbt.setBoolean("notifications", notifications);
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		super.readData(nbt, type);
		object = nbt.getInteger("object");
		requiredRedstone = MathHelper.clamp(nbt.getByte("required_redstone"), 1, 15);
		currentRedstone = nbt.getByte("current_redstone");
		notifications = !nbt.hasKey("notifications") || nbt.getBoolean("notifications");
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		markDirty();
	}

	public void checkRedstone()
	{
		int prev = currentRedstone;
		currentRedstone = world.getRedstonePowerFromNeighbors(pos);

		if (prev != currentRedstone && currentRedstone >= requiredRedstone && !world.isRemote)
		{
			QuestObject o = ServerQuestFile.INSTANCE.get(object);

			if (o != null)
			{
				ITeamData data = getTeam();

				if (data != null)
				{
					o.forceProgress(data, EnumChangeProgress.COMPLETE, notifications);
				}
			}
		}
	}

	public void editConfig(EntityPlayerMP player)
	{
		if (!FTBQuests.canEdit(player))
		{
			return;
		}

		QuestObject cObject = ServerQuestFile.INSTANCE.get(object);

		ConfigGroup group0 = ConfigGroup.newGroup("tile");
		group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.progress_detector.name"));
		ConfigGroup config = group0.getGroup("ftbquests.custom_trigger");

		config.add("team", createTeamConfig(), ConfigNull.INSTANCE).setDisplayName(new TextComponentTranslation("ftbquests.team"));

		config.add("object", new ConfigQuestObject(ServerQuestFile.INSTANCE, cObject, PREDICATE)
		{
			@Override
			public void setObject(@Nullable QuestObjectBase v)
			{
				object = v == null ? 0 : v.id;
			}
		}, new ConfigQuestObject(ServerQuestFile.INSTANCE, ServerQuestFile.INSTANCE, PREDICATE));

		config.addInt("required_redstone", () -> requiredRedstone, v -> requiredRedstone = v, 1, 1, 15);
		config.addBool("notifications", () -> notifications, v -> notifications = v, true);

		FTBLibAPI.editServerConfig(player, group0, this);
	}
}