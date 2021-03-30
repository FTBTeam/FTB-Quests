package dev.ftb.mods.ftbquests.quest;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestObject extends QuestObjectBase {
	public boolean disableToast = false;

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);

		if (disableToast) {
			nbt.putBoolean("disable_toast", true);
		}
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		disableToast = nbt.getBoolean("disable_toast");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeBoolean(disableToast);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		disableToast = buffer.readBoolean();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addBool("disable_toast", disableToast, v -> disableToast = v, false).setNameKey("ftbquests.disable_completion_toast").setCanEdit(getQuestChapter() == null || !getQuestChapter().alwaysInvisible).setOrder(10);
	}

	@Override
	public abstract void changeProgress(PlayerData data, ChangeProgress type);

	public abstract int getRelativeProgressFromChildren(PlayerData data);

	public boolean cacheProgress() {
		return true;
	}

	public static int getRelativeProgressFromChildren(int progressSum, int count) {
		if (count <= 0 || progressSum <= 0) {
			return 0;
		} else if (progressSum >= count * 100) {
			return 100;
		}

		return Math.max(1, (int) (progressSum / (double) count));
	}

	public boolean isVisible(PlayerData data) {
		return true;
	}

	public void onCompleted(PlayerData data, List<ServerPlayer> onlineMembers, List<ServerPlayer> notifiedPlayers) {
	}

	protected void verifyDependenciesInternal(long original, int depth) {
	}

	@Environment(EnvType.CLIENT)
	public Color4I getProgressColor(PlayerData data) {
		if (data.isComplete(this)) {
			return ThemeProperties.QUEST_COMPLETED_COLOR.get();
		} else if (data.isStarted(this)) {
			return ThemeProperties.QUEST_STARTED_COLOR.get();
		}

		return Color4I.WHITE;
	}

	@Environment(EnvType.CLIENT)
	public Color4I getProgressColor(PlayerData data, boolean dim) {
		Color4I c = getProgressColor(data);

		if (dim) {
			return c.addBrightness(-0.35F);
		}

		return c;
	}
}