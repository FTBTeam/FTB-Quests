package dev.ftb.mods.ftbquests.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

import static net.minecraft.commands.Commands.literal;

public class FTBQuestsCommands {
	public static final DynamicCommandExceptionType NO_OBJECT = new DynamicCommandExceptionType(
			(object) -> Component.translatable("commands.ftbquests.command.error.no_object", object));
	public static final DynamicCommandExceptionType INVALID_ID = new DynamicCommandExceptionType(
			(id) -> Component.translatable("commands.ftbquests.command.error.invalid_id", id));
	static final SimpleCommandExceptionType NO_INVENTORY = new SimpleCommandExceptionType(
			Component.translatable("commands.ftbquests.command.error.no_inventory"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal(FTBQuestsAPI.MOD_ID)
				.then(EditModeCommand.register())
				.then(LockedCommand.register())
				.then(DeleteEmptyRewardTablesCommand.register())
				.then(ChangeProgressCommand.register())
				.then(ExportRewardTableCommand.register())
				.then(ImportRewardTableCommand.register())
				.then(GenerateChapterCommand.register())
				.then(ReloadCommand.register())
				.then(BlockRewardsCommand.register())
				.then(OpenBookCommand.register())
				.then(ClearItemDisplayCacheCommand.register())
		);
	}

	static boolean isSSPOrEditor(CommandSourceStack s) {
		// s.getServer() *can* be null here, whatever the IDE thinks!
		//noinspection ConstantValue
		return s.getServer() != null && s.getServer().isSingleplayer() || hasEditorPermission(s);
	}

	static boolean hasEditorPermission(CommandSourceStack stack) {
		//noinspection DataFlowIssue
		return stack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
				|| stack.isPlayer() && PermissionsHelper.hasEditorPermission(stack.getPlayer(), false);
	}

	static QuestObjectBase getQuestObjectForString(String idStr) throws CommandSyntaxException {
		ServerQuestFile file = ServerQuestFile.getInstance();

        if (idStr.startsWith("#")) {
			String val = idStr.substring(1);
			for (QuestObjectBase qob : file.getAllObjects()) {
				if (qob.hasTag(val)) {
					return qob;
				}
			}
			throw NO_OBJECT.create(idStr);
		} else {
			long id = QuestObjectBase.parseHexId(idStr).orElseThrow(() -> INVALID_ID.create(idStr));
			QuestObjectBase qob = file.getBase(id);
			if (qob == null) {
				throw NO_OBJECT.create(idStr);
			}
			return qob;
		}
	}
}
