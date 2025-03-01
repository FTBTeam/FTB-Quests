package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KillTask extends Task {
	private static final ResourceLocation ZOMBIE = ResourceLocation.withDefaultNamespace("zombie");

	private ResourceLocation entityTypeId = ZOMBIE;
	private long value = 100L;
	private String customName = "";
	private NameMap<ResourceLocation> entityNameMap = null;
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
		nbt.putLong("value", value);
		if (!customName.isEmpty()) nbt.putString("custom_name", customName);
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		entityTypeId = ResourceLocation.tryParse(nbt.getString("entity"));
		value = nbt.getLong("value");
		customName = nbt.getString("custom_name");
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(entityTypeId.toString(), Short.MAX_VALUE);
		buffer.writeVarLong(value);
		buffer.writeUtf(customName);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		entityTypeId = ResourceLocation.tryParse(buffer.readUtf(Short.MAX_VALUE));
		value = buffer.readVarInt();
		customName = buffer.readUtf();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		List<ResourceLocation> ids = new ArrayList<>(BuiltInRegistries.ENTITY_TYPE.keySet());

		if (entityNameMap == null) {
			entityNameMap = NameMap.of(ZOMBIE, ids)
					.nameKey(id -> "entity." + id.toLanguageKey())
					.icon(KillTask::getIconForEntityType)
					.create();
		}

		config.addEnum("entity", entityTypeId, v -> entityTypeId = v, entityNameMap, ZOMBIE);
		config.addLong("value", value, v -> value = v, 100L, 1L, Long.MAX_VALUE);
		config.addString("custom_name", customName, v -> customName = v, "");
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

		entityNameMap = null;
		entityIcons.clear();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		if (!customName.isEmpty()) {
			return Component.translatable("ftbquests.task.ftbquests.kill.title_named", formatMaxProgress(),
					Component.translatable("entity." + entityTypeId.toLanguageKey()), Component.literal(customName));
		} else {
			return Component.translatable("ftbquests.task.ftbquests.kill.title", formatMaxProgress(),
					Component.translatable("entity." + entityTypeId.toLanguageKey()));
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		return getIconForEntityType(entityTypeId);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
	}

	public void kill(TeamData teamData, LivingEntity e) {
		if (!teamData.isCompleted(this) && entityTypeId.equals(RegistrarManager.getId(e.getType(), Registries.ENTITY_TYPE)) && nameMatchOK(e)) {
			teamData.addProgress(this, 1L);
		}
	}

	private boolean nameMatchOK(LivingEntity e) {
		return customName.isEmpty() ||
				(e instanceof Player p ?
						p.getGameProfile().getName().equals(customName) :
						e.getName().getString().equals(customName));
	}
}
