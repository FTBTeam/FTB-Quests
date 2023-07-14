package dev.ftb.mods.ftbquests.quest.task;

import com.mojang.datafixers.util.Either;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class BiomeTask extends BooleanTask {
	private static final ResourceKey<Biome> DEFAULT_BIOME = Biomes.PLAINS;

	private static final List<String> KNOWN_BIOMES = new ArrayList<>();

	private Either<ResourceKey<Biome>, TagKey<Biome>> biome;

	public BiomeTask(Quest quest) {
		super(quest);
		biome = Either.left(DEFAULT_BIOME);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.BIOME;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("biome", getBiome());
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		setBiome(nbt.getString("biome"));
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(getBiome());
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		setBiome(buffer.readUtf(Short.MAX_VALUE));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addEnum("biome", getBiome(), this::setBiome, NameMap.of(DEFAULT_BIOME.location().toString(), getKnownBiomes()).create());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.task.ftbquests.biome").append(": ")
				.append(Component.literal(getBiome())).withStyle(ChatFormatting.DARK_GREEN);
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 20;
	}

	@Override
	public boolean checkOnLogin() {
		// checking world-related stuff on player login can cause issues
		return false;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		if (player.isSpectator()) return false;

		Holder<Biome> biomeHolder = player.level().getBiome(player.blockPosition());
		return biome.map(
				key -> biomeHolder.unwrapKey().map(k -> k == key).orElse(false),
				tagKey -> {
					var reg = player.level().registryAccess().registry(Registries.BIOME).orElseThrow();
					return reg.getTag(tagKey).map(holderSet -> holderSet.contains(biomeHolder)).orElse(false);
				}
		);
	}

	private String getBiome() {
		return biome.map(
				key -> key.location().toString(),
				tagKey -> "#" + tagKey.location()
		);
	}

	private void setBiome(String str) {
		biome = str.startsWith("#") ?
				Either.right(TagKey.create(Registries.BIOME, safeResourceLocation(str.substring(1), DEFAULT_BIOME.location()))) :
				Either.left(ResourceKey.create(Registries.BIOME, safeResourceLocation(str, DEFAULT_BIOME.location())));
	}

	private List<String> getKnownBiomes() {
		if (KNOWN_BIOMES.isEmpty()) {
			RegistryAccess registryAccess = FTBQuests.PROXY.getClientPlayer().level().registryAccess();
			KNOWN_BIOMES.addAll(registryAccess
					.registryOrThrow(Registries.BIOME).registryKeySet().stream()
					.map(o -> o.location().toString())
					.sorted(String::compareTo)
					.toList()
			);
			KNOWN_BIOMES.addAll(registryAccess
					.registryOrThrow(Registries.BIOME).getTagNames()
					.map(o -> "#" + o.location())
					.sorted(String::compareTo)
					.toList()
			);
		}
		return KNOWN_BIOMES;
	}
}
