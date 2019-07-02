package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public class TileQuestBarrier extends TileBase implements IHasConfig
{
	public int object = 1;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		nbt.setInteger("object", object);
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		object = nbt.getInteger("object");
	}

	@Override
	public void editConfig(EntityPlayerMP player, boolean editor)
	{
		if (!editor)
		{
			return;
		}

		ConfigGroup group0 = ConfigGroup.newGroup("tile");
		group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.barrier.name"));
		ConfigGroup config = group0.getGroup("ftbquests.barrier");

		config.add("object", new ConfigQuestObject(ServerQuestFile.INSTANCE, object, QuestObjectType.ALL_PROGRESSING)
		{
			@Override
			public void setObject(int v)
			{
				object = v;
			}
		}, new ConfigQuestObject(ServerQuestFile.INSTANCE, 1, QuestObjectType.ALL_PROGRESSING));

		FTBLibAPI.editServerConfig(player, group0, this);
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		updateContainingBlockInfo();
		markDirty();
		BlockUtils.notifyBlockUpdate(world, pos, getBlockState());
	}
}