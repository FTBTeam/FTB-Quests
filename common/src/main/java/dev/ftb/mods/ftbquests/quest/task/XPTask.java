package dev.ftb.mods.ftbquests.quest.task;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.PlayerData;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class XPTask extends Task implements ISingleLongValueTask {
	public long value = 1L;
	public boolean points = false;

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
	public String getMaxProgressString() {
		return Long.toUnsignedString(points && value <= Integer.MAX_VALUE ? getLevelForExperience((int) value) : value);
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
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addLong("value", value, v -> value = v, 1L, 1L, Long.MAX_VALUE);
		config.addBool("points", points, v -> points = v, false);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return new TranslatableComponent("ftbquests.reward.ftbquests.xp_levels").append(": ").append(new TextComponent(getMaxProgressString()).withStyle(ChatFormatting.RED));
	}

	@Override
	public boolean consumesResources() {
		return true;
	}

	@Override
	public TaskData createData(PlayerData data) {
		return new Data(this, data);
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

	public static class Data extends TaskData<XPTask> {
		private Data(XPTask task, PlayerData data) {
			super(task, data);
		}

		@Override
		public String getProgressString() {
			return Long.toUnsignedString(task.points && task.value <= Integer.MAX_VALUE ? getLevelForExperience((int) progress) : progress);
		}

		@Override
		public void submitTask(ServerPlayer player, ItemStack item) {
			int add = (int) Math.min(task.points ? getPlayerXP(player) : player.experienceLevel, Math.min(task.value - progress, Integer.MAX_VALUE));

			if (add <= 0) {
				return;
			}

			if (task.points) {
				addPlayerXP(player, -add);
				player.giveExperienceLevels(0);
			} else {
				player.giveExperienceLevels(-add);
			}

			addProgress(add);
		}
	}
}