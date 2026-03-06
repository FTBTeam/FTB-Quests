package dev.ftb.mods.ftbquests.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.CreateObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.SyncTranslationMessageToClient;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.util.InventoryUtil;

import org.jspecify.annotations.Nullable;

import static net.minecraft.commands.Commands.argument;

public class ImportRewardTableCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("import_reward_table_from_chest")
                .requires(FTBQuestsCommands::hasEditorPermission)
                .then(argument("name", StringArgumentType.string())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            return importRewards(ctx.getSource(), name, null);
                        })
                        .then(argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    BlockPos pos = BlockPosArgument.getSpawnablePos(ctx, "pos");
                                    return importRewards(ctx.getSource(), name, pos);
                                })
                        )
                );
    }

    private static int importRewards(CommandSourceStack source, String name, @Nullable BlockPos pos) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();
        ServerQuestFile file = ServerQuestFile.getInstance();

        if (pos == null) {
            pos = BlockPos.containing(player.pick(10, 1F, false).getLocation());
        }

        if (level.getBlockEntity(pos) == null) {
            throw FTBQuestsCommands.NO_INVENTORY.create();
        }

        RewardTable table = new RewardTable(file.newID(), file);
        table.setRawTitle(name);
        table.setRawIcon(Items.CHEST.getDefaultInstance());

        for (ItemStack stack : InventoryUtil.getItemsInInventory(level, pos, Direction.UP)) {
            if (!stack.isEmpty()) {
                table.addReward(table.makeWeightedItemReward(stack, 1f));
            }
        }

        file.addRewardTable(table);
        file.refreshIDMap();
        file.clearCachedData();
        file.markDirty();

        NetworkHelper.sendToAll(level.getServer(), CreateObjectResponseMessage.create(table, null));
        NetworkHelper.sendToAll(level.getServer(), SyncTranslationMessageToClient.create(table, file.getLocale(), TranslationKey.TITLE, name));

        source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.table_imported", name, table.getWeightedRewards().size()), false);

        return Command.SINGLE_SUCCESS;
    }
}
