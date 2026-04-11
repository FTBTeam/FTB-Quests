package dev.ftb.mods.ftbquests.quest.task;

import com.mojang.datafixers.util.Either;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

public class BiomeTask extends AbstractBooleanTask {
	private static final ResourceKey<Biome> DEFAULT_BIOME = Biomes.PLAINS;

	private static final List<String> KNOWN_BIOMES = new ArrayList<>();

	private Either<ResourceKey<Biome>, TagKey<Biome>> biome;

	public BiomeTask(long id, Quest quest) {
		super(id, quest);
		biome = Either.left(DEFAULT_BIOME);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.BIOME;
	}

	@Override
	public void writeData(@UnknownNullability Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);
		json.addProperty("biome", getBiome());
	}

	@Override
	public void readData(@UnknownNullability Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);
		setBiome(Json5Util.getString(json, "biome").orElseThrow());
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(getBiome());
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		setBiome(buffer.readUtf(Short.MAX_VALUE));
	}

	@Override
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);
		config.addEnum("biome", getBiome(), this::setBiome, NameMap.of(DEFAULT_BIOME.identifier().toString(), getKnownBiomes()).create());
	}

	@Override
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
					var reg = player.level().registryAccess().getOrThrow(Registries.BIOME).value();
					return reg.get(tagKey).map(holderSet -> holderSet.contains(biomeHolder)).orElse(false);
				}
		);
	}

	private String getBiome() {
		return biome.map(
				key -> key.identifier().toString(),
				tagKey -> "#" + tagKey.location()
		);
	}

	private void setBiome(String str) {
		biome = str.startsWith("#") ?
				Either.right(TagKey.create(Registries.BIOME, safeResourceLocation(str.substring(1), DEFAULT_BIOME.identifier()))) :
				Either.left(ResourceKey.create(Registries.BIOME, safeResourceLocation(str, DEFAULT_BIOME.identifier())));
	}

	private List<String> getKnownBiomes() {
		// only called client-side to fill the config screen options
		if (KNOWN_BIOMES.isEmpty()) {
			RegistryAccess registryAccess = ClientUtils.getClientPlayer().level().registryAccess();
			KNOWN_BIOMES.addAll(registryAccess
					.lookupOrThrow(Registries.BIOME).entrySet().stream()
					.map(e -> e.getKey().identifier().toString())
					.sorted(String::compareTo)
					.toList()
			);
			KNOWN_BIOMES.addAll(registryAccess
					.lookupOrThrow(Registries.BIOME).getTags()
					.map(o -> "#" + o.key().location())
					.sorted(String::compareTo)
					.toList()
			);
		}
		return KNOWN_BIOMES;
	}
}
