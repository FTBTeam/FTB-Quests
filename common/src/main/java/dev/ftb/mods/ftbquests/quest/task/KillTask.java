package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftbguilibrary.config.ConfigGroup;
import dev.ftb.mods.ftbguilibrary.config.NameMap;
import dev.ftb.mods.ftbguilibrary.icon.Icon;
import dev.ftb.mods.ftbguilibrary.icon.ItemIcon;
import dev.ftb.mods.ftbguilibrary.widget.Button;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import me.shedaniel.architectury.registry.Registries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		List<ResourceLocation> ids = new ArrayList<>(Registry.ENTITY_TYPE.keySet());

		config.addEnum("entity", entity, v -> entity = v, NameMap.of(ZOMBIE, ids)
				.nameKey(v -> "entity." + v.getNamespace() + "." + v.getPath())
				.icon(v -> {
					SpawnEggItem item = SpawnEggItem.byId(Registry.ENTITY_TYPE.get(v));
					return ItemIcon.getItemIcon(item != null ? item : Items.SPAWNER);
				})
				.create(), ZOMBIE);

		config.addLong("value", value, v -> value = v, 100L, 1L, Long.MAX_VALUE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return new TranslatableComponent("ftbquests.task.ftbquests.kill.title", formatMaxProgress(), new TranslatableComponent("entity." + entity.getNamespace() + "." + entity.getPath()));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		SpawnEggItem item = SpawnEggItem.byId(Registry.ENTITY_TYPE.get(entity));
		return ItemIcon.getItemIcon(item != null ? item : Items.SPAWNER);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
	}

	public void kill(TeamData teamData, LivingEntity e) {
		if (!teamData.isCompleted(this) && entity.equals(Registries.getId(e.getType(), Registry.ENTITY_TYPE_REGISTRY))) {
			teamData.addProgress(this, 1L);
		}
	}
}