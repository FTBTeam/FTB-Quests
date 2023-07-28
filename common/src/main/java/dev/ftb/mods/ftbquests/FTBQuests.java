package dev.ftb.mods.ftbquests;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.net.FTBQuestsNetHandler;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class FTBQuests {
	public static final String MOD_ID = "ftbquests";
	public static final Logger LOGGER = LogManager.getLogger("FTB Quests");

	public static FTBQuests instance;

	private static RecipeModHelper recipeModHelper;
	private static final RecipeModHelper NO_OP_HELPER = new RecipeModHelper.NoOp();

	public FTBQuests() {
		TaskTypes.init();
		RewardTypes.init();
		FTBQuestsNetHandler.init();
		FTBQuestsEventHandler.INSTANCE.init();

		EnvExecutor.runInEnv(Env.CLIENT, () -> FTBQuestsClient::init);
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

	public static QuestFile getQuestFile(boolean isClient) {
		return isClient ?
				Objects.requireNonNull(FTBQuestsClient.getClientQuestFile()) :
				ServerQuestFile.INSTANCE;
	}

	public void setup() {
	}
}
