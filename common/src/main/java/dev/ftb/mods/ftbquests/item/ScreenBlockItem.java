package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Consumer;

public class ScreenBlockItem extends BlockItem {
    public enum ScreenSize {
        ONE_X_ONE(1),
        THREE_X_THREE(3),
        FIVE_X_FIVE(5),
        SEVEN_X_SEVEN(7);

        private final int size;

        ScreenSize(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }
    }

    private final ScreenSize size;

    public ScreenBlockItem(Block block, ScreenSize size) {
        super(block, ModItems.defaultProps());

        this.size = size;
    }

    public static int getSize(ItemStack stack) {
        return stack.getItem() instanceof ScreenBlockItem sb ? sb.size.getSize() : 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, consumer, tooltipFlag);

        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            CompoundTag subTag = data.copyTagWithoutId();
            BaseQuestFile questFile = FTBQuestsClient.getClientQuestFile();
            if (questFile != null) {
                subTag.getLong("TaskID").ifPresent(taskID -> {
                    Task task = questFile.getTask(taskID);
                    if (task != null) {
                        consumer.accept(Component.translatable("ftbquests.chapter").append(": ")
                                .append(task.getQuest().getChapter().getTitle().copy().withStyle(ChatFormatting.YELLOW)));
                        consumer.accept(Component.translatable("ftbquests.quest").append(": ").append(task.getQuest().getMutableTitle().withStyle(ChatFormatting.YELLOW)));
                        consumer.accept(Component.translatable("ftbquests.task").append(": ").append(task.getMutableTitle().withStyle(ChatFormatting.YELLOW)));
                    }
                });
            }
        }
    }
}
