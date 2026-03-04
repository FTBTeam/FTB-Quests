package dev.ftb.mods.ftbquests.quest.task;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

import dev.architectury.registry.registries.RegistrarManager;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.icon.AnimatedIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.Lazy;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public class KillTask extends Task {
	private static final Identifier ZOMBIE = Identifier.withDefaultNamespace("zombie");

	private static final Lazy<NameMap<Identifier>> entityNameMap = Lazy.of(KillTask::scanEntityTypes);
	private static final Lazy<NameMap<String>> entityTagMap = Lazy.of(KillTask::scanEntityTags);

	private Identifier entityTypeId = ZOMBIE;
	@Nullable
	private TagKey<EntityType<?>> entityTypeTag = null;
	private long value = 100L;
	private String customName = "";
	private static final Map<Identifier,Icon<?>> entityIcons = new HashMap<>();

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
		entityTypeId = parseTypeId(nbt.getString("entity").orElse(""));
		entityTypeTag = parseTypeTag(nbt.getString("entityTypeTag").orElse(""));
		value = nbt.getLong("value").orElse(0L);
		customName = nbt.getString("custom_name").orElse("");
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
		entityTypeId = parseTypeId(buffer.readUtf());
		entityTypeTag = parseTypeTag(buffer.readUtf());
		value = buffer.readVarInt();
		customName = buffer.readUtf();
	}

	private static Identifier parseTypeId(String idStr) {
		return Objects.requireNonNullElse(Identifier.tryParse(idStr), ZOMBIE);
	}

	private static @Nullable TagKey<EntityType<?>> parseTypeTag(String tagStr) {
		if (tagStr.isEmpty()) {
			return null;
		}
		if (tagStr.startsWith("#")) {
			tagStr = tagStr.substring(1);
		}
		Identifier rl = Identifier.tryParse(tagStr);
        return rl == null || rl.getPath().isEmpty() ? null : TagKey.create(Registries.ENTITY_TYPE, rl);
    }

	@Override
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);

		config.addEnum("entity", entityTypeId, v -> entityTypeId = v, entityNameMap.get(), ZOMBIE);
		config.addEnum("entity_type_tag", getTypeTagStr(), v -> entityTypeTag = parseTypeTag(v), entityTagMap.get());
		config.addLong("value", value, v -> value = v, 100L, 1L, Long.MAX_VALUE);
		config.addString("custom_name", customName, v -> customName = v, "");
	}

	@Override
	public TaskClient client() {
		return TaskClient.NoOp.INSTANCE;
	}

	private String getTypeTagStr() {
		return entityTypeTag == null ? "" : entityTypeTag.location().toString();
	}

	private static Icon<?> getIconForEntityType(Identifier typeId) {
		return entityIcons.computeIfAbsent(typeId, k -> {
			Optional<Holder.Reference<EntityType<?>>> entityTypeOpt = BuiltInRegistries.ENTITY_TYPE.get(typeId);
			if (entityTypeOpt.isEmpty()) {
				return Icons.BARRIER;
			}

			var entityType = entityTypeOpt.get().value();
			if (entityType.equals(EntityType.PLAYER)) {
				return Icons.PLAYER;
			}

			Item item = SpawnEggItem.byId(entityType);
			if (item == null) {
				Entity e = entityType.create(ClientUtils.getClientLevel(), EntitySpawnReason.TRIGGERED);
				if (e != null) {
					ItemStack stack = e.getPickResult();
					if (stack != null) item = stack.getItem();
				}
			}
			return ItemIcon.ofItem(item != null ? item : Items.SPAWNER);
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
	public Icon<?> getAltIcon() {
		if (entityTypeTag == null) {
			return getIconForEntityType(entityTypeId);
		}

		List<Icon<?>> icons = new ArrayList<>();
		BuiltInRegistries.ENTITY_TYPE.get(entityTypeTag)
				.ifPresent(set ->
						set.forEach(holder ->
								holder.unwrapKey().map(k -> icons.add(getIconForEntityType(k.identifier())))
						)
				);
		return icons.isEmpty() ? Icons.BARRIER : AnimatedIcon.fromList(icons, false);
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
						p.getGameProfile().name().equals(customName) :
						e.getName().getString().equals(customName));
	}

	private static NameMap<Identifier> scanEntityTypes() {
		List<Identifier> ids = new ArrayList<>();
		BuiltInRegistries.ENTITY_TYPE.forEach(type -> {
			try {
				if (type.create(ClientUtils.getClientLevel(), EntitySpawnReason.TRIGGERED) instanceof LivingEntity) {
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

	private static NameMap<String> scanEntityTags() {
		List<String> tags = new ArrayList<>(List.of(""));
		tags.addAll(BuiltInRegistries.ENTITY_TYPE.listTagIds().map(key -> key.location().toString()).toList());
		return NameMap.of("minecraft:zombies", tags).create();
	}
}
