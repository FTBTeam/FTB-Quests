package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StatTask extends Task {
	private Identifier stat;
	private int value = 1;

	public StatTask(long id, Quest quest) {
		super(id, quest);
		stat = Stats.MOB_KILLS;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.STAT;
	}

	@Override
	public long getMaxProgress() {
		return value;
	}

	@Override
	public String formatMaxProgress() {
		return Integer.toString(value);
	}

	@Override
	public String formatProgress(TeamData teamData, long progress) {
		return Long.toUnsignedString(progress);
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.putString("stat", stat.toString());
		nbt.putInt("value", value);
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		stat = Identifier.tryParse(nbt.getString("stat").orElseThrow());
		value = nbt.getInt("value").orElseThrow();
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeIdentifier(stat);
		buffer.writeVarInt(value);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		stat = buffer.readIdentifier();
		value = buffer.readVarInt();
	}

	@Override
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		List<Identifier> list = new ArrayList<>();
		Stats.CUSTOM.iterator().forEachRemaining(s -> list.add(s.getValue()));
		config.addEnum("stat", stat, v -> stat = v, NameMap.of(Stats.MOB_KILLS, list).name(v -> Component.translatable("stat." + v.getNamespace() + "." + v.getPath())).create());
		config.addInt("value", value, v -> value = v, 1, 1, Integer.MAX_VALUE);
	}

	@Override
	public MutableComponent getAltTitle() {
		return Component.translatable("stat." + stat.getNamespace() + "." + stat.getPath());
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 3;
	}

	@Override
	public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
		if (teamData.isCompleted(this) || !checkTaskSequence(teamData)) {
			return;
		}

		Optional<Holder.Reference<Identifier>> statId = BuiltInRegistries.CUSTOM_STAT.get(stat);

		// workaround for a bug where mods might register a modded stat in the vanilla namespace
		//  https://github.com/FTBTeam/FTB-Mods-Issues/issues/724
		if (statId.isEmpty()) {
			var attemptedId = Identifier.tryParse(stat.getPath());
			if (attemptedId != null) {
				statId = BuiltInRegistries.CUSTOM_STAT.get(attemptedId);
			}
		}

		if (statId.isPresent()) {
			// could be null, if someone brought an FTB Quests save from a different world and the stat's missing here
			int set = Math.min(value, player.getStats().getValue(Stats.CUSTOM.get(statId.get().value())));
			if (set > teamData.getProgress(this)) {
				teamData.setProgress(this, set);
			}
		}
	}
}
