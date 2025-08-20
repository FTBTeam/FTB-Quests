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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.ftb.mods.ftbquests.block.QuestBarrierBlock.OPEN;

public abstract class BaseBarrierBlockEntity extends EditableBlockEntity {
	protected String objStr = "";
	protected BlockState camo = null;
	private boolean invisibleWhenOpen = false;
	private ItemStack skin = ItemStack.EMPTY;

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

	public record BarrierSavedData(String objStr, ItemStack skin, boolean invisibleWhenOpen) {
		public static final BarrierSavedData DEFAULT = new BarrierSavedData("", ItemStack.EMPTY, false);

		public static BarrierSavedData fromBlockEntity(BaseBarrierBlockEntity b) {
			return new BarrierSavedData(b.objStr, b.skin, b.invisibleWhenOpen);
		}

		public static final Codec<BarrierSavedData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				Codec.STRING.optionalFieldOf("object", "").forGetter(BarrierSavedData::objStr),
				ItemStack.CODEC.optionalFieldOf("skin", ItemStack.EMPTY).forGetter(BarrierSavedData::skin),
				Codec.BOOL.optionalFieldOf("invis_when_open", false).forGetter(BarrierSavedData::invisibleWhenOpen)
		).apply(builder, BarrierSavedData::new));

		public static StreamCodec<RegistryFriendlyByteBuf, BarrierSavedData> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, BarrierSavedData::objStr,
				ItemStack.OPTIONAL_STREAM_CODEC, BarrierSavedData::skin,
				ByteBufCodecs.BOOL, BarrierSavedData::invisibleWhenOpen,
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
}
