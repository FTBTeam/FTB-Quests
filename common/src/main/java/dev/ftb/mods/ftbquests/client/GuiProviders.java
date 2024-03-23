package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.config.*;
import dev.ftb.mods.ftblibrary.config.ui.*;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.*;
import dev.ftb.mods.ftbquests.quest.task.*;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Consumer;

public class GuiProviders {
    public static RewardType.GuiProvider defaultRewardGuiProvider(RewardType.Provider provider) {
        return (gui, quest, callback) -> {
            Reward reward = provider.create(0L, quest);

            if (reward instanceof RandomReward randomReward) {
                ConfigQuestObject<RewardTable> config = new ConfigQuestObject<>(QuestObjectType.REWARD_TABLE);
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
                ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
                    if (accepted) {
                        callback.accept(reward);
                    }
                    gui.run();
                });
                reward.fillConfigGroup(reward.createSubGroup(group));
                new EditConfigScreen(group).openGui();
            }
        };
    }

    public static TaskType.GuiProvider defaultTaskGuiProvider(TaskType.Provider provider) {
        return (panel, quest, callback) -> {
            Task task = provider.create(0L, quest);

            if (task instanceof ISingleLongValueTask slvTask) {
                LongConfig c = new LongConfig(slvTask.getMinConfigValue(), slvTask.getMaxConfigValue());
                c.setValue(slvTask.getMinConfigValue());

                EditStringConfigOverlay<Long> overlay = new EditStringConfigOverlay<>(panel.getGui(), c, accepted -> {
                    if (accepted) {
                        slvTask.setValue(c.getValue());
                        callback.accept(task);
                    }
                    panel.run();
                }, task.getType().getDisplayName()).atMousePosition();
                overlay.setExtraZlevel(300);
                panel.getGui().pushModalPanel(overlay);
            } else {
                ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
                    if (accepted) {
                        callback.accept(task);
                    }
                    panel.run();
                });
                task.fillConfigGroup(task.createSubGroup(group));
                new EditConfigScreen(group).openGui();
            }
        };
    }

    public static void setTaskGuiProviders() {
        TaskTypes.ITEM.setGuiProvider((gui, quest, callback) -> {
            ItemStackConfig c = new ItemStackConfig(false, false);

            new SelectItemStackScreen(c, accepted -> {
                gui.run();
                if (accepted) {
                    ItemTask itemTask = new ItemTask(0L, quest).setStackAndCount(c.getValue(), c.getValue().getCount());
                    callback.accept(itemTask);
                }
            }).openGui();
        });

        TaskTypes.CHECKMARK.setGuiProvider((panel, quest, callback) -> {
            StringConfig c = new StringConfig(null);
            c.setValue("");

            EditStringConfigOverlay<String> overlay = new EditStringConfigOverlay<>(panel, c, accepted -> {
                if (accepted) {
                    CheckmarkTask checkmarkTask = new CheckmarkTask(0L, quest);
                    checkmarkTask.setRawTitle(c.getValue());
                    callback.accept(checkmarkTask);
                }
                panel.run();
            }, TaskTypes.CHECKMARK.getDisplayName())
                    .atPosition(panel.width / 3, panel.height + 5);
            panel.getGui().pushModalPanel(overlay);
        });

        TaskTypes.FLUID.setGuiProvider((gui, quest, callback) -> {
            FluidConfig c = new FluidConfig(false);

            new SelectFluidScreen(c, accepted -> {
                gui.run();
                if (accepted) {
                    FluidTask fluidTask = new FluidTask(0L, quest).setFluid(c.getValue());
                    callback.accept(fluidTask);
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
                    callback.accept(task);
                    return;
                }
            }

            openSetupGui(gui, callback, task);
        });
    }

    private static void openSetupGui(Runnable gui, Consumer<Task> callback, Task task) {
        ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
            gui.run();
            if (accepted) {
                callback.accept(task);
            }
        });
        task.fillConfigGroup(task.createSubGroup(group));

        new EditConfigScreen(group).openGui();
    }

    public static void setRewardGuiProviders() {
        RewardTypes.ITEM.setGuiProvider((gui, quest, callback) -> {
            ItemStackConfig c = new ItemStackConfig(false, false);

            new SelectItemStackScreen(c, accepted -> {
                if (accepted) {
                    ItemStack copy = c.getValue().copy();
                    copy.setCount(1);
                    ItemReward reward = new ItemReward(0L, quest, copy, c.getValue().getCount());
                    callback.accept(reward);
                }
                gui.run();
            }).openGui();
        });

        RewardTypes.XP.setGuiProvider((panel, quest, callback) -> {
            IntConfig c = new IntConfig(1, Integer.MAX_VALUE);
            c.setValue(100);

            EditStringConfigOverlay<Integer> overlay = new EditStringConfigOverlay<>(panel, c, accepted -> {
                if (accepted) {
                    callback.accept(new XPReward(0L, quest, c.getValue()));
                }

                panel.run();
            }, RewardTypes.XP.getDisplayName())
                    .atPosition(panel.width / 3, panel.height + 5);
            panel.getGui().pushModalPanel(overlay);
        });

        RewardTypes.XP_LEVELS.setGuiProvider((panel, quest, callback) -> {
            IntConfig c = new IntConfig(1, Integer.MAX_VALUE);
            c.setValue(5);

            EditStringConfigOverlay<Integer> overlay = new EditStringConfigOverlay<>(panel, c, accepted -> {
                if (accepted) {
                    callback.accept(new XPLevelsReward(0L, quest, c.getValue()));
                }
                panel.run();
            }, RewardTypes.XP_LEVELS.getDisplayName())
                    .atPosition(panel.width / 3, panel.height + 5);
            panel.getGui().pushModalPanel(overlay);
        });
    }
}
