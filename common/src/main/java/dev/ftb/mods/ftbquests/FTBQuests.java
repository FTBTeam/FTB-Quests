package dev.ftb.mods.ftbquests;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.utils.EnvExecutor;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.net.FTBQuestsNetHandler;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class FTBQuests {
	public static final String MOD_ID = "ftbquests";
	public static final Logger LOGGER = LogManager.getLogger("FTB Quests");

	public static FTBQuests instance;

	public static FTBQuestsCommon PROXY;
	public static FTBQuestsNetCommon NET_PROXY;

	public static final CreativeModeTab ITEM_GROUP = CreativeTabRegistry.create(new ResourceLocation(FTBQuests.MOD_ID, FTBQuests.MOD_ID), () -> new ItemStack(FTBQuestsItems.BOOK.get()));

	private static RecipeModHelper recipeModHelper;
	private static final RecipeModHelper NO_OP_HELPER = new RecipeModHelper.NoOp();

	public FTBQuests() {
		TaskTypes.init();
		RewardTypes.init();
		FTBQuestsNetHandler.init();
		PROXY = EnvExecutor.getEnvSpecific(() -> FTBQuestsClient::new, () -> FTBQuestsCommon::new);
		NET_PROXY = EnvExecutor.getEnvSpecific(() -> FTBQuestsNetClient::new, () -> FTBQuestsNetCommon::new);
		new FTBQuestsEventHandler().init();

		PROXY.init();
	}

	public static RecipeModHelper getRecipeModHelper() {
		return Objects.requireNonNullElse(recipeModHelper, NO_OP_HELPER);
	}

	public static void setRecipeModHelper(RecipeModHelper recipeModHelper) {
		if (FTBQuests.recipeModHelper != null) {
			throw new IllegalStateException("recipe mod helper has already been initialised!");
		}
		FTBQuests.recipeModHelper = recipeModHelper;
	}

	public void setup() {
	}
}
