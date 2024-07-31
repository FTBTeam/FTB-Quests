package dev.ftb.mods.ftbquests;

import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQClientProxy;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.net.ClearDisplayCacheMessage;
import dev.ftb.mods.ftbquests.net.FTBQuestsNetHandler;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class FTBQuests {
	public static final Logger LOGGER = LogManager.getLogger(FTBQuestsAPI.MOD_NAME);

	public static FTBQuests instance;

	public static IQuestProxy PROXY;

	private static RecipeModHelper recipeModHelper;
	private static final RecipeModHelper NO_OP_HELPER = new RecipeModHelper.NoOp();

	public FTBQuests() {
		FTBQuestsAPI._init(FTBQuestsAPIImpl.INSTANCE);

		PROXY = EnvExecutor.getEnvSpecific(() -> FTBQClientProxy::new, () -> FTBQServerProxy::new);

		TaskTypes.init();
		RewardTypes.init();
		FTBQuestsNetHandler.init();
		FTBQuestsEventHandler.INSTANCE.init();

		ReloadListenerRegistry.register(PackType.SERVER_DATA, new TagReloadListener());

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

	public void setup() {
	}

	private static class TagReloadListener implements ResourceManagerReloadListener {
		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			ClearDisplayCacheMessage.clearForAll(GameInstance.getServer());
		}
	}

	public static <T> Optional<T> getComponent(ItemStack stack, Supplier<DataComponentType<T>> componentType) {
		return Optional.ofNullable(stack.get(componentType.get()));
	}

	public static <T> Optional<T> getComponent(ItemStack stack, DataComponentType<T> componentType) {
		return Optional.ofNullable(stack.get(componentType));
	}
}
