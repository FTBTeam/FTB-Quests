package dev.ftb.mods.ftbquests.gametest.base;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.EnergyTask;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.gametest.framework.GameTestHelper;
import dev.ftb.mods.ftblibrary.client.config.Tristate;
import dev.ftb.mods.ftblibrary.platform.fluid.FluidStack;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;

public final class QuestTestSupport {

	private QuestTestSupport() {}

	public static ServerQuestFile file() {
		return ServerQuestFile.getInstance();
	}

	private static void applyConfig(QuestObjectBase object, Json5Object overrides) {
		Json5Object json = new Json5Object();
		object.writeData(json, file().holderLookup());
		for (Map.Entry<String, Json5Element> entry : overrides.entrySet()) {
			json.add(entry.getKey(), entry.getValue());
		}
		object.readData(json, file().holderLookup());
	}

	public static Chapter newChapter() {
		return newChapter(new Json5Object());
	}

	public static Chapter newChapter(Json5Object config) {
		ServerQuestFile file = file();
		Chapter chapter = (Chapter) file.create(file.newID(), QuestObjectType.CHAPTER, 1L, new Json5Object());
		applyConfig(chapter, config);
		chapter.onCreated();
		file.refreshIDMap();
		file.clearCachedData();
		return chapter;
	}

	public static Quest newQuest(Chapter chapter) {
		return newQuest(chapter, new Json5Object());
	}

	public static Quest newQuest(Chapter chapter, Json5Object config) {
		ServerQuestFile file = file();
		Quest quest = (Quest) file.create(file.newID(), QuestObjectType.QUEST, chapter.id, new Json5Object());
		applyConfig(quest, config);
		quest.onCreated();
		file.refreshIDMap();
		file.clearCachedData();
		return quest;
	}

	public static Task newCheckmarkTask(Quest quest) {
		return newCheckmarkTask(quest, false);
	}

	public static Task newCheckmarkTask(Quest quest, boolean optional) {
		ServerQuestFile file = file();
		Json5Object extra = new Json5Object();
		extra.addProperty("type", "checkmark");
		Task task = (Task) file.create(file.newID(), QuestObjectType.TASK, quest.id, extra);
		Json5Object config = new Json5Object();
		if (optional) {
			config.addProperty("optional_task", true);
		}
		applyConfig(task, config);
		task.onCreated();
		file.refreshIDMap();
		file.clearCachedData();
		return task;
	}

	private static Task newTask(Quest quest, String typeId) {
		ServerQuestFile file = file();
		Json5Object extra = new Json5Object();
		extra.addProperty("type", typeId);
		return (Task) file.create(file.newID(), QuestObjectType.TASK, quest.id, extra);
	}

	private static <T extends Task> T finishTask(T task) {
		ServerQuestFile file = file();
		task.onCreated();
		file.refreshIDMap();
		file.clearCachedData();
		return task;
	}

	public static ItemTask newItemTask(Quest quest, ItemStack stack, int count) {
		ItemTask task = (ItemTask) newTask(quest, "item");
		task.setStackAndCount(stack, count);
		task.setConsumeItems(Tristate.TRUE);
		return finishTask(task);
	}

	public static FluidTask newFluidTask(Quest quest, FluidStack fluid) {
		FluidTask task = (FluidTask) newTask(quest, "fluid");
		task.setFluid(fluid);
		return finishTask(task);
	}

	public static EnergyTask newEnergyTask(Quest quest, long value) {
		EnergyTask task = (EnergyTask) newTask(quest, "forge_energy");
		task.setValue(value);
		return finishTask(task);
	}

	public static Reward newXpReward(Quest quest) {
		ServerQuestFile file = file();
		Json5Object extra = new Json5Object();
		extra.addProperty("type", "xp");
		Reward reward = (Reward) file.create(file.newID(), QuestObjectType.REWARD, quest.id, extra);
		reward.onCreated();
		file.refreshIDMap();
		file.clearCachedData();
		return reward;
	}

	public static TeamData newTeam() {
		ServerQuestFile file = file();
		TeamData data = new TeamData(UUID.randomUUID(), true);
		file.addData(data, true);
		return data;
	}

	public static void complete(TeamData data, Task task) {
		data.setProgress(task, task.getMaxProgress());
	}

	public static void assertTrue(GameTestHelper helper, boolean condition, String message) {
		if (!condition) {
			helper.fail(message);
		}
	}

	public static void assertFalse(GameTestHelper helper, boolean condition, String message) {
		assertTrue(helper, !condition, message);
	}
}
