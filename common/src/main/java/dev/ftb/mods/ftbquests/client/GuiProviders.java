package dev.ftb.mods.ftbquests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableFluid;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableInt;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableItemStack;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableLong;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableString;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableStringifiedConfig;
import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.client.config.gui.EditStringConfigOverlay;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectFluidScreen;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.integration.currency.CurrencyHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.config.EditableQuestObject;
import dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.CurrencyReward;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.reward.StageReward;
import dev.ftb.mods.ftbquests.quest.reward.XPLevelsReward;
import dev.ftb.mods.ftbquests.quest.reward.XPReward;
import dev.ftb.mods.ftbquests.quest.task.*;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;

import java.util.function.BiConsumer;

public class GuiProviders {
    public static RewardType.GuiProvider defaultRewardGuiProvider(RewardType.Provider provider) {
        return (gui, quest, callback) -> {
            Reward reward = provider.create(0L, quest);

            if (reward instanceof RandomReward randomReward) {
                EditableQuestObject<RewardTable> config = new EditableQuestObject<>(QuestObjectType.REWARD_TABLE);
                SelectQuestObjectScreen<?> s = new SelectQuestObjectScreen<>(config, accepted -> {
                    if (accepted) {
                        randomReward.setTable(config.getValue());
                        callback.accept(reward);
                    }
                    gui.run();
                });
                s.setTitle(Component.translatable("ftbquests.gui.select_reward_table"));
                s.setHasSearchBox(true);
                s.openGui();
            } else {
                EditableConfigGroup group = new EditableConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
                    if (accepted) {
                        callback.accept(reward);
                    }
                    gui.run();
                });
                group.setNameKey(reward.getType().getTypeId().toLanguageKey("ftbquests.reward"));
                reward.fillConfigGroup(reward.createSubGroup(group));
                new EditConfigScreen(group).openGui();
            }
        };
    }

    public static TaskType.GuiProvider defaultTaskGuiProvider(TaskType.Provider provider) {
        return (panel, quest, callback) -> {
            Task task = provider.create(0L, quest);

            if (task instanceof ISingleLongValueTask slvTask) {
                EditableLong editable = new EditableLong(slvTask.getMinConfigValue(), slvTask.getMaxConfigValue());
                editable.setValue(slvTask.getMinConfigValue());

                EditStringConfigOverlay<Long> overlay = new EditStringConfigOverlay<>(panel.getGui(), editable, accepted -> {
                    if (accepted) {
                        slvTask.setValue(editable.getValue());
                        callback.accept(task, task.getType().makeExtraNBT());
                    }
                    panel.run();
                }, task.getType().getDisplayName()).atMousePosition();
                panel.getGui().pushModalPanel(overlay);
            } else {
                openSetupGui(panel.getGui(), callback, task);
            }
        };
    }

    public static void setTaskGuiProviders() {
        TaskTypes.ITEM.setGuiProvider((gui, quest, callback) -> {
            EditableItemStack editable = new EditableItemStack(false, false);
            new SelectItemStackScreen(editable, accepted -> {
                gui.run();
                if (accepted) {
                    ItemTask itemTask = new ItemTask(0L, quest).setStackAndCount(editable.getValue(), editable.getValue().getCount());
                    callback.accept(itemTask, itemTask.getType().makeExtraNBT());
                }
            }).openGui();
        });

        TaskTypes.CHECKMARK.setGuiProvider((panel, quest, callback) -> {
            EditableString editable = new EditableString();
            editable.setValue("");

            EditStringConfigOverlay<String> overlay = new EditStringConfigOverlay<>(panel.getGui(), editable, accepted -> {
                if (accepted) {
                    CheckmarkTask checkmarkTask = new CheckmarkTask(0L, quest);
                    checkmarkTask.setRawTitle(editable.getValue());
                    CompoundTag extra = checkmarkTask.getType().makeExtraNBT();
                    quest.getQuestFile().getTranslationManager().addInitialTranslation(extra, quest.getQuestFile().getLocale(), TranslationKey.TITLE, editable.getValue());
                    callback.accept(checkmarkTask, extra);
                }
                panel.run();
            }, TaskTypes.CHECKMARK.getDisplayName()).atMousePosition();
            panel.getGui().pushModalPanel(overlay);
        });

        TaskTypes.FLUID.setGuiProvider((gui, quest, callback) -> {
            EditableFluid editable = new EditableFluid(false);
            new SelectFluidScreen(editable, accepted -> {
                gui.run();
                if (accepted) {
                    FluidTask fluidTask = new FluidTask(0L, quest).setFluid(editable.getValue());
                    callback.accept(fluidTask, fluidTask.getType().makeExtraNBT());
                }
            }).openGui();
        });

        TaskTypes.DIMENSION.setGuiProvider((gui, quest, callback) -> {
            DimensionTask task = new DimensionTask(0L, quest)
                    .withDimension(Minecraft.getInstance().level.dimension());
            openSetupGui(gui, callback, task);
        });

        TaskTypes.OBSERVATION.setGuiProvider((gui, quest, callback) -> {
            ObservationTask task = new ObservationTask(0L, quest);
            if (Minecraft.getInstance().hitResult instanceof BlockHitResult bhr) {
                Block block = Minecraft.getInstance().level.getBlockState(bhr.getBlockPos()).getBlock();
                task.setToObserve(BuiltInRegistries.BLOCK.getKey(block).toString());
            }
            openSetupGui(gui, callback, task);
        });

        TaskTypes.LOCATION.setGuiProvider((gui, quest, callback) -> {
            LocationTask task = new LocationTask(0L, quest);
            Minecraft mc = Minecraft.getInstance();

            if (mc.hitResult instanceof BlockHitResult bhr) {
                var blockEntity = mc.level.getBlockEntity(bhr.getBlockPos());

                if (blockEntity instanceof StructureBlockEntity structure) {
                    task.initFromStructure(structure);
                    callback.accept(task, task.getType().makeExtraNBT());
                    return;
                }
            }

            openSetupGui(gui, callback, task);
        });
    }

    private static void openSetupGui(Runnable gui, BiConsumer<Task, CompoundTag> callback, Task task) {
        EditableConfigGroup group = new EditableConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
            gui.run();
            if (accepted) {
                callback.accept(task, task.getType().makeExtraNBT());
            }
        });
        group.setNameKey(task.getType().getTypeId().toLanguageKey("ftbquests.task"));
        task.fillConfigGroup(task.createSubGroup(group));

        new EditConfigScreen(group).openGui();
    }

    public static void setRewardGuiProviders() {
        RewardTypes.ITEM.setGuiProvider((gui, quest, callback) -> {
            EditableItemStack editable = new EditableItemStack(false, false);
            new SelectItemStackScreen(editable, accepted -> {
                if (accepted) {
                    ItemReward reward = new ItemReward(0L, quest, editable.getValue().copyWithCount(1), editable.getValue().getCount());
                    callback.accept(reward);
                }
                gui.run();
            }).openGui();
        });

        simpleRewardProvider(RewardTypes.XP, XPReward::new, Util.make(new EditableInt(1, Integer.MAX_VALUE), c -> c.setValue(100)));
        simpleRewardProvider(RewardTypes.XP_LEVELS, XPLevelsReward::new, Util.make(new EditableInt(1, Integer.MAX_VALUE), c -> c.setValue(5)));
        simpleRewardProvider(RewardTypes.STAGE, StageReward::new, new EditableString());
        if (CurrencyHelper.getInstance().getProvider().isValidProvider()) {
            simpleRewardProvider(RewardTypes.CURRENCY, CurrencyReward::new, Util.make(new EditableInt(1, Integer.MAX_VALUE), c -> c.setValue(1)));
        }
    }

    private static <T> void simpleRewardProvider(RewardType type, RewardFactory<T> factory, EditableStringifiedConfig<T> cfg) {
        type.setGuiProvider((panel, quest, callback) -> {
            EditStringConfigOverlay<T> overlay = new EditStringConfigOverlay<>(panel.getGui(), cfg, accepted -> {
                if (accepted) {
                    callback.accept(factory.create(0L, quest, cfg.getValue()));
                }
                panel.run();
            }, type.getDisplayName()).atMousePosition();
            panel.getGui().pushModalPanel(overlay);
        });
    }

    private interface RewardFactory<T> {
        Reward create(long id, Quest quest, T val);
    }
}
