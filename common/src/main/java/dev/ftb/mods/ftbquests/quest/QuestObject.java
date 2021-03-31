package dev.ftb.mods.ftbquests.quest;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

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
	public void changeProgress(Instant time, TeamData data, ChangeProgress type) {
		if (type.reset) {
			if (data.isStarted(this)) {
				data.setStarted(id, null);
			}

			if (data.isCompleted(this)) {
				data.setCompleted(id, null);
			}
		} else {
			if (!data.isStarted(this)) {
				data.setStarted(id, time);
			}

			if (!data.isCompleted(this)) {
				data.setCompleted(id, time);
			}
		}

		for (QuestObject child : getChildren()) {
			child.changeProgress(time, data, type);
		}
	}

	public abstract int getRelativeProgressFromChildren(TeamData data);

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

	public boolean isVisible(TeamData data) {
		return true;
	}

	public void onStarted(QuestProgressEventData<?> data) {
	}

	public void onCompleted(QuestProgressEventData<?> data) {
	}

	protected void verifyDependenciesInternal(long original, int depth) {
	}

	@Environment(EnvType.CLIENT)
	public Color4I getProgressColor(TeamData data) {
		if (data.isCompleted(this)) {
			return ThemeProperties.QUEST_COMPLETED_COLOR.get();
		} else if (data.isStarted(this)) {
			return ThemeProperties.QUEST_STARTED_COLOR.get();
		}

		return Color4I.WHITE;
	}

	@Environment(EnvType.CLIENT)
	public Color4I getProgressColor(TeamData data, boolean dim) {
		Color4I c = getProgressColor(data);

		if (dim) {
			return c.addBrightness(-0.35F);
		}

		return c;
	}

	public Collection<? extends QuestObject> getChildren() {
		return Collections.emptyList();
	}

	public boolean isCompletedRaw(TeamData data) {
		for (QuestObject child : getChildren()) {
			if (data.isCompleted(child)) {
				return false;
			}
		}

		return true;
	}
}