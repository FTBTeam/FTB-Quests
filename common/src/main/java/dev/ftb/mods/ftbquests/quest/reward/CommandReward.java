package dev.ftb.mods.ftbquests.quest.reward;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.net.DisplayRewardToastMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandReward extends Reward {
	private static final String DEFAULT_COMMAND = "/say Hi, @p!";
	private static final Icon REWARD_ICON = Icon.getIcon("minecraft:block/command_block_back");
	public static final Pattern PATTERN = Pattern.compile("[{](\\w+)}");

	private String command;
	private boolean elevatePerms;
	private boolean silent;
	private String feedbackMessage;

	public CommandReward(long id, Quest quest) {
		super(id, quest);
		command = DEFAULT_COMMAND;
		feedbackMessage = "";
	}

	@Override
	public RewardType getType() {
		return RewardTypes.COMMAND;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.putString("command", command);
		if (elevatePerms) {
			nbt.putBoolean("elevate_perms", true);
		}
		if (silent) nbt.putBoolean("silent", true);
		if (!feedbackMessage.isEmpty()) {
			nbt.putString("feedback_message", feedbackMessage);
		}
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		command = nbt.getString("command");
		elevatePerms = nbt.getBoolean("elevate_perms");
		silent = nbt.getBoolean("silent");
		feedbackMessage = nbt.getString("feedback_message");
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(command, Short.MAX_VALUE);
		buffer.writeBoolean(elevatePerms);
		buffer.writeBoolean(silent);
		buffer.writeUtf(feedbackMessage, Short.MAX_VALUE);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		command = buffer.readUtf(Short.MAX_VALUE);
		elevatePerms = buffer.readBoolean();
		silent = buffer.readBoolean();
		feedbackMessage = buffer.readUtf(Short.MAX_VALUE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addString("command", command, v -> command = v, DEFAULT_COMMAND).setNameKey("ftbquests.reward.ftbquests.command");
		config.addBool("elevate", elevatePerms, v -> elevatePerms = v, false);
		config.addBool("silent", silent, v -> silent = v, false);
		config.addString("feedback_message", feedbackMessage, v -> feedbackMessage = v, "");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		Map<String, Object> overrides = new HashMap<>();
		overrides.put("p", player.getGameProfile().getName());

		BlockPos pos = player.blockPosition();
		overrides.put("x", pos.getX());
		overrides.put("y", pos.getY());
		overrides.put("z", pos.getZ());

		if (getQuestChapter() != null) {
			overrides.put("chapter", getQuestChapter());
		}

		overrides.put("quest", quest);
		FTBTeamsAPI.api().getManager().getTeamForPlayer(player).ifPresent(team -> {
			overrides.put("team", team.getName().getString());
			overrides.put("team_id", team.getShortName());
			overrides.put("long_team_id", team.getId().toString());
			overrides.put("member_count", team.getMembers().size());
			overrides.put("online_member_count", team.getOnlineMembers().size());
		});

		String cmd = format(command, overrides);

		CommandSourceStack source = player.createCommandSourceStack();
		if (elevatePerms) source = source.withPermission(4);
		if (silent) source = source.withSuppressedOutput();

		player.server.getCommands().performPrefixedCommand(source, cmd);

		if (notify) {
			String key = feedbackMessage.isEmpty() ? "ftbquests.reward.ftbquests.command.success" : feedbackMessage;
			NetworkManager.sendToPlayer(player, new DisplayRewardToastMessage(id, Component.translatable(key), REWARD_ICON, disableRewardScreenBlur));
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.reward.ftbquests.command").append(": ").append(Component.literal(command).withStyle(ChatFormatting.RED));
	}

	public static String format(String template, Map<String, Object> parameters) {
		StringBuilder newTemplate = new StringBuilder(template);
		List<Object> valueList = new ArrayList<>();

		Matcher matcher = PATTERN.matcher(template);

		while (matcher.find()) {
			String key = matcher.group(1);

			if (parameters.containsKey(key)) {
				String paramName = "{" + key + "}";
				int index = newTemplate.indexOf(paramName);
				if (index != -1) {
					newTemplate.replace(index, index + paramName.length(), "%s");
					valueList.add(parameters.get(key));
				}
			}
		}

		return String.format(newTemplate.toString(), valueList.toArray());
	}
}
