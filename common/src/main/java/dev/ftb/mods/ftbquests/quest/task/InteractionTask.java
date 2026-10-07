package dev.ftb.mods.ftbquests.quest.task;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
    public void readData(Json5Object json, HolderLookup.Provider provider) {
        super.readData(json, provider);

        interactionType = InteractionType.NAME_MAP.get(Json5Util.getString(json, "interaction_type").orElseThrow());
        toInteract = Json5Util.getString(json,"to_interact").orElseThrow();
    }

    @Override
    public void writeData(Json5Object json, HolderLookup.Provider provider) {
        super.writeData(json, provider);

        json.addProperty("interaction_type", InteractionType.NAME_MAP.getName(interactionType));
        json.addProperty("to_interact", toInteract);
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
    public void fillConfigGroup(EditableConfigGroup config) {
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
    public void onEditButtonClicked(Runnable gui, Component title) {
        super.onEditButtonClicked(gui, title);
    }

    @Override
    public boolean checkOnLogin() {
        return false;
    }

    @Override
    public TaskClient client() {
        return InteractionTaskClient.INSTANCE;
    }

    public void interacted(TeamData data, Level level, BlockPos pos) {
        if (data.isCompleted(this)) {
            return;
        }

        BlockInWorld blockInWorld = new BlockInWorld(level, pos, false);
        BlockState blockState = level.getBlockState(pos);

        boolean matched = switch (interactionType) {
            case BLOCK -> BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString().equals(toInteract);
            case BLOCK_TAG ->
                    ObservationTask.asTagId(toInteract)
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
                    ObservationTask.asTagId(toInteract)
                            .map(rl -> entity.is(TagKey.create(Registries.ENTITY_TYPE, rl)))
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
