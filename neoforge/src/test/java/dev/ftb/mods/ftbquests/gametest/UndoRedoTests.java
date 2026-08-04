package dev.ftb.mods.ftbquests.gametest;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftbquests.gametest.base.FTBQTestRegistrar;
import dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.events.MoveMovableObject;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.util.Util;

import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertFalse;
import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertTrue;

public class UndoRedoTests {
    private UndoRedoTests() {}

    public static void register(FTBQTestRegistrar registrar) {
        registrar.add("undo_redo/object_creation", 20, helper -> helper.runAfterDelay(1, () -> {
            ServerQuestFile file = QuestTestSupport.file();
            file.getHistoryStack().clear();  // start from known empty state

            assertFalse(helper, file.getHistoryStack().tryUndo(file), "undo should fail (empty stack)");
            assertFalse(helper, file.getHistoryStack().tryRedo(file), "redo should fail (empty stack)");

            Chapter chapter = QuestTestSupport.newChapter();
            Quest quest = QuestTestSupport.newQuest(chapter);
            Task task = QuestTestSupport.newCheckmarkTask(quest);

            assertTrue(helper, file.get(chapter.id) instanceof Chapter, "chapter should exist after creation");
            assertTrue(helper, file.get(quest.id) instanceof Quest, "quest should exist after creation");
            assertTrue(helper, file.get(task.id) instanceof Task, "task should exist after creation");

            file.getHistoryStack().tryUndo(file);
            assertFalse(helper, file.get(task.id) instanceof Task, "task should not exist after undo");
            file.getHistoryStack().tryUndo(file);
            assertFalse(helper, file.get(quest.id) instanceof Quest, "quest should not exist after undo");
            file.getHistoryStack().tryUndo(file);
            assertFalse(helper, file.get(chapter.id) instanceof Chapter, "chapter should not exist after undo");

            file.getHistoryStack().tryRedo(file);
            assertTrue(helper, file.get(chapter.id) instanceof Chapter, "chapter should exist after redo");
            file.getHistoryStack().tryRedo(file);
            assertTrue(helper, file.get(quest.id) instanceof Quest, "quest should exist after redo");
            file.getHistoryStack().tryRedo(file);
            assertTrue(helper, file.get(task.id) instanceof Task, "task should exist after redo");

            assertTrue(helper, file.getHistoryStack().tryUndo(file), "undo should succeed");
            assertTrue(helper, file.getHistoryStack().tryUndo(file), "undo should succeed");
            assertTrue(helper, file.getHistoryStack().tryUndo(file), "undo should succeed");
            assertFalse(helper, file.getHistoryStack().tryUndo(file), "undo should fail (empty stack)");

            helper.succeed();
        }));

        registrar.add("undo_redo/object_deletion", 20, helper -> helper.runAfterDelay(1, () -> {
            ServerQuestFile file = QuestTestSupport.file();

            Chapter chapter = QuestTestSupport.newChapter();
            Quest quest = QuestTestSupport.newQuest(chapter);

            assertTrue(helper, file.get(quest.id) instanceof Quest, "quest should exist after creation");

            quest.setPosition(10.0, 20.0);
            quest.setSize(2.0);
            quest.setRawTitle("testing");
            Json5Object saved = Util.make(new Json5Object(), o -> quest.writeData(o, file.holderLookup()));

            assertTrue(helper, QuestTestSupport.deleteObject(quest), "quest deletion should succeed");
            assertFalse(helper, file.getQuest(quest.getId()) instanceof Quest, "quest should not be present");

            file.getHistoryStack().tryUndo(file);

            assertTrue(helper, file.getQuest(quest.getId()) instanceof Quest, "quest should be present");

            Json5Object restored = Util.make(new Json5Object(), o -> quest.writeData(o, file.holderLookup()));
            assertTrue(helper, saved.equals(restored), "config of undeleted quest should be the same as config before deletion");

            helper.succeed();
        }));

        registrar.add("undo_redo/object_moving", 20, helper -> helper.runAfterDelay(1, () -> {
            ServerQuestFile file = QuestTestSupport.file();

            Chapter chapter = QuestTestSupport.newChapter();
            Chapter chapter2 = QuestTestSupport.newChapter();
            Quest quest = QuestTestSupport.newQuest(chapter);

            double origX = quest.getX();
            double origY = quest.getY();

            assertTrue(helper, file.get(quest.id) instanceof Quest, "quest should exist after creation");

            file.getHistoryStack().addAndApply(file, MoveMovableObject.create(quest, chapter, origX + 5.0, origY + 2.0).orElseThrow());
            assertTrue(helper, quest.getX() == origX + 5.0, "quest X should be " + origX + 5.0 + ", got " + quest.getX());

            file.getHistoryStack().tryUndo(file);
            assertTrue(helper, quest.getX() == origX, "quest X should be " + origX + ", got " + quest.getX());

            assertTrue(helper, MoveMovableObject.create(quest, chapter, origX, origY).isEmpty(), "no-op move should return empty optional");

            file.getHistoryStack().addAndApply(file, MoveMovableObject.create(quest, chapter2, origX, origY).orElseThrow());
            assertTrue(helper, quest.getChapter().getId() == chapter2.getId(), "quest chapter ID should be " + chapter2.getId() + ", got " + quest.getChapter().getId());

            file.getHistoryStack().tryUndo(file);
            assertTrue(helper, quest.getChapter().getId() == chapter.getId(), "quest chapter ID should be " + chapter.getId() + ", got " + quest.getChapter().getId());

            helper.succeed();
        }));

        // TODO add more tests for other quest book edit events
    }
}
