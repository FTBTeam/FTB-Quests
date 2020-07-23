package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class BiomeTask extends Task
{
	public Biome biome = null;

	public BiomeTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.BIOME;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);

		if (biome != null)
		{
			nbt.setString("biome", biome.getRegistryName().toString());
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		biome = nbt.hasKey("biome") ? Biome.REGISTRY.getObject(new ResourceLocation(nbt.getString("biome"))) : null;
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeResourceLocation(biome.getRegistryName());
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		biome = Biome.REGISTRY.getObject(data.readResourceLocation());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("biome", () -> biome == null ? "" : biome.getRegistryName().toString(), v -> biome = v.isEmpty() ? null : Biome.REGISTRY.getObject(new ResourceLocation(v)), "").setDisplayName(new TextComponentTranslation("ftbquests.task.ftbquests.biome"));
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.task.ftbquests.biome") + ": " + TextFormatting.DARK_GREEN + (biome == null ? "null" : biome.getRegistryName());
	}

	@Override
	public int autoSubmitOnPlayerTick()
	{
		return 20;
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<BiomeTask>
	{
		private Data(BiomeTask task, QuestData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(EntityPlayerMP player)
		{
			return player.world.getBiome(player.getPosition()) == task.biome && !player.isSpectator();
		}
	}
}