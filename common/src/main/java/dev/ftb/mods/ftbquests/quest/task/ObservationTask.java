package dev.ftb.mods.ftbquests.quest.task;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ObservationTask extends AbstractBooleanTask {
	// Max server ticks between two matching samples for them to count as continuous observation
	private static final long MAX_SAMPLE_GAP_TICKS = 10L;
	// Minimum tolerance for network timing jitter when comparing accumulated observation time with the timer
	private static final long JITTER_TICKS = 2L;

	private final Map<UUID, ObserveState> observeStates = new HashMap<>();

	// cached result of targetsFluid(), valid for the observe type & string it was computed for
	private ObserveType fluidCheckType = null;
	private String fluidCheckStr = null;
	private boolean fluidTarget = false;

	private long timer;
	private ObserveType observeType;
	private String toObserve;

	public ObservationTask(long id, Quest quest) {
		super(id, quest);

		timer = 0L;
		observeType = ObserveType.BLOCK;
		toObserve = "minecraft:dirt";
	}

	public void setToObserve(String toObserve) {
		this.toObserve = toObserve;
	}

	public long getTimer() {
		return timer;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.OBSERVATION;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.putLong("timer", timer);
		nbt.putString("observation_type", ObserveType.NAME_MAP.getName(observeType));
		nbt.putInt("observe_type", observeType.ordinal());
		nbt.putString("to_observe", toObserve);
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		timer = nbt.getLong("timer");
		if (nbt.contains("observe_type")) {
			// legacy - remove in 26.1
			observeType = ObserveType.values()[nbt.getInt("observe_type")];
		} else {
			observeType = ObserveType.NAME_MAP.get(nbt.getString("observation_type"));
		}
		toObserve = nbt.getString("to_observe");
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeVarLong(timer);
		buffer.writeEnum(observeType);
		buffer.writeUtf(toObserve);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		timer = buffer.readVarLong();
		observeType = buffer.readEnum(ObserveType.class);
		toObserve = buffer.readUtf(Short.MAX_VALUE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addLong("timer", timer, v -> timer = v, 0L, 0L, 1200L);
		config.addEnum("observe_type", observeType, v -> observeType = v, ObserveType.NAME_MAP);
		config.addString("to_observe", toObserve, v -> toObserve = v, "minecraft:dirt");
	}

	@Override
	public Component getAltTitle() {
		return Component.translatable("ftbquests.task.ftbquests.observation").append(": ")
				.append(Component.literal(toObserve).withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		// The client sends a submit packet periodically while it believes the player is observing this task. The server
		// samples what the player is really looking at only when such a packet arrives (so no per-tick cost for players
		// who aren't observing), and credits the elapsed *server* time between consecutive matching samples. Spamming
		// packets therefore can't speed up the timer, and the target must really be under the crosshair when sampled.
		long now = player.level().getGameTime();
		ObserveState state = observeStates.computeIfAbsent(player.getUUID(), k -> new ObserveState());

		if (state.lastSampleTick != now) {
			state.lastSampleTick = now;
			if (observe(player, getServerHitResult(player, observeType.isEntityType(), targetsFluid()))) {
				if (state.progress == 0L || now - state.lastMatchTick > MAX_SAMPLE_GAP_TICKS) {
					state.progress = 1L;
				} else {
					state.progress += now - state.lastMatchTick;
				}
				state.lastMatchTick = now;
			} else if (now - state.lastMatchTick > MAX_SAMPLE_GAP_TICKS) {
				// a single missed sample is tolerated (client/server aim can differ slightly at the edge of a target)
				state.progress = 0L;
			}
		}

		// allow for network timing jitter and for the server running slightly slower than 20 TPS, which makes
		// server-tick credit lag behind the client's own tick count
		long tolerance = Math.max(JITTER_TICKS, timer / 10L);
		if (state.progress > 0L && state.progress + tolerance >= timer && now - state.lastMatchTick <= MAX_SAMPLE_GAP_TICKS) {
			observeStates.remove(player.getUUID());
			return true;
		}
		return false;
	}

	private static HitResult getServerHitResult(ServerPlayer player, boolean wantEntity, boolean wantFluid) {
		Vec3 eye = player.getEyePosition();
		Vec3 view = player.getViewVector(1.0F);
		double blockReach = player.blockInteractionRange();
		double entityReach = player.entityInteractionRange();

		if (wantFluid) {
			return pickIncludingFluids(player);
		}

		// the block pick is always needed: for entity tasks it also tells us whether a block is in the way
		HitResult hit = player.pick(blockReach, 1.0F, false);
		if (!wantEntity) {
			return hit;
		}

		double maxDistSqr = Math.max(blockReach, entityReach);
		maxDistSqr *= maxDistSqr;
		if (hit.getType() != HitResult.Type.MISS) {
			maxDistSqr = hit.getLocation().distanceToSqr(eye);
		}

		Vec3 end = eye.add(view.scale(entityReach));
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player.level(), player, eye, end,
				player.getBoundingBox().expandTowards(view.scale(entityReach)).inflate(1.0),
				e -> !e.isSpectator() && e.isPickable(), 0.0F);
		if (entityHit != null && entityHit.getLocation().distanceToSqr(eye) < maxDistSqr
				&& entityHit.getLocation().distanceToSqr(eye) <= entityReach * entityReach) {
			hit = entityHit;
		}

		return hit;
	}

	/**
	 * The vanilla crosshair target (as used by the client) ignores fluids, so for tasks which target a fluid block, both
	 * client and server must use this explicit fluid-inclusive pick instead.
	 */
	public static HitResult pickIncludingFluids(Player player) {
		return player.pick(player.blockInteractionRange(), 1.0F, true);
	}

	/**
	 * {@return true if this task is known to be looking for a fluid block, i.e. a block id or block state which is a liquid}
	 * Block tags are not considered, since vanilla has no tags for fluid blocks.
	 */
	public boolean targetsFluid() {
		if (fluidCheckType != observeType || !toObserve.equals(fluidCheckStr)) {
			fluidCheckType = observeType;
			fluidCheckStr = toObserve;
			fluidTarget = computeTargetsFluid();
		}
		return fluidTarget;
	}

	private boolean computeTargetsFluid() {
		return switch (observeType) {
			case BLOCK -> {
				var id = ResourceLocation.tryParse(toObserve);
				yield id != null && BuiltInRegistries.BLOCK.getOptional(id).map(b -> b instanceof LiquidBlock).orElse(false);
			}
			case BLOCK_STATE -> {
				BlockInput input = tryMatchBlock(toObserve, false);
				yield input != null && input.getState().getBlock() instanceof LiquidBlock;
			}
			default -> false;
		};
	}

	private static class ObserveState {
		long lastSampleTick = -1L;
		long lastMatchTick = -1L;
		long progress = 0L;
	}

	public boolean observe(Player player, HitResult result) {
		if (toObserve.isEmpty()) {
			return false;
		}

		if (result instanceof BlockHitResult blockResult) {
			BlockInWorld blockInWorld = new BlockInWorld(player.level(), blockResult.getBlockPos(), false);

			BlockState state = blockInWorld.getState();
			if (state == null) {
				return false;
			}

			Block block = state.getBlock();
			BlockEntity blockEntity = blockInWorld.getEntity();

			switch (observeType) {
				case BLOCK -> {
					return String.valueOf(RegistrarManager.getId(block, Registries.BLOCK)).equals(toObserve);
				}
				case BLOCK_TAG -> {
					return asTagRL(toObserve)
							.map(rl -> state.is(TagKey.create(Registries.BLOCK, rl)))
							.orElse(false);
				}
				case BLOCK_STATE -> {
					BlockInput stateMatch = tryMatchBlock(toObserve, false);
					return stateMatch != null && stateMatch.test(blockInWorld);
				}
				case BLOCK_ENTITY -> {
					BlockInput stateNbtMatch = tryMatchBlock(toObserve, true);
					return stateNbtMatch != null && stateNbtMatch.test(blockInWorld);
				}
				case BLOCK_ENTITY_TYPE -> {
					return blockEntity != null && toObserve.equals(String.valueOf(RegistrarManager.getId(blockEntity.getType(), Registries.BLOCK_ENTITY_TYPE)));
				}
				default -> {
					return false;
				}
			}
		} else if (result instanceof EntityHitResult entityResult) {
			if (observeType == ObserveType.ENTITY_TYPE) {
				return tryMatchEntity(toObserve, entityResult.getEntity());
			} else if (observeType == ObserveType.ENTITY_TYPE_TAG) {
				return asTagRL(toObserve)
						.map(rl -> entityResult.getEntity().getType().is(TagKey.create(Registries.ENTITY_TYPE, rl)))
						.orElse(false);
			}
		}

		return false;
	}

    static boolean tryMatchEntity(String matchAgainst, Entity entity) {
		ResourceLocation entityType = RegistrarManager.getId(entity.getType(), Registries.ENTITY_TYPE);
		if (entityType == null) return false;

		int nbtStart = matchAgainst.indexOf('{');
		int nbtEnd = nbtStart > 0 ? matchAgainst.indexOf('}', nbtStart) : -1;

		String entityId = nbtStart < 0 ? matchAgainst : matchAgainst.substring(0, nbtStart);
		String nbtFilter = nbtStart > 0 && nbtEnd > nbtStart ? matchAgainst.substring(nbtStart, nbtEnd + 1) : "";

		return entityType.toString().equals(entityId) && matchEntityNBT(entity, nbtFilter);
    }

	static boolean matchEntityNBT(Entity entity, String nbtFilter) {
        if (nbtFilter.isEmpty()) return true;
		if (!nbtFilter.startsWith("{")) {
			nbtFilter = "{" + nbtFilter + "}";
		}

		try {
			var filterTag = TagParser.parseTag(nbtFilter);
			CompoundTag entityTag = Util.make(new CompoundTag(), entity::saveWithoutId);
			return NbtUtils.compareNbt(filterTag, entityTag, true);
		} catch (CommandSyntaxException e) {
			return false;
		}
    }

	static Optional<ResourceLocation> asTagRL(String str) {
		try {
			return Optional.ofNullable(ResourceLocation.tryParse(str.startsWith("#") ? str.substring(1) : str));
		} catch (ResourceLocationException e) {
			return Optional.empty();
		}
	}

	static BlockInput tryMatchBlock(String matchAgainst, boolean parseNbt) {
		try {
			BlockStateParser.BlockResult blockStateParser = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), new StringReader(matchAgainst), parseNbt);
			return new BlockInput(blockStateParser.blockState(), blockStateParser.properties().keySet(), parseNbt ? blockStateParser.nbt() : null);
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

		boolean isEntityType() {
			return this == ENTITY_TYPE || this == ENTITY_TYPE_TAG;
		}

		public static final NameMap<ObserveType> NAME_MAP = NameMap.of(BLOCK, values()).id(v -> v.name().toLowerCase()).create();
	}
}
