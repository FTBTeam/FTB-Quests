package dev.ftb.mods.ftbquests.quest;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbquests.events.progress.ProgressEventData;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class QuestObject extends QuestObjectBase {
	protected boolean disableToast = false;

	public QuestObject(long id) {
		super(id);
	}

	@Override
	public void writeData(Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);

		if (disableToast) json.addProperty("disable_toast", true);
	}

	@Override
	public void readData(Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);
		disableToast = Json5Util.getBoolean(json, "disable_toast").orElse(false);
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
	public void fillConfigGroup(EditableConfigGroup config) {
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

	public boolean isSearchable(@Nullable TeamData data) {
		return data != null && isVisible(data);
	}

	public void onStarted(ProgressEventData<?> data) {
	}

	public void onCompleted(ProgressEventData<?> data) {
	}

	protected void verifyDependenciesInternal(long original, int depth) {
	}

	public Color4I getProgressColor(TeamData data) {
		if (data.isCompleted(this)) {
			return ThemeProperties.QUEST_COMPLETED_COLOR.get();
		} else if (data.isStarted(this)) {
			return ThemeProperties.QUEST_STARTED_COLOR.get();
		}

		return ThemeProperties.QUEST_NOT_STARTED_COLOR.get();
	}

	public Color4I getProgressColor(TeamData data, boolean dim) {
		Color4I c = getProgressColor(data);
		return dim ? c.addBrightness(-0.35F) : c;
	}

	public Collection<? extends QuestObject> getChildren() {
		return List.of();
	}

	public boolean isCompletedRaw(TeamData data) {
		int nOptional = 0;
		int nCompleted = 0;
        for (QuestObject child : getChildren()) {
			boolean uncompleted = !data.isCompleted(child) && !data.isExcludedByOtherQuestline(child);
			if (uncompleted) {
				if (child.isOptionalForProgression(data)) {
					nOptional++;
				} else {
					return false;
				}
			} else {
				nCompleted++;
			}
        }
		// if there are no children at all, it's auto-completed (degenerate case)
		// if ALL children are optional, require at least one to be completed (e.g. quests with either/or tasks)
        return getChildren().isEmpty() || nOptional < getChildren().size() || nCompleted > 0;
	}

	public boolean isOptionalForProgression(TeamData teamData) {
		return false;
	}

	public boolean hasUnclaimedRewardsRaw(TeamData teamData, UUID player) {
		return false;
	}

	@Nullable
	public Quest getRelatedQuest() {
		return null;
	}
}
