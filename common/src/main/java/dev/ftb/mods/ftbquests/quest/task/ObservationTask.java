package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.registry.registries.Registries;
import com.mojang.brigadier.StringReader;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * @author LatvianModder
 */
public class ObservationTask extends BooleanTask {
	public long timer;
	public ObserveType observeType;
	public String toObserve;

	public ObservationTask(Quest quest) {
		super(quest);
		timer = 0L;
		observeType = ObserveType.BLOCK;
		toObserve = "minecraft:dirt";
	}

	@Override
	public TaskType getType() {
		return TaskTypes.OBSERVATION;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putLong("timer", timer);
		nbt.putInt("observe_type", observeType.ordinal());
		nbt.putString("to_observe", toObserve);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		timer = nbt.getLong("timer");
		observeType = ObserveType.values()[nbt.getInt("observe_type")];
		toObserve = nbt.getString("to_observe");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeVarLong(timer);
		buffer.writeEnum(observeType);
		buffer.writeUtf(toObserve);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		timer = buffer.readVarLong();
		observeType = buffer.readEnum(ObserveType.class);
		toObserve = buffer.readUtf(Short.MAX_VALUE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addLong("timer", timer, v -> timer = v, 0L, 0L, 1200L);
		config.addEnum("observe_type", observeType, v -> observeType = v, ObserveType.NAME_MAP);
		config.addString("to_observe", toObserve, v -> toObserve = v, "minecraft:dirt");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return true;
	}

	@Override
	public boolean checkOnLogin() {
		return false;
	}

	public boolean observe(Player player, HitResult result) {
		if (toObserve.isEmpty()) {
			return false;
		}

		if (result instanceof BlockHitResult) {
			BlockHitResult blockResult = (BlockHitResult) result;
			BlockInWorld blockInWorld = new BlockInWorld(player.level, blockResult.getBlockPos(), false);

			BlockState state = blockInWorld.getState();
			Block block = state.getBlock();
			BlockEntity blockEntity = blockInWorld.getEntity();

			switch (observeType) {
				case BLOCK:
					return String.valueOf(Registries.getId(block, Registry.BLOCK_REGISTRY)).equals(toObserve);
				case BLOCK_TAG:
					return state.is(TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(toObserve)));
				case BLOCK_STATE:
					BlockInput stateMatch = tryMatchBlock(toObserve, false);
					return stateMatch != null && stateMatch.test(blockInWorld);
				case BLOCK_ENTITY:
					BlockInput stateNbtMatch = tryMatchBlock(toObserve, true);
					return stateNbtMatch != null && stateNbtMatch.test(blockInWorld);
				case BLOCK_ENTITY_TYPE:
					return blockEntity != null && toObserve.equals(String.valueOf(Registries.getId(blockEntity.getType(), Registry.BLOCK_ENTITY_TYPE_REGISTRY)));
				default:
					return false;
			}
		} else if (result instanceof EntityHitResult) {
			EntityHitResult entityResult = (EntityHitResult) result;
			if (observeType == ObserveType.ENTITY_TYPE) {
				return toObserve.equals(String.valueOf(Registries.getId(entityResult.getEntity().getType(), Registry.ENTITY_TYPE_REGISTRY)));
			} else if (observeType == ObserveType.ENTITY_TYPE_TAG) {
				return entityResult.getEntity().getType().is(TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(toObserve)));
			}
		}

		return false;
	}

	private BlockInput tryMatchBlock(String string, boolean parseNbt) {
		try {
			BlockStateParser blockStateParser = (new BlockStateParser(new StringReader(string), false)).parse(parseNbt);
			return new BlockInput(blockStateParser.getState(), blockStateParser.getProperties().keySet(), blockStateParser.getNbt());
		} catch (Exception ex) {
			return null;
		}
	}

	enum ObserveType {
		BLOCK,
		BLOCK_TAG,
		BLOCK_STATE,
		BLOCK_ENTITY,
		BLOCK_ENTITY_TYPE,
		ENTITY_TYPE,
		ENTITY_TYPE_TAG;

		public static final NameMap<ObserveType> NAME_MAP = NameMap.of(BLOCK, values()).id(v -> v.name().toLowerCase()).create();
	}
}