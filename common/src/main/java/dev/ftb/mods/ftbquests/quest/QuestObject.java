package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class QuestObject extends QuestObjectBase {
	protected boolean disableToast = false;

	public QuestObject(long id) {
		super(id);
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);

		if (disableToast) {
			nbt.putBoolean("disable_toast", true);
		}
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		disableToast = nbt.getBoolean("disable_toast");
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeBoolean(disableToast);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		disableToast = buffer.readBoolean();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addBool("disable_toast", disableToast, v -> disableToast = v, false).setNameKey("ftbquests.disable_completion_toast").setCanEdit(getQuestChapter() == null || !getQuestChapter().isAlwaysInvisible()).setOrder(127);
	}

	@Override
	public void forceProgress(TeamData teamData, ProgressChange progressChange) {
		if (progressChange.shouldReset()) {
			teamData.setStarted(id, null);
			teamData.setCompleted(id, null);
		} else {
			teamData.setStarted(id, progressChange.getDate());
			teamData.setCompleted(id, progressChange.getDate());
		}

		for (QuestObject child : getChildren()) {
			child.forceProgress(teamData, progressChange);
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

	public boolean isSearchable(TeamData data) {
		return isVisible(data);
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
		return dim ? c.addBrightness(-0.35F) : c;
	}

	public Collection<? extends QuestObject> getChildren() {
		return List.of();
	}

	public boolean isCompletedRaw(TeamData data) {
		FTBQuests.LOGGER.info("is completed raw: checking {}", this);
        for (QuestObject child : getChildren()) {
			FTBQuests.LOGGER.info("- check child {}", child);
            if (!child.isOptionalForProgression()
                    && !data.isExcludedByOtherQuestline(child)
                    && !data.isCompleted(child)) {
				FTBQuests.LOGGER.info("- not completed!");
				return false;
			}
        }
        return true;

//		return getChildren().stream().noneMatch(child -> !child.isOptionalForProgression() && !data.isCompleted(child));
	}

	public boolean isOptionalForProgression() {
		return false;
	}

	public boolean hasUnclaimedRewardsRaw(TeamData teamData, UUID player) {
		return false;
	}

	public Quest getRelatedQuest() {
		return null;
	}
}