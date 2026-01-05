package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.util.Lazy;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KillTask extends Task {
	private static final ResourceLocation ZOMBIE = ResourceLocation.withDefaultNamespace("zombie");

	private static final Lazy<NameMap<ResourceLocation>> entityNameMap = Lazy.of(KillTask::scanEntityTypes);
	private static final Lazy<NameMap<String>> entityTagMap = Lazy.of(KillTask::scanEntityTags);

	private ResourceLocation entityTypeId = ZOMBIE;
	private TagKey<EntityType<?>> entityTypeTag = null;
	private long value = 100L;
	private String customName = "";
	private static final Map<ResourceLocation,Icon> entityIcons = new HashMap<>();

	public KillTask(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.KILL;
	}

	@Override
	public long getMaxProgress() {
		return value;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.putString("entity", entityTypeId.toString());
		if (entityTypeTag != null) nbt.putString("entityTypeTag", entityTypeTag.location().toString());
		nbt.putLong("value", value);
		if (!customName.isEmpty()) nbt.putString("custom_name", customName);
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		entityTypeId = ResourceLocation.tryParse(nbt.getString("entity"));
		entityTypeTag = parseTypeTag(nbt.getString("entityTypeTag"));
		value = nbt.getLong("value");
		customName = nbt.getString("custom_name");
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(entityTypeId.toString());
		buffer.writeUtf(entityTypeTag == null ? "" : entityTypeTag.location().toString());
		buffer.writeVarLong(value);
		buffer.writeUtf(customName);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		entityTypeId = ResourceLocation.tryParse(buffer.readUtf());
		entityTypeTag = parseTypeTag(buffer.readUtf());
		value = buffer.readVarInt();
		customName = buffer.readUtf();
	}

	private static @Nullable TagKey<EntityType<?>> parseTypeTag(String tag) {
		if (tag == null || tag.isEmpty()) {
			return null;
		}
		if (tag.startsWith("#")) {
			tag = tag.substring(1);
		}
		ResourceLocation rl = ResourceLocation.tryParse(tag);
        return rl == null || rl.getPath().isEmpty() ? null : TagKey.create(Registries.ENTITY_TYPE, rl);
    }

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		config.addEnum("entity", entityTypeId, v -> entityTypeId = v, entityNameMap.get(), ZOMBIE);
		config.addEnum("entity_type_tag", getTypeTagStr(), v -> entityTypeTag = parseTypeTag(v), entityTagMap.get());
		config.addLong("value", value, v -> value = v, 100L, 1L, Long.MAX_VALUE);
		config.addString("custom_name", customName, v -> customName = v, "");
	}

	private String getTypeTagStr() {
		return entityTypeTag == null ? "" : entityTypeTag.location().toString();
	}

	private static Icon getIconForEntityType(ResourceLocation typeId) {
		return entityIcons.computeIfAbsent(typeId, k -> {
			EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(typeId);
			if (entityType.equals(EntityType.PLAYER)) {
				return Icons.PLAYER;
			}
			Item item = SpawnEggItem.byId(entityType);
			if (item == null) {
				Entity e = entityType.create(FTBQuestsClient.getClientLevel());
				if (e != null) {
					ItemStack stack = e.getPickResult();
					if (stack != null) item = stack.getItem();
				}
			}
			return ItemIcon.getItemIcon(item != null ? item : Items.SPAWNER);
		});
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();

		entityNameMap.invalidate();
		entityTagMap.invalidate();
		entityIcons.clear();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		MutableComponent name = entityTypeTag == null ?
				Component.translatable("entity." + entityTypeId.toLanguageKey()) :
				Component.literal("#" + getTypeTagStr());
		if (!customName.isEmpty()) {
			return Component.translatable("ftbquests.task.ftbquests.kill.title_named", formatMaxProgress(), name, Component.literal(customName));
		} else {
			return Component.translatable("ftbquests.task.ftbquests.kill.title", formatMaxProgress(), name);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		if (entityTypeTag == null) {
			return getIconForEntityType(entityTypeId);
		}

		List<Icon> icons = new ArrayList<>();
		BuiltInRegistries.ENTITY_TYPE.getTag(entityTypeTag)
				.ifPresent(set ->
						set.forEach(holder ->
								holder.unwrapKey().map(k -> icons.add(getIconForEntityType(k.location())))
						)
				);
		return icons.isEmpty() ? Icons.BARRIER : IconAnimation.fromList(icons, false);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
	}

	public void kill(TeamData teamData, LivingEntity e) {
		if (!teamData.isCompleted(this) && match(e)) {
			teamData.addProgress(this, 1L);
		}
	}

	private boolean match(LivingEntity e) {
		return entityTypeTag == null ?
				entityTypeId.equals(RegistrarManager.getId(e.getType(), Registries.ENTITY_TYPE)) && nameMatchOK(e) :
				e.getType().is(entityTypeTag) && nameMatchOK(e);
	}

	private boolean nameMatchOK(LivingEntity e) {
		return customName.isEmpty() ||
				(e instanceof Player p ?
						p.getGameProfile().getName().equals(customName) :
						e.getName().getString().equals(customName));
	}

	private static @NotNull NameMap<ResourceLocation> scanEntityTypes() {
		List<ResourceLocation> ids = new ArrayList<>();
		BuiltInRegistries.ENTITY_TYPE.forEach(type -> {
			try {
				if (type.create(FTBQuestsClient.getClientLevel()) instanceof LivingEntity) {
					ids.add(type.arch$registryName());
				}
			} catch (Exception e) {
				FTBQuests.LOGGER.warn("Entity creation failed during kill task scanning for {}: {}", type.arch$registryName(), e.getMessage());
			}
		});
		ids.sort((r1, r2) -> {
			Component c1 = Component.translatable("entity." + r1.toLanguageKey());
			Component c2 = Component.translatable("entity." + r2.toLanguageKey());
			return c1.getString().compareTo(c2.getString());
		});
		return NameMap.of(ZOMBIE, ids)
				.name(id -> Component.translatable("entity." + id.toLanguageKey())
						.append(Component.empty().withStyle(ChatFormatting.GRAY).append(" [").append(Component.literal(id.toString())).append("]"))
				)
				.icon(KillTask::getIconForEntityType)
				.create();
	}

	private static @NotNull NameMap<String> scanEntityTags() {
		List<String> tags = new ArrayList<>(List.of(""));
		tags.addAll(BuiltInRegistries.ENTITY_TYPE.getTags().map(pair -> pair.getFirst().location().toString()).sorted().toList());
		return NameMap.of("minecraft:zombies", tags).create();
	}
}
