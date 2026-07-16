package dev.ftb.mods.ftbquests.gametest;

import dev.ftb.mods.ftbquests.gametest.base.FTBQTestRegistrar;
import dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;

public class UndoRedoTests {
    private UndoRedoTests() {}

    public static void register(FTBQTestRegistrar registrar) {
        registrar.add("undo_redo/object_creation", 20, helper -> helper.runAfterDelay(1, () -> {
            Chapter chapter = QuestTestSupport.newChapter();
            Quest quest = QuestTestSupport.newQuest(chapter);
            Task task = QuestTestSupport.newCheckmarkTask(quest);

            ServerQuestFile file = QuestTestSupport.file();

            QuestTestSupport.assertTrue(helper, file.get(chapter.id) instanceof Chapter, "chapter should exist after creation");
            QuestTestSupport.assertTrue(helper, file.get(quest.id) instanceof Quest, "quest should exist after creation");
            QuestTestSupport.assertTrue(helper, file.get(task.id) instanceof Task, "task should exist after creation");

            file.getHistoryStack().tryUndo(file);
            QuestTestSupport.assertFalse(helper, file.get(task.id) instanceof Task, "task should not exist after undo");

            file.getHistoryStack().tryUndo(file);
            QuestTestSupport.assertFalse(helper, file.get(quest.id) instanceof Quest, "quest should not exist after undo");

            file.getHistoryStack().tryUndo(file);
            QuestTestSupport.assertFalse(helper, file.get(chapter.id) instanceof Chapter, "chapter should not exist after undo");

            file.getHistoryStack().tryRedo(file);
            QuestTestSupport.assertTrue(helper, file.get(chapter.id) instanceof Chapter, "chapter should exist after redo");

            helper.succeed();
        }));

        // TODO add more tests for other quest book edit events
    }
}
