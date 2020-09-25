package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public class DimensionTask extends Task
{
	public ResourceLocation dimension;

	public DimensionTask(Quest quest)
	{
		super(quest);
		dimension = World.field_234919_h_.getRegistryName();
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.DIMENSION;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("dimension", dimension.toString());
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		dimension = new ResourceLocation(nbt.getString("dimension"));
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeResourceLocation(dimension);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		dimension = buffer.readResourceLocation();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("dim", dimension.toString(), v -> dimension = new ResourceLocation(v), "minecraft:the_nether");
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.task.ftbquests.dimension") + ": " + TextFormatting.DARK_GREEN + dimension;
	}

	@Override
	public int autoSubmitOnPlayerTick()
	{
		return 20;
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<DimensionTask>
	{
		private Data(DimensionTask task, PlayerData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(ServerPlayerEntity player)
		{
			return !player.isSpectator() && player.world.func_234923_W_().getRegistryName().equals(task.dimension);
		}
	}
}