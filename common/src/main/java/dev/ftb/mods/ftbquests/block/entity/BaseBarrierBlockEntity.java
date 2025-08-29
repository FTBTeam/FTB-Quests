package dev.ftb.mods.ftbquests.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.QuestBarrierBlock;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.net.BlockConfigResponseMessage;
import dev.ftb.mods.ftbquests.registry.ModBlocks;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static dev.ftb.mods.ftbquests.block.QuestBarrierBlock.OPEN;

public abstract class BaseBarrierBlockEntity extends EditableBlockEntity {
	protected String objStr = "";
	protected BlockState camo = null;
	private boolean invisibleWhenOpen = false;
	private ItemStack skin = ItemStack.EMPTY;
	private TeleportData teleportData = TeleportData.NONE;

	public BaseBarrierBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	public static void tick(Level level, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
		if (blockEntity instanceof BaseBarrierBlockEntity barrier) {
			if (level.isClientSide && FTBQuestsClient.isClientDataLoaded() && level.getGameTime() % 5L == 0L) {
				boolean completed = barrier.isOpen(FTBQuestsClient.getClientPlayer());

				if (completed != blockState.getValue(OPEN)) {
					level.setBlock(blockPos, blockState.setValue(OPEN, completed), Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
					blockEntity.setChanged();
					barrier.forceAppearanceUpdate();
				}
			}
		}
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);

		if (tag.contains("Object")) {
			// TODO legacy - remove in 1.22
			objStr = tag.getString("Object");
			skin = ItemStack.EMPTY;
		} else {
			BarrierSavedData data = BarrierSavedData.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("savedData"))
					.result().orElse(BarrierSavedData.DEFAULT);
			applySavedData(data);
		}
	}

	@Override
	public void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);

		BarrierSavedData.CODEC.encodeStart(NbtOps.INSTANCE, BarrierSavedData.fromBlockEntity(this))
				.ifSuccess(tag -> compoundTag.put("savedData", tag));
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput dataComponentInput) {
		super.applyImplicitComponents(dataComponentInput);

		applySavedData(dataComponentInput.getOrDefault(ModDataComponents.BARRIER_SAVED.get(), BarrierSavedData.DEFAULT));
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);

		builder.set(ModDataComponents.BARRIER_SAVED.get(), BarrierSavedData.fromBlockEntity(this));
	}

	protected void applySavedData(BarrierSavedData data) {
		objStr = data.objStr;
		setSkin(data.skin);
		setInvisibleWhenOpen(data.invisibleWhenOpen);
		teleportData = data.teleportData.orElse(TeleportData.NONE);
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
	public void setChanged() {
		if (level instanceof ServerLevel) level.blockEntityChanged(getBlockPos());
	}

	public void updateFromString(String objStr) {
		this.objStr = objStr;
		setChanged();
	}

	public abstract boolean isOpen(Player player);

	protected abstract void addConfigEntries(ConfigGroup cg);

	public ConfigGroup fillConfigGroup() {
		ConfigGroup group = new ConfigGroup("ftbquests.barrier", accepted -> {
			if (accepted) {
				NetworkManager.sendToServer(new BlockConfigResponseMessage(getBlockPos(), saveWithoutMetadata(getLevel().registryAccess())));
			}
		});

		group.setNameKey(getBlockState().getBlock().getDescriptionId());
		addConfigEntries(group);

		if (Platform.isForgeLike()) {
			ConfigGroup appearance = group.getOrCreateSubgroup("appearance").setNameKey("ftbquests.quest.appearance");
			appearance.add("skin", new ItemStackConfig(true, true), getSkin(), this::setSkin, ItemStack.EMPTY)
					.withFilter(stack -> stack.getItem() instanceof BlockItem)
					.setNameKey("block.ftbquests.screen.skin");
			appearance.addBool("invis_when_open", isInvisibleWhenOpen(), this::setInvisibleWhenOpen, false).setNameKey("block.ftbquests.barrier.invis_when_open");
		}

		ConfigGroup teleport = group.getOrCreateSubgroup("teleport").setNameKey("ftbquests.barrier.teleport");
		teleport.addBool("enabled", teleportData.enabled, v -> teleportData = teleportData.withEnabled(v), false);
		teleport.addBool("relative", teleportData.relative, v -> teleportData = teleportData.withRelative(v), false);
		teleport.addInt("dest_x", teleportData.dest.getX(), v -> teleportData = teleportData.withDestX(v), 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		teleport.addInt("dest_y", teleportData.dest.getY(), v -> teleportData = teleportData.withDestY(v), 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		teleport.addInt("dest_z", teleportData.dest.getZ(), v -> teleportData = teleportData.withDestZ(v), 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		teleport.addDouble("pitch", teleportData.pitch, v -> teleportData = teleportData.withPitch(v.floatValue()), 0.0, -90.0, 90.0);
		teleport.addDouble("yaw", teleportData.yaw, v -> teleportData = teleportData.withYaw(v.floatValue()), 0.0, 0.0, 360.0);
		teleport.addString("dimension", teleportData.dimStr(), v -> teleportData = teleportData.withDimId(v), "");

		return group;
	}

	public ItemStack getSkin() {
		return skin;
	}

	public void setSkin(ItemStack skin) {
		this.skin = skin;
		forceAppearanceUpdate();
		setChanged();
	}

	public boolean isInvisibleWhenOpen() {
		return invisibleWhenOpen;
	}

	public void setInvisibleWhenOpen(boolean invisibleWhenOpen) {
		this.invisibleWhenOpen = invisibleWhenOpen;
		forceAppearanceUpdate();
		setChanged();
	}

	public void forceAppearanceUpdate() {
		camo = null;
	}

	public BlockState getClientAppearance() {
		if (camo == null) {
			if (isOpen(FTBQuestsClient.getClientPlayer())) {
				camo = invisibleWhenOpen ?
						Blocks.AIR.defaultBlockState() :
						ModBlocks.BARRIER.get().defaultBlockState().setValue(QuestBarrierBlock.OPEN, true);
			} else {
				camo = skin.getItem() instanceof BlockItem bi ?
						bi.getBlock().defaultBlockState() :
						ModBlocks.BARRIER.get().defaultBlockState().setValue(QuestBarrierBlock.OPEN, false);
			}
		}
		return camo;
	}

	@Override
	public boolean hasPermissionToEdit(Player player) {
		return FTBQuestsAPI.api().getQuestFile(level.isClientSide).getTeamData(player)
				.map(team -> team.getCanEdit(player))
				.orElse(false);
	}

	public Optional<TeleportData> optionalTeleportData() {
		return teleportData.equals(TeleportData.NONE) ? Optional.empty() : Optional.of(teleportData);
	}

	public record BarrierSavedData(String objStr, ItemStack skin, boolean invisibleWhenOpen, Optional<TeleportData> teleportData) {
		public static final BarrierSavedData DEFAULT
				= new BarrierSavedData("", ItemStack.EMPTY, false, Optional.empty());

		public static BarrierSavedData fromBlockEntity(BaseBarrierBlockEntity b) {
			return new BarrierSavedData(b.objStr, b.skin, b.invisibleWhenOpen, b.optionalTeleportData());
		}

		public static final Codec<BarrierSavedData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				Codec.STRING.optionalFieldOf("object", "").forGetter(BarrierSavedData::objStr),
				ItemStack.CODEC.optionalFieldOf("skin", ItemStack.EMPTY).forGetter(BarrierSavedData::skin),
				Codec.BOOL.optionalFieldOf("invis_when_open", false).forGetter(BarrierSavedData::invisibleWhenOpen),
				TeleportData.CODEC.optionalFieldOf("teleport").forGetter(BarrierSavedData::teleportData)
		).apply(builder, BarrierSavedData::new));

		public static StreamCodec<RegistryFriendlyByteBuf, BarrierSavedData> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, BarrierSavedData::objStr,
				ItemStack.OPTIONAL_STREAM_CODEC, BarrierSavedData::skin,
				ByteBufCodecs.BOOL, BarrierSavedData::invisibleWhenOpen,
				ByteBufCodecs.optional(TeleportData.STREAM_CODEC), BarrierSavedData::teleportData,
				BarrierSavedData::new
		);

		public void addTooltipInfo(BaseBarrierBlockEntity.BarrierSavedData data, List<Component> tooltip, String what) {
			tooltip.add(Component.translatable("item.ftbquests.barrier.object." + what, data.objStr().isEmpty() ? "-" : data.objStr).withStyle(ChatFormatting.GRAY));
			if (Platform.isForgeLike() && !data.skin().isEmpty()) {
				tooltip.add(Component.translatable("item.ftbquests.barrier.skin", data.skin().getDisplayName()).withStyle(ChatFormatting.GRAY));
			}
			tooltip.add(Component.translatable("item.ftbquests.barrier.invis_when_open", data.invisibleWhenOpen()).withStyle(ChatFormatting.GRAY));
		}
	}

	public record TeleportData(boolean enabled, boolean relative, BlockPos dest, float pitch, float yaw, Optional<ResourceKey<Level>> dimId) {
		public static final Codec<TeleportData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				Codec.BOOL.optionalFieldOf("enabled", false).forGetter(TeleportData::enabled),
				Codec.BOOL.optionalFieldOf("relative", false).forGetter(TeleportData::relative),
				BlockPos.CODEC.fieldOf("dest").forGetter(TeleportData::dest),
				Codec.FLOAT.fieldOf("pitch").forGetter(TeleportData::pitch),
				Codec.FLOAT.fieldOf("yaw").forGetter(TeleportData::yaw),
				ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dim_id").forGetter(TeleportData::dimId)
		).apply(builder, TeleportData::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, TeleportData> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.BOOL, TeleportData::enabled,
				ByteBufCodecs.BOOL, TeleportData::relative,
				BlockPos.STREAM_CODEC, TeleportData::dest,
				ByteBufCodecs.FLOAT, TeleportData::pitch,
				ByteBufCodecs.FLOAT, TeleportData::yaw,
				ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), TeleportData::dimId,
				TeleportData::new
		);
		public static final TeleportData NONE = new TeleportData(
				false, false, BlockPos.ZERO, 0f, 0f, Optional.empty()
		);

		public TeleportData withEnabled(boolean enabled) {
			return new TeleportData(enabled, relative, dest, pitch, yaw, dimId);
		}

		public TeleportData withRelative(boolean relative) {
			return new TeleportData(enabled, relative, dest, pitch, yaw, dimId);
		}

		public TeleportData withPitch(float pitch) {
			return new TeleportData(enabled, relative, dest, pitch, yaw, dimId);
		}

		public TeleportData withYaw(float yaw) {
			return new TeleportData(enabled, relative, dest, pitch, yaw, dimId);
		}

		public TeleportData withDimId(String dimStr) {
			return withDimId(dimStr.isEmpty() || ResourceLocation.tryParse(dimStr) == null ?
					null :
                    ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr)));
		}

		public TeleportData withDimId(@Nullable ResourceKey<Level> dimId) {
			return new TeleportData(enabled, relative, dest, pitch, yaw, Optional.ofNullable(dimId));
		}

		public TeleportData withDestPos(BlockPos pos) {
			return new TeleportData(enabled, relative, pos, pitch, yaw, dimId);
		}
		public TeleportData withDestX(int x) {
			return withDestPos(new BlockPos(x, dest.getY(), dest.getY()));
		}
		public TeleportData withDestY(int y) {
			return withDestPos(new BlockPos(dest.getX(), y, dest.getY()));
		}
		public TeleportData withDestZ(int z) {
			return withDestPos(new BlockPos(dest.getX(), dest.getY(), z));
		}

		public String dimStr() {
			return dimId.map(id -> id.location().toString()).orElse("");
		}

		@Nullable
		public Level getLevel(MinecraftServer server) {
			return dimId.map(server::getLevel).orElse(null);
		}

		public TeleportData effectiveDest(BlockPos basePos) {
			return relative ? this.withDestPos(basePos.offset(dest)) : this;
		}

		public void teleportPlayer(ServerPlayer player) {
            if (player != null && player.getServer() != null && enabled) {
                Vec3 dest = dest().getBottomCenter();
                Level destLevel = Objects.requireNonNullElse(getLevel(player.getServer()), player.level());
                if (destLevel instanceof ServerLevel serverLevel) {
                    player.teleportTo(serverLevel, dest.x, dest.y, dest.z, yaw(), pitch());
                    var sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_TELEPORT);
                    player.connection.send(new ClientboundSoundPacket(sound, SoundSource.PLAYERS, dest.x, dest.y, dest.z, 0.5f, 1f, serverLevel.getRandom().nextLong()));
                }
            }
        }
	}
}
