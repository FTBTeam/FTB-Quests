package dev.ftb.mods.ftbquests.neoforge;

import dev.ftb.mods.ftbquests.command.ChangeProgressArgument;
import dev.ftb.mods.ftbquests.command.QuestObjectArgument;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ArgumentTypes {
    static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES
            = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, FTBTeamsAPI.MOD_ID);

    private static final DeferredHolder<ArgumentTypeInfo<?,?>, SingletonArgumentInfo<ChangeProgressArgument>> CHANGE_PROGRESS
            = COMMAND_ARGUMENT_TYPES.register("change_progress", () -> ArgumentTypeInfos.registerByClass(ChangeProgressArgument.class, SingletonArgumentInfo.contextFree(ChangeProgressArgument::changeProgress)));
    private static final DeferredHolder<ArgumentTypeInfo<?,?>, SingletonArgumentInfo<QuestObjectArgument>> QUEST_OBJECT
            = COMMAND_ARGUMENT_TYPES.register("quest_object", () -> ArgumentTypeInfos.registerByClass(QuestObjectArgument.class, SingletonArgumentInfo.contextFree(QuestObjectArgument::new)));
}
