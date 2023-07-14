package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class KillTask extends Task {
	public static final ResourceLocation ZOMBIE = new ResourceLocation("minecraft:zombie");
	public ResourceLocation entity = ZOMBIE;
	public long value = 100L;

	public KillTask(Quest quest) {
		super(quest);
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
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("entity", entity.toString());
		nbt.putLong("value", value);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		entity = new ResourceLocation(nbt.getString("entity"));
		value = nbt.getLong("value");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(entity.toString(), Short.MAX_VALUE);
		buffer.writeVarLong(value);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		entity = new ResourceLocation(buffer.readUtf(Short.MAX_VALUE));
		value = buffer.readVarInt();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		List<ResourceLocation> ids = new ArrayList<>(BuiltInRegistries.ENTITY_TYPE.keySet());

		config.addEnum("entity", entity, v -> entity = v, NameMap.of(ZOMBIE, ids)
				.nameKey(v -> "entity." + v.getNamespace() + "." + v.getPath())
				.icon(v -> {
					SpawnEggItem item = SpawnEggItem.byId(BuiltInRegistries.ENTITY_TYPE.get(v));
					return ItemIcon.getItemIcon(item != null ? item : Items.SPAWNER);
				})
				.create(), ZOMBIE);

		config.addLong("value", value, v -> value = v, 100L, 1L, Long.MAX_VALUE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.task.ftbquests.kill.title", formatMaxProgress(), Component.translatable("entity." + entity.getNamespace() + "." + entity.getPath()));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		SpawnEggItem item = SpawnEggItem.byId(BuiltInRegistries.ENTITY_TYPE.get(entity));
		return ItemIcon.getItemIcon(item != null ? item : Items.SPAWNER);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
	}

	public void kill(TeamData teamData, LivingEntity e) {
		if (!teamData.isCompleted(this) && entity.equals(RegistrarManager.getId(e.getType(), Registries.ENTITY_TYPE))) {
			teamData.addProgress(this, 1L);
		}
	}
}
