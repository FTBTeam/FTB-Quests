package dev.ftb.mods.ftbquests.gametest.base;

import com.google.common.util.concurrent.AtomicDouble;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import dev.ftb.mods.ftblibrary.platform.fluid.FluidStack;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.history.CreateOrDeleteRecord;
import dev.ftb.mods.ftbquests.quest.history.events.CreateQuestObjects;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.EnergyTask;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class QuestTestSupport {

	private static final AtomicInteger chapterFileNumber = new AtomicInteger(1);
	private static final AtomicDouble questXpos = new AtomicDouble(0.0);

	private QuestTestSupport() {}

	public static ServerQuestFile file() {
		return ServerQuestFile.getInstance();
	}

	public static Chapter newChapter() {
		return newChapter(new Json5Object());
	}

	public static Chapter newChapter(Json5Object config) {
		ServerQuestFile file = file();
		long chapterId = file.newID();
		config.addProperty("filename", "test_chapter_" + chapterFileNumber.getAndIncrement());
		file.getHistoryStack().addAndApply(file, new CreateQuestObjects(
				new CreateOrDeleteRecord(chapterId, 1L, QuestObjectType.CHAPTER,
						Component.empty(), config, new Json5Object()),
				null)
		);
		return file.getQuestObjectOrThrow(chapterId, Chapter.class);
	}

	public static Quest newQuest(Chapter chapter) {
		return newQuest(chapter, new Json5Object());
	}

	public static Quest newQuest(Chapter chapter, Json5Object config) {
		ServerQuestFile file = file();
		long questId = file.newID();
		config.addProperty("x", questXpos.getAndAdd(2.0));
		config.addProperty("y", 0.0);
		file.getHistoryStack().addAndApply(file, new CreateQuestObjects(
				new CreateOrDeleteRecord(questId, chapter.id, QuestObjectType.QUEST,
						Component.empty(), config, new Json5Object()),
				null)
		);
		return file.getQuestObjectOrThrow(questId, Quest.class);
	}

	public static Task newCheckmarkTask(Quest quest) {
		return newCheckmarkTask(quest, false);
	}

	private static Task newTask(Quest quest, Json5Object config, String typeId) {
		ServerQuestFile file = file();
		long taskId = file.newID();
		Json5Object metadata = Util.make(new Json5Object(), o -> o.addProperty("type", typeId));
		file.getHistoryStack().addAndApply(file, new CreateQuestObjects(
				new CreateOrDeleteRecord(taskId, quest.getId(), QuestObjectType.TASK, Component.empty(), config, metadata),
				null)
		);
		return file.getQuestObjectOrThrow(taskId, Task.class);
	}

	public static Task newCheckmarkTask(Quest quest, boolean optional) {
		Json5Object config = Util.make(new Json5Object(), o -> {
			if (optional) o.addProperty("optional_task", true);
		});
		return newTask(quest, config, "checkmark");
	}

	public static ItemTask newItemTask(Quest quest, ItemStack stack, int count) {
		Json5Object config = Util.make(new Json5Object(), o -> {
			o.add("item", ItemStack.CODEC.encodeStart(quest.getQuestFile().holderLookup().createSerializationContext(Json5Ops.INSTANCE), stack).getOrThrow());
			o.addProperty("count", count);
			o.addProperty("consume_items", true);
		});
		return (ItemTask) newTask(quest, config, "item");
	}

	public static FluidTask newFluidTask(Quest quest, FluidStack fluid) {
		Json5Object config = Util.make(new Json5Object(), o -> o.add("fluid", FluidStack.CODEC.encodeStart(quest.getQuestFile().holderLookup().createSerializationContext(Json5Ops.INSTANCE), fluid).getOrThrow()));
		return (FluidTask) newTask(quest, config, "fluid");
	}

	public static EnergyTask newEnergyTask(Quest quest, long value) {
		Json5Object config = Util.make(new Json5Object(), o -> o.addProperty("value", value));
		return (EnergyTask) newTask(quest, config, "forge_energy");
	}

	public static Reward newXpReward(Quest quest) {
		ServerQuestFile file = file();
		long rewardID = file.newID();
		Json5Object config = Util.make(new Json5Object(), o -> o.addProperty("xp", 10));
		Json5Object metadata = Util.make(new Json5Object(), o -> o.addProperty("type", "xp"));
		file.getHistoryStack().addAndApply(file, new CreateQuestObjects(
				new CreateOrDeleteRecord(rewardID, quest.getId(), QuestObjectType.REWARD, Component.empty(), config, metadata),
				null)
		);
		return file.getQuestObjectOrThrow(rewardID, Reward.class);
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
