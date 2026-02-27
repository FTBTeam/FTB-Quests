package dev.ftb.mods.ftbquests.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.BooleanConfig;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.TaskScreenBlock;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.net.BlockConfigResponseMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbquests.registry.ModBlocks;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static dev.ftb.mods.ftbquests.block.TaskScreenBlock.FACING;

public class TaskScreenBlockEntity extends EditableBlockEntity implements ITaskScreen {
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
        super(ModBlockEntityTypes.CORE_TASK_SCREEN.get(), blockPos, blockState);
    }

    public Task getTask() {
        if (task == null && taskId != 0L || task != null && task.id != taskId) {
            task = FTBQuestsAPI.api().getQuestFile(level.isClientSide).getTask(taskId);
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
            BaseQuestFile f = FTBQuestsAPI.api().getQuestFile(level.isClientSide);
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
                if (level.getBlockState(pos).getBlock() == ModBlocks.AUX_SCREEN.get()) {
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
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);

        teamId = compoundTag.hasUUID("TeamID") ? compoundTag.getUUID("TeamID") : Util.NIL_UUID;

        TaskScreenSaveData data = TaskScreenSaveData.CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound("savedData"))
                .result().orElse(TaskScreenSaveData.DEFAULT);
        applySavedData(data);

        task = null;
        fakeTextureUV = null;  // force recalc
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);

        if (teamId != Util.NIL_UUID) compoundTag.putUUID("TeamID", teamId);

        TaskScreenSaveData.CODEC.encodeStart(NbtOps.INSTANCE, TaskScreenSaveData.fromBlockEntity(this))
                .ifSuccess(tag -> compoundTag.put("savedData", tag));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput dataComponentInput) {
        super.applyImplicitComponents(dataComponentInput);

        applySavedData(dataComponentInput.getOrDefault(ModDataComponents.TASK_SCREEN_SAVED.get(), TaskScreenSaveData.DEFAULT));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        builder.set(ModDataComponents.TASK_SCREEN_SAVED.get(), TaskScreenSaveData.fromBlockEntity(this));
    }

    private void applySavedData(TaskScreenSaveData data) {
        taskId = data.taskId;
        skin = data.skin;
        indestructible = data.indestructible;
        inputOnly = data.inputOnly;
        inputModeIcon = data.inputModeIcon;
        textShadow = data.textShadow;
    }

    public ConfigGroup fillConfigGroup(TeamData data) {
        ConfigGroup cg0 = new ConfigGroup("task_screen", accepted -> {
            if (accepted) {
                NetworkManager.sendToServer(new BlockConfigResponseMessage(getBlockPos(), saveWithoutMetadata(getLevel().registryAccess())));
            }
        });

        cg0.setNameKey(getBlockState().getBlock().getDescriptionId());
        ConfigGroup cg = cg0.getOrCreateSubgroup("screen");
        cg.add("task", new ConfigQuestObject<>(o -> isSuitableTask(data, o), this::formatLine), getTask(), this::setTask, null).setNameKey("ftbquests.task");
        cg.add("skin", new ItemStackConfig(true, true), getSkin(), this::setSkin, ItemStack.EMPTY).setNameKey("block.ftbquests.screen.skin");
        cg.add("text_shadow", new BooleanConfig(), isTextShadow(), this::setTextShadow, false).setNameKey("block.ftbquests.screen.text_shadow");
        cg.add("indestructible", new BooleanConfig(), isIndestructible(), this::setIndestructible, false).setNameKey("block.ftbquests.screen.indestructible");
        cg.add("input_only", new BooleanConfig(), isInputOnly(), this::setInputOnly, false).setNameKey("block.ftbquests.screen.input_only");
        cg.add("input_icon", new ItemStackConfig(true, true), getInputModeIcon(), this::setInputModeIcon, ItemStack.EMPTY).setNameKey("block.ftbquests.screen.input_mode_icon");

        return cg0;
    }

    private Component formatLine(Task task) {
        if (task == null) return Component.empty();

        Component questTxt = Component.literal(" [").append(task.getQuest().getTitle()).append("]").withStyle(ChatFormatting.GREEN);
        return ConfigQuestObject.formatEntry(task).copy().append(questTxt);
    }

    private boolean isSuitableTask(TeamData data, QuestObjectBase o) {
        return o instanceof Task t && (data.getCanEdit(FTBQuestsClient.getClientPlayer()) || data.canStartTasks(t.getQuest())) && t.consumesResources();
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
                fakeTextureUV = FTBQuestsClient.getTextureUV(state, facing);
            } else {
                fakeTextureUV = new float[0];
            }
        }
        return fakeTextureUV;
    }

    @Override
    public boolean hasPermissionToEdit(Player player) {
        // either the player must be the owner of the screen...
        if (player.getUUID().equals(getTeamId())) {
            return true;
        }

        // ...or in the same team as the owner of the screen
        return FTBTeamsAPI.api().getManager().getTeamByID(getTeamId())
                .map(team -> team.getRankForPlayer(player.getUUID()).isMemberOrBetter())
                .orElse(false);
    }

    public record TaskScreenSaveData(long taskId, ItemStack skin, boolean indestructible, boolean inputOnly, ItemStack inputModeIcon, boolean textShadow) {
        public static TaskScreenSaveData DEFAULT = new TaskScreenSaveData(
                0L, ItemStack.EMPTY, false, false, ItemStack.EMPTY, false
        );

        public static TaskScreenSaveData fromBlockEntity(TaskScreenBlockEntity b) {
            return new TaskScreenSaveData(b.taskId, b.skin, b.indestructible, b.inputOnly, b.inputModeIcon, b.textShadow);
        }

        public static final Codec<TaskScreenSaveData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.LONG.optionalFieldOf("taskId", 0L).forGetter(TaskScreenSaveData::taskId),
                ItemStack.CODEC.optionalFieldOf("skin", ItemStack.EMPTY).forGetter(TaskScreenSaveData::skin),
                Codec.BOOL.optionalFieldOf("indestructible", false).forGetter(TaskScreenSaveData::indestructible),
                Codec.BOOL.optionalFieldOf("input_only", false).forGetter(TaskScreenSaveData::inputOnly),
                ItemStack.CODEC.optionalFieldOf("skin", ItemStack.EMPTY).forGetter(TaskScreenSaveData::inputModeIcon),
                Codec.BOOL.optionalFieldOf("text_shadow", false).forGetter(TaskScreenSaveData::textShadow)
        ).apply(builder, TaskScreenSaveData::new));

        public static StreamCodec<RegistryFriendlyByteBuf, TaskScreenSaveData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG, TaskScreenSaveData::taskId,
                ItemStack.OPTIONAL_STREAM_CODEC, TaskScreenSaveData::skin,
                ByteBufCodecs.BOOL, TaskScreenSaveData::indestructible,
                ByteBufCodecs.BOOL, TaskScreenSaveData::inputOnly,
                ItemStack.OPTIONAL_STREAM_CODEC, TaskScreenSaveData::inputModeIcon,
                ByteBufCodecs.BOOL, TaskScreenSaveData::textShadow,
                TaskScreenSaveData::new
        );
    }
}
