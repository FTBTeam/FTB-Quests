package dev.ftb.mods.ftbquests.gametest;

import dev.ftb.mods.ftbquests.block.neoforge.NeoTaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.gametest.base.FTBQTestRegistrar;
import dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.EnergyTask;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.registry.ModBlocks;
import dev.ftb.mods.ftblibrary.platform.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertTrue;

public final class CapabilityTests {

	private CapabilityTests() {}

	private static NeoTaskScreenBlockEntity placeScreen(GameTestHelper helper, BlockPos pos, TeamData data) {
		helper.setBlock(pos, ModBlocks.TASK_SCREEN_1.get().defaultBlockState());
		NeoTaskScreenBlockEntity screen = helper.getBlockEntity(pos, NeoTaskScreenBlockEntity.class);
		screen.setTeamId(data.getTeamId());
		return screen;
	}

	public static void register(FTBQTestRegistrar registrar) {
		registrar.add("capability/item_screen_adds_task_progress", 40, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			ItemStack target = new ItemStack(Items.DIAMOND);
			ItemTask task = QuestTestSupport.newItemTask(quest, target, 4);
			TeamData data = QuestTestSupport.newTeam();

			NeoTaskScreenBlockEntity screen = placeScreen(helper, new BlockPos(1, 1, 1), data);
			screen.setTask(task);

			int inserted;
			try (Transaction tx = Transaction.open(null)) {
				inserted = screen.getItemHandler().insert(ItemResource.of(target), 4, tx);
				tx.commit();
			}

			assertTrue(helper, inserted == 4, "item handler should accept 4 items, accepted " + inserted);
			assertTrue(helper, data.getProgress(task) == 4L, "item task progress should reflect inserted items");
			assertTrue(helper, data.isCompleted(task), "filling the item task via capability should complete it");
			helper.succeed();
		}));

		registrar.add("capability/item_screen_extract_reduces_progress", 40, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			ItemStack target = new ItemStack(Items.DIAMOND);
			ItemTask task = QuestTestSupport.newItemTask(quest, target, 8);
			TeamData data = QuestTestSupport.newTeam();

			NeoTaskScreenBlockEntity screen = placeScreen(helper, new BlockPos(1, 1, 1), data);
			screen.setTask(task);

			try (Transaction tx = Transaction.open(null)) {
				screen.getItemHandler().insert(ItemResource.of(target), 5, tx);
				tx.commit();
			}
			assertTrue(helper, data.getProgress(task) == 5L, "partial insert should leave 5 progress");

			int extracted;
			try (Transaction tx = Transaction.open(null)) {
				extracted = screen.getItemHandler().extract(ItemResource.of(target), 2, tx);
				tx.commit();
			}

			assertTrue(helper, extracted == 2, "item handler should return 2 items, returned " + extracted);
			assertTrue(helper, data.getProgress(task) == 3L, "extracting items should reduce task progress to 3");
			helper.succeed();
		}));

		registrar.add("capability/fluid_screen_adds_task_progress", 40, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			FluidTask task = QuestTestSupport.newFluidTask(quest, new FluidStack(Fluids.WATER, 1000));
			TeamData data = QuestTestSupport.newTeam();

			NeoTaskScreenBlockEntity screen = placeScreen(helper, new BlockPos(1, 1, 1), data);
			screen.setTask(task);

			int inserted;
			try (Transaction tx = Transaction.open(null)) {
				inserted = screen.getFluidHandler().insert(FluidResource.of(Fluids.WATER), 1000, tx);
				tx.commit();
			}

			assertTrue(helper, inserted == 1000, "fluid handler should accept 1000, accepted " + inserted);
			assertTrue(helper, data.getProgress(task) == 1000L, "fluid task progress should reflect inserted fluid");
			assertTrue(helper, data.isCompleted(task), "filling the fluid task via capability should complete it");
			helper.succeed();
		}));

		registrar.add("capability/energy_screen_adds_task_progress", 40, helper -> helper.runAfterDelay(1, () -> {
			Chapter chapter = QuestTestSupport.newChapter();
			Quest quest = QuestTestSupport.newQuest(chapter);
			EnergyTask task = QuestTestSupport.newEnergyTask(quest, 500L);
			TeamData data = QuestTestSupport.newTeam();

			NeoTaskScreenBlockEntity screen = placeScreen(helper, new BlockPos(1, 1, 1), data);
			screen.setTask(task);

			int inserted;
			try (Transaction tx = Transaction.open(null)) {
				inserted = screen.getEnergyHandler().insert(500, tx);
				tx.commit();
			}

			assertTrue(helper, inserted == 500, "energy handler should accept 500, accepted " + inserted);
			assertTrue(helper, data.getProgress(task) == 500L, "energy task progress should reflect inserted energy");
			assertTrue(helper, data.isCompleted(task), "filling the energy task via capability should complete it");
			helper.succeed();
		}));
	}
}
