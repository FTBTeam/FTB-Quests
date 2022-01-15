package dev.ftb.mods.ftbquests.integration.jei;

import dev.ftb.mods.ftbquests.integration.fabric.FTBQuestsJEIHelperImpl;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.ReloadStage;

public class FTBQuestsREIIntegration implements REIClientPlugin {
	@Override
	public void postStage(PluginManager<REIClientPlugin> manager, ReloadStage stage) {
		FTBQuestsJEIHelperImpl.view = stack -> {
			for (EntryDefinition<?> definition : EntryTypeRegistry.getInstance().values()) {
				if (definition.getValueType().isInstance(stack)) {
					try {
						ViewSearchBuilder.builder()
								.addRecipesFor(EntryStack.of(definition.getType().cast(), stack))
								.open();
					} catch (Exception e) {
						// EntryStack.of could fail because we are guessing the type
						e.printStackTrace();
					}
					break;
				}
			}
		};
	}
}
