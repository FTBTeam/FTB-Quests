package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class CommandReward extends Reward
{
	public String command;
	public boolean playerCommand;

	public CommandReward(Quest quest)
	{
		super(quest);
		command = "/say Hi, @team!";
	}

	@Override
	public RewardType getType()
	{
		return FTBQuestsRewards.COMMAND;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("command", command);
		nbt.putBoolean("player_command", true);
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		command = nbt.getString("command");
		playerCommand = nbt.getBoolean("player_command");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeString(command);
		buffer.writeBoolean(playerCommand);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		command = buffer.readString();
		playerCommand = buffer.readBoolean();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("command", command, v -> command = v, "/say Hi, @team!").setNameKey("ftbquests.reward.ftbquests.command");
		config.addBool("player", playerCommand, v -> playerCommand = v, false);
	}

	@Override
	public void claim(ServerPlayerEntity player, boolean notify)
	{
		Map<String, Object> overrides = new HashMap<>();
		overrides.put("p", player.getGameProfile().getName());

		BlockPos pos = player.getPosition();
		overrides.put("x", pos.getX());
		overrides.put("y", pos.getY());
		overrides.put("z", pos.getZ());

		Chapter chapter = getQuestChapter();

		if (chapter != null)
		{
			overrides.put("chapter", chapter);
		}

		overrides.put("quest", quest);

		String s = command;

		for (Map.Entry<String, Object> entry : overrides.entrySet())
		{
			if (entry.getValue() != null)
			{
				s = s.replace("@" + entry.getKey(), entry.getValue().toString());
			}
		}

		player.server.getCommandManager().handleCommand(playerCommand ? player.getCommandSource() : player.server.getCommandSource(), s);
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.reward.ftbquests.command") + ": " + TextFormatting.RED + command;
	}
}