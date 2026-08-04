package dev.ftb.mods.ftbquests;

import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.net.ClearDisplayCacheMessage;
import dev.ftb.mods.ftbquests.net.FTBQuestsNetHandler;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbquests.registry.ModBlocks;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class FTBQuests {
	public static final Logger LOGGER = LogManager.getLogger(FTBQuestsAPI.MOD_NAME);

	private static RecipeModHelper recipeModHelper = RecipeModHelper.NONE;

	public final FTBQuestsEventHandler eventHandler;

	public FTBQuests() {
		FTBQuestsAPI._init(FTBQuestsAPIImpl.INSTANCE);

		eventHandler = new FTBQuestsEventHandler();

		TaskTypes.init();
		RewardTypes.init();
		FTBQuestsNetHandler.init();

		ModDataComponents.register();
		ModBlocks.register();
		ModItems.register();
		ModBlockEntityTypes.register();

		Platform.get().addDataPackReloadListener(FTBQuestsAPI.MOD_ID, FTBQuestsAPI.id("tag_reload"), new TagReloadListener());

		if (Platform.get().isModLoaded("ftbqoptimizer")) {
			LOGGER.warn("WARNING: FTB Quests Optimizer detected!");
			LOGGER.warn("         FTB recommends against the use of this third party mod with FTB Quests");
			LOGGER.warn("         It is likely to *reduce* mod performance and may cause server stability issues");
		}
	}

	public static RecipeModHelper getRecipeModHelper() {
		return recipeModHelper;
	}

	public static void setRecipeModHelper(RecipeModHelper recipeModHelper) {
		// called from FTB XMod Compat
		if (FTBQuests.recipeModHelper != RecipeModHelper.NONE) {
			throw new IllegalStateException("recipe mod helper has already been initialised!");
		}
		FTBQuests.recipeModHelper = recipeModHelper;
	}

    public static Collection<LootCrate> getKnownLootCrates() {
		BaseQuestFile file = null;
		if (ServerQuestFile.exists()) {
			file = ServerQuestFile.getInstance();
		} else if (ClientQuestFile.exists()) {
			file = ClientQuestFile.getInstance();
		}

		return file == null ? List.of() : file.getRewardTables().stream()
				.map(RewardTable::getLootCrate)
				.filter(Objects::nonNull)
				.toList();

    }

	private static class TagReloadListener implements ResourceManagerReloadListener {
		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			ClearDisplayCacheMessage.clearForAll();
		}
	}

	public static <T> Optional<T> getComponent(ItemStack stack, Supplier<DataComponentType<T>> componentType) {
		return Optional.ofNullable(stack.get(componentType.get()));
	}

	public static <T> Optional<T> getComponent(ItemStack stack, DataComponentType<T> componentType) {
		return Optional.ofNullable(stack.get(componentType));
	}
}
