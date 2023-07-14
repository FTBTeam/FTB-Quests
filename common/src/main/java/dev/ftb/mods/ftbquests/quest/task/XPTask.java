package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class XPTask extends Task implements ISingleLongValueTask {
	private long value = 1L;
	private boolean points = false;

	public XPTask(Quest quest) {
		super(quest);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.XP;
	}

	@Override
	public long getMaxProgress() {
		return value;
	}

	@Override
	public String formatMaxProgress() {
		return Long.toUnsignedString(points && value <= Integer.MAX_VALUE ? getLevelForExperience((int) value) : value);
	}

	@Override
	public String formatProgress(TeamData teamData, long progress) {
		return Long.toUnsignedString(points && value <= Integer.MAX_VALUE ? getLevelForExperience((int) progress) : progress);
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putLong("value", value);
		nbt.putBoolean("points", points);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		value = nbt.getLong("value");
		points = nbt.getBoolean("points");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeVarLong(value);
		buffer.writeBoolean(points);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		value = buffer.readVarLong();
		points = buffer.readBoolean();
	}

	@Override
	public void setValue(long v) {
		value = v;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addLong("value", value, v -> value = v, 1L, 1L, Long.MAX_VALUE);
		config.addBool("points", points, v -> points = v, false);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.reward.ftbquests.xp_levels").append(": ").append(Component.literal(formatMaxProgress()).withStyle(ChatFormatting.RED));
	}

	@Override
	public boolean consumesResources() {
		return true;
	}

	public static int getPlayerXP(Player player) {
		return (int) (getExperienceForLevel(player.experienceLevel) + (player.experienceProgress * player.getXpNeededForNextLevel()));
	}

	public static void addPlayerXP(Player player, int amount) {
		int experience = getPlayerXP(player) + amount;
		player.totalExperience = experience;
		player.experienceLevel = getLevelForExperience(experience);
		int expForLevel = getExperienceForLevel(player.experienceLevel);
		player.experienceProgress = (float) (experience - expForLevel) / (float) player.getXpNeededForNextLevel();
	}

	public static int xpBarCap(int level) {
		if (level >= 30) {
			return 112 + (level - 30) * 9;
		}

		if (level >= 15) {
			return 37 + (level - 15) * 5;
		}

		return 7 + level * 2;
	}

	private static int sum(int n, int a0, int d) {
		return n * (2 * a0 + (n - 1) * d) / 2;
	}

	public static int getExperienceForLevel(int level) {
		if (level == 0) {
			return 0;
		}

		if (level <= 15) {
			return sum(level, 7, 2);
		}

		if (level <= 30) {
			return 315 + sum(level - 15, 37, 5);
		}

		return 1395 + sum(level - 30, 112, 9);
	}

	public static int getLevelForExperience(int targetXp) {
		int level = 0;

		while (true) {
			final int xpToNextLevel = xpBarCap(level);

			if (targetXp < xpToNextLevel) {
				return level;
			}

			level++;
			targetXp -= xpToNextLevel;
		}
	}

	@Override
	public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
		int add = (int) Math.min(points ? getPlayerXP(player) : player.experienceLevel, Math.min(value - teamData.getProgress(this), Integer.MAX_VALUE));

		if (add <= 0) {
			return;
		}

		if (points) {
			addPlayerXP(player, -add);
			player.giveExperienceLevels(0);
		} else {
			player.giveExperienceLevels(-add);
		}

		teamData.addProgress(this, add);
	}
}
