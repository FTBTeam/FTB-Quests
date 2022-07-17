package dev.ftb.mods.ftbquests.fabric;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.command.ChangeProgressArgument;
import dev.ftb.mods.ftbquests.command.QuestObjectArgument;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;

public class FTBQuestsFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		new FTBQuests().setup();

		ArgumentTypeRegistry.registerArgumentType(new ResourceLocation(FTBQuests.MOD_ID, "change_progress"), ChangeProgressArgument.class, SingletonArgumentInfo.contextFree(ChangeProgressArgument::changeProgress));
		ArgumentTypeRegistry.registerArgumentType(new ResourceLocation(FTBQuests.MOD_ID, "quest_object"), QuestObjectArgument.class, SingletonArgumentInfo.contextFree(QuestObjectArgument::new));
	}
}
