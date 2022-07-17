package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class CommandReward extends Reward {
	public String command;
	public boolean playerCommand;

	public CommandReward(Quest quest) {
		super(quest);
		command = "/say Hi, @p!";
	}

	@Override
	public RewardType getType() {
		return RewardTypes.COMMAND;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("command", command);
		nbt.putBoolean("player_command", playerCommand);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		command = nbt.getString("command");
		playerCommand = nbt.getBoolean("player_command");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(command, Short.MAX_VALUE);
		buffer.writeBoolean(playerCommand);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		command = buffer.readUtf(Short.MAX_VALUE);
		playerCommand = buffer.readBoolean();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("command", command, v -> command = v, "/say Hi, @team!").setNameKey("ftbquests.reward.ftbquests.command");
		config.addBool("player", playerCommand, v -> playerCommand = v, false);
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		Map<String, Object> overrides = new HashMap<>();
		overrides.put("p", player.getGameProfile().getName());

		BlockPos pos = player.blockPosition();
		overrides.put("x", pos.getX());
		overrides.put("y", pos.getY());
		overrides.put("z", pos.getZ());

		Chapter chapter = getQuestChapter();

		if (chapter != null) {
			overrides.put("chapter", chapter);
		}

		overrides.put("quest", quest);

		String s = command;

		for (Map.Entry<String, Object> entry : overrides.entrySet()) {
			if (entry.getValue() != null) {
				s = s.replace("@" + entry.getKey(), entry.getValue().toString());
			}
		}

		player.server.getCommands().performCommand(playerCommand ? player.createCommandSourceStack() : player.server.createCommandSourceStack(), s);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.reward.ftbquests.command").append(": ").append(Component.literal(command).withStyle(ChatFormatting.RED));
	}
}
