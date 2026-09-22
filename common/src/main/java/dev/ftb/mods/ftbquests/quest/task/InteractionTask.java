package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class InteractionTask extends Task {
    private InteractionType interactionType;
    private String toInteract;

    public InteractionTask(long id, Quest quest) {
        super(id, quest);

        interactionType = InteractionType.BLOCK;
        toInteract = "minecraft:dirt";
    }

    public void setToInteract(InteractionType interactionType, String toInteract) {
        this.interactionType = interactionType;
        this.toInteract = toInteract;
    }

    @Override
    public TaskType getType() {
        return TaskTypes.INTERACTION;
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);

        interactionType = InteractionType.NAME_MAP.get(nbt.getString("interaction_type"));
        toInteract = nbt.getString("to_interact");
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);

        nbt.putString("interaction_type", InteractionType.NAME_MAP.getName(interactionType));
        nbt.putString("to_interact", toInteract);
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);

        buffer.writeEnum(interactionType);
        buffer.writeUtf(toInteract);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);

        interactionType = buffer.readEnum(InteractionType.class);
        toInteract = buffer.readUtf();
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);

        config.addEnum("type", interactionType, v -> interactionType = v, InteractionType.NAME_MAP);
        config.addString("match", toInteract, v -> toInteract = v, "minecraft:dirt");
    }

    @Override
    public Component getAltTitle() {
        return Component.translatable("ftbquests.task.ftbquests.interaction").append(": ")
                .append(Component.literal(toInteract).withStyle(ChatFormatting.DARK_GREEN));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onButtonClicked(Button button, boolean canClick) {
    }

    @Override
    public boolean checkOnLogin() {
        return false;
    }

    public void interacted(TeamData data, Level level, BlockPos pos) {
        if (data.isCompleted(this)) {
            return;
        }

        BlockInWorld blockInWorld = new BlockInWorld(level, pos, false);
        BlockState blockState = level.getBlockState(pos);

        boolean matched = switch (interactionType) {
            case BLOCK ->
                    String.valueOf(RegistrarManager.getId(blockState.getBlock(), Registries.BLOCK)).equals(toInteract);
            case BLOCK_TAG ->
                    ObservationTask.asTagRL(toInteract)
                            .map(rl -> blockState.is(TagKey.create(Registries.BLOCK, rl)))
                            .orElse(false);
            case BLOCK_STATE -> {
                BlockInput stateMatch = ObservationTask.tryMatchBlock(toInteract, false);
                yield stateMatch != null && stateMatch.test(blockInWorld);
            }
            default -> false;
        };
        if (matched) {
            data.addProgress(this, 1L);
        }
    }

    public void interacted(TeamData data, Entity entity) {
        if (data.isCompleted(this)) {
            return;
        }

        boolean matched = switch (interactionType) {
            case ENTITY_TYPE ->
                    ObservationTask.tryMatchEntity(toInteract, entity);
            case ENTITY_TYPE_TAG ->
                    ObservationTask.asTagRL(toInteract)
                            .map(rl -> entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, rl)))
                            .orElse(false);
            default -> false;
        };
        if (matched) {
            data.addProgress(this, 1L);
        }
    }

    public enum InteractionType {
        BLOCK,
        BLOCK_TAG,
        BLOCK_STATE,
        ENTITY_TYPE,
        ENTITY_TYPE_TAG;

        public static final NameMap<InteractionType> NAME_MAP = NameMap.of(BLOCK, values()).id(v -> v.name().toLowerCase()).create();
    }
}
