package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftblibrary.config.BooleanConfig;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.block.FTBQuestsBlocks;
import dev.ftb.mods.ftbquests.block.TaskScreenBlock;
import dev.ftb.mods.ftbquests.net.TaskScreenConfigResponse;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static dev.ftb.mods.ftbquests.block.TaskScreenBlock.FACING;

public class TaskScreenBlockEntity extends BlockEntity implements ITaskScreen {
    private long taskId = 0L;
    private Task task = null;
    private boolean indestructible = false;
    private boolean inputOnly = false;
    private boolean textShadow = false;
    private ItemStack inputModeIcon = ItemStack.EMPTY;
    private ItemStack skin = ItemStack.EMPTY;
    @NotNull
    private UUID teamId = Util.NIL_UUID;
    public float[] fakeTextureUV = null;  // null for unknown, 0-array for no texture, 4-array for a texture
    private TeamData cachedTeamData = null;

    public TaskScreenBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(FTBQuestsBlockEntities.CORE_TASK_SCREEN.get(), blockPos, blockState);
    }

    public Task getTask() {
        if (task == null && taskId != 0L || task != null && task.id != taskId) {
            task = FTBQuests.PROXY.getQuestFile(level.isClientSide).getTask(taskId);
        }

        return task;
    }

    public void setTask(Task task) {
        this.task = task;
        this.taskId = task == null ? 0L: task.id;
        setChanged();
    }

    @Override
    public boolean isInputOnly() {
        return inputOnly;
    }

    public void setInputOnly(boolean inputOnly) {
        this.inputOnly = inputOnly;
        setChanged();
    }

    public ItemStack getInputModeIcon() {
        return inputModeIcon;
    }

    public void setInputModeIcon(ItemStack inputModeIcon) {
        this.inputModeIcon = inputModeIcon;
        setChanged();
    }

    @Override
    public boolean isIndestructible() {
        return indestructible;
    }

    public void setIndestructible(boolean indestructible) {
        this.indestructible = indestructible;
        setChanged();
    }

    @Override
    public ItemStack getSkin() {
        return skin;
    }

    public void setSkin(ItemStack skin) {
        this.skin = skin;
        fakeTextureUV = null;
    }

    public boolean isTextShadow() {
        return textShadow;
    }

    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }

    public void setTeamId(@NotNull UUID teamId) {
        this.teamId = teamId;
        cachedTeamData = null;
    }

    @Override
    @NotNull
    public UUID getTeamId() {
        return teamId;
    }

    public TeamData getCachedTeamData() {
        if (cachedTeamData == null) {
            QuestFile f = FTBQuests.PROXY.getQuestFile(level.isClientSide);
            cachedTeamData = f.getNullableTeamData(getTeamId());
        }
        return cachedTeamData;
    }

    @Override
    public Optional<TaskScreenBlockEntity> getCoreScreen() {
        return Optional.of(this);
    }

    public void removeAllAuxScreens() {
        if (level != null && getBlockState().getBlock() instanceof TaskScreenBlock tsb) {
            BlockPos.betweenClosedStream(TaskScreenBlock.getMultiblockBounds(getBlockPos(), tsb.getSize(), getBlockState().getValue(FACING))).forEach(pos -> {
                if (level.getBlockState(pos).getBlock() == FTBQuestsBlocks.AUX_SCREEN.get()) {
                    level.removeBlock(pos, false);
                }
            });
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        teamId = compoundTag.hasUUID("TeamID") ? compoundTag.getUUID("TeamID") : Util.NIL_UUID;
        taskId = compoundTag.getLong("TaskID");
        skin = compoundTag.contains("Skin") ? ItemStack.of(compoundTag.getCompound("Skin")) : ItemStack.EMPTY;
        indestructible = compoundTag.getBoolean("Indestructible");
        inputOnly = compoundTag.getBoolean("InputOnly");
        inputModeIcon = compoundTag.contains("InputModeIcon") ? ItemStack.of(compoundTag.getCompound("InputModeIcon")) : ItemStack.EMPTY;
        textShadow = compoundTag.getBoolean("TextShadow");

        task = null;
        fakeTextureUV = null;  // force recalc
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        if (teamId != Util.NIL_UUID) compoundTag.putUUID("TeamID", teamId);
        if (taskId != 0L) compoundTag.putLong("TaskID", taskId);
        if (!skin.isEmpty()) compoundTag.put("Skin", skin.save(new CompoundTag()));
        if (indestructible) compoundTag.putBoolean("Indestructible", true);
        if (inputOnly) compoundTag.putBoolean("InputOnly", true);
        if (!inputModeIcon.isEmpty()) compoundTag.put("InputModeIcon", inputModeIcon.save(new CompoundTag()));
        if (textShadow) compoundTag.putBoolean("TextShadow", true);
    }

    public ConfigGroup fillConfigGroup(TeamData data) {
        ConfigGroup cg0 = new ConfigGroup("task_screen", accepted -> {
            if (accepted) {
                new TaskScreenConfigResponse(this).sendToServer();
            }
        });

        cg0.setNameKey(getBlockState().getBlock().getDescriptionId());
        ConfigGroup cg = cg0.getOrCreateSubgroup("screen");
        cg.add("task", new ConfigQuestObject<>(o -> isSuitableTask(data, o)), getTask(), this::setTask, null).setNameKey("ftbquests.task");
        cg.add("skin", new ItemStackConfig(true, true), getSkin(), this::setSkin, ItemStack.EMPTY).setNameKey("block.ftbquests.screen.skin");
        cg.add("text_shadow", new BooleanConfig(), isTextShadow(), this::setTextShadow, false).setNameKey("block.ftbquests.screen.text_shadow");
        cg.add("indestructible", new BooleanConfig(), isIndestructible(), this::setIndestructible, false).setNameKey("block.ftbquests.screen.indestructible");
        cg.add("input_only", new BooleanConfig(), isInputOnly(), this::setInputOnly, false).setNameKey("block.ftbquests.screen.input_only");
        cg.add("input_icon", new ItemStackConfig(true, true), getInputModeIcon(), this::setInputModeIcon, ItemStack.EMPTY).setNameKey("block.ftbquests.screen.input_mode_icon");

        return cg0;
    }

    private boolean isSuitableTask(TeamData data, QuestObjectBase o) {
        return o instanceof Task t && (data.getCanEdit(FTBQuests.PROXY.getClientPlayer()) || data.canStartTasks(t.quest)) && t.consumesResources();
    }

    public float[] getFakeTextureUV() {
        if (fakeTextureUV == null) {
            if (!skin.isEmpty() && skin.getItem() instanceof BlockItem bi) {
                BlockState state = bi.getBlock().defaultBlockState();
                Direction facing = getBlockState().getValue(FACING);
                if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
                } else if (state.hasProperty(BlockStateProperties.FACING)) {
                    state = state.setValue(BlockStateProperties.FACING, facing);
                }
                fakeTextureUV = FTBQuests.PROXY.getTextureUV(state, facing);
            } else {
                fakeTextureUV = new float[0];
            }
        }
        return fakeTextureUV;
    }
}
