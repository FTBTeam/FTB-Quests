package dev.ftb.mods.ftbquests.quest.task;

import com.mojang.datafixers.util.Either;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.tags.TagKey;

/**
 * @author MaxNeedsSnacks
 */
public class StructureTask extends BooleanTask {
	// FIXME port to 1.19
	private static final ResourceLocation DEFAULT_STRUCTURE = new ResourceLocation("minecraft:mineshaft");

	private Either<ResourceKey<ConfiguredStructureFeature<?, ?>>, TagKey<ConfiguredStructureFeature<?, ?>>> structure;

	public StructureTask(Quest quest) {
		super(quest);
		structure = Either.left(ResourceKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DEFAULT_STRUCTURE));
	}

	@Override
	public TaskType getType() {
		return TaskTypes.STRUCTURE;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("structure", getStructure());
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		setStructure(nbt.getString("structure"));
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(getStructure());
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		setStructure(buffer.readUtf(1024));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("structure", getStructure(), this::setStructure, "minecraft:mineshaft");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.task.ftbquests.structure")
				.append(": ").append(new TextComponent(getStructure()).withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 20;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level;
		return structure.map(
				key -> level.structureFeatureManager().getStructureWithPieceAt(player.blockPosition(), key).isValid(),
				tag -> {
					var reg = level.registryAccess().registry(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).orElseThrow();
					for (var holder : reg.getTagOrEmpty(tag)) {
						if (level.structureFeatureManager().getStructureWithPieceAt(player.blockPosition(), holder.value()).isValid()) {
							return true;
						}
					}
					return false;
				}
		);
	}

	private void setStructure(String resLoc) {
		structure = resLoc.startsWith("#") ?
				Either.right(TagKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, safeResourceLocation(resLoc.substring(1)))) :
				Either.left(ResourceKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, safeResourceLocation(resLoc)));
	}

	private String getStructure() {
		return structure.map(structure -> structure.location().toString(), tag -> "#" + tag.location().toString());
	}

	private ResourceLocation safeResourceLocation(String str) {
		try {
			return new ResourceLocation(str);
		} catch (ResourceLocationException e) {
			if (getQuestFile().isServerSide()) {
				FTBQuests.LOGGER.warn("Ignoring bad structure resource location '{}' for structure task {}", str, id);
			} else {
				FTBQuests.PROXY.getClientPlayer().displayClientMessage(
						new TextComponent("Ignoring bad structure resource location: " + str).withStyle(ChatFormatting.RED), false);
			}
			return DEFAULT_STRUCTURE;
		}
	}

}
