package dev.ftb.mods.ftbquests.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.architectury.registry.registries.RegistrarManager;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.net.CreateObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.SyncTranslationMessageToClient;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.util.InventoryUtil;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GenerateChapterCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("generate_chapter")
                .requires(FTBQuestsCommands::hasEditorPermission)
                .then(literal("from_entire_creative_list")
                        .executes(context -> generateAllItemChapter(context.getSource()))
                )
                .then(literal("from_player_inventory")
                        .executes(context -> generateChapterFromPlayerInv(context.getSource()))
                )
                .then(literal("from_inventory")
                        .then(argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> generateChapterFromInv(context.getSource(), BlockPosArgument.getSpawnablePos(context, "pos")))
                        )
                );
    }

    private static int generateAllItemChapter(CommandSourceStack source) {
        if (!CreativeModeTabs.searchTab().hasAnyItems()) {
            CreativeModeTabs.tryRebuildTabContents(source.enabledFeatures(), true, source.getLevel().registryAccess());
        }

        return generateMultiItemChapter(source, CreativeModeTabs.searchTab().getSearchTabDisplayItems());
    }

    private static int generateChapterFromInv(CommandSourceStack source, BlockPos pos) {
        return generateMultiItemChapter(source, InventoryUtil.getItemsInInventory(source.getLevel(), pos, Direction.UP));
    }

    private static int generateChapterFromPlayerInv(CommandSourceStack source) throws CommandSyntaxException {
        return generateMultiItemChapter(source, source.getPlayerOrException().getInventory().getNonEquipmentItems());
    }

    private static int generateMultiItemChapter(CommandSourceStack source, Collection<ItemStack> allItems) {
        //noinspection DataFlowIssue
        List<ItemStack> list = allItems.stream()
                .filter(stack -> !stack.isEmpty() && RegistrarManager.getId(stack.getItem(), Registries.ITEM) != null)
                .sorted(Comparator.comparing(a -> RegistrarManager.getId(a.getItem(), Registries.ITEM)))
                .toList();

        if (list.isEmpty()) {
            FTBQuests.LOGGER.warn("No suitable items found for chapter auto-creation");
            return 0;
        }

        ServerQuestFile file = ServerQuestFile.getInstance();

        long newId = file.newID();
        Chapter chapter = new Chapter(newId, file, file.getDefaultChapterGroup());
        chapter.onCreated();

        FTBQuests.LOGGER.info("Adding {} items to new chapter ID: {}", list.size(), newId);

        chapter.setRawTitle("Auto-generated chapter [" + list.size() + " items]");
        chapter.setRawIcon(new ItemStack(Items.COMPASS));
        chapter.setDefaultQuestShape("rsquare");

        NetworkHelper.sendToAll(source.getServer(), CreateObjectResponseMessage.create(chapter, null));
        NetworkHelper.sendToAll(source.getServer(), SyncTranslationMessageToClient.create(chapter, file.getLocale(), TranslationKey.TITLE, chapter.getRawTitle()));

        addItemsToChapter(source, list, chapter);

        file.markDirty();
        file.saveNow();

        source.sendSuccess(() -> Component.literal("Done!"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static void addItemsToChapter(CommandSourceStack source, Collection<ItemStack> list, Chapter chapter) {
        int col = 0;
        int row = 0;
        String modid = null;

        for (ItemStack stack : list) {
            Identifier id = RegistrarManager.getId(stack.getItem(), Registries.ITEM);
            if (id == null) {
                continue;
            }
            if (modid == null) {
                modid = id.getNamespace();
            }
            if (!modid.equals(id.getNamespace())) {
                modid = id.getNamespace();
                col = 0;
                row += 2;
            } else if (col >= 40) {
                col = 0;
                row++;
            }

            Quest quest = new Quest(chapter.file.newID(), chapter);
            quest.onCreated();
            quest.setX(col);
            quest.setY(row);
            quest.setRawSubtitle(ItemStack.CODEC.encodeStart(source.registryAccess().createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow().toString());

            NetworkHelper.sendToAll(source.getServer(), CreateObjectResponseMessage.create(quest, null));

            ItemTask task = new ItemTask(chapter.file.newID(), quest);
            task.onCreated();
            task.setStackAndCount(stack, 1);//.setConsumeItems(Tristate.TRUE);

            NetworkHelper.sendToAll(source.getServer(), CreateObjectResponseMessage.create(task, task.getType().makeExtraNBT()));

            col++;
        }
    }
}
