package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public class AdvancementTask extends AbstractBooleanTask {
	private Identifier advancement = Identifier.parse("minecraft:story/root");
	private String criterion = "";

	public AdvancementTask(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.ADVANCEMENT;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.store("advancement", Identifier.CODEC, advancement);
		nbt.putString("criterion", criterion);
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		advancement = nbt.read("advancement", Identifier.CODEC).orElseThrow();
		criterion = nbt.getString("criterion").orElseThrow();
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeIdentifier(advancement);
		buffer.writeUtf(criterion, Short.MAX_VALUE);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		advancement = buffer.readIdentifier();
		criterion = buffer.readUtf(Short.MAX_VALUE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		if (KnownServerRegistries.client != null && !KnownServerRegistries.client.advancements().isEmpty()) {
			var advancements = KnownServerRegistries.client.advancements();
			KnownServerRegistries.AdvancementInfo def = advancements.values().iterator().next();
			config.addEnum("advancement", advancement, v -> advancement = v,
					NameMap.of(def.id(), advancements.keySet().toArray(new Identifier[0]))
							.icon(id -> ItemIcon.ofItemStack(advancements.getOrDefault(id, def).icon()))
							.name(id -> advancements.getOrDefault(id, def).name())
							.create()).setNameKey("ftbquests.task.ftbquests.advancement");
		} else {
			config.addString("advancement", advancement.toString(), v -> advancement = Identifier.tryParse(v), "minecraft:story/root").setNameKey("ftbquests.task.ftbquests.advancement");
		}

		config.addString("criterion", criterion, v -> criterion = v, "");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		KnownServerRegistries.AdvancementInfo info = KnownServerRegistries.client == null ?
				null :
				KnownServerRegistries.client.advancements().get(advancement);

		if (info != null && !TextUtils.isComponentEmpty(info.name())) {
			return Component.translatable("ftbquests.task.ftbquests.advancement").append(": ").append(Component.literal("").append(info.name()).withStyle(ChatFormatting.YELLOW));
		}

		return super.getAltTitle();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		KnownServerRegistries.AdvancementInfo info = KnownServerRegistries.client == null ?
				null :
				KnownServerRegistries.client.advancements().get(advancement);

		if (info != null && !info.icon().isEmpty()) {
			return ItemIcon.ofItemStack(info.icon());
		}

		return super.getAltIcon();
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 5;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		AdvancementHolder advancementHolder = player.level().getServer().getAdvancements().get(advancement);
		if (advancementHolder == null) {
			return false;
		}

		AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancementHolder);

		if (criterion.isEmpty()) {
			return progress.isDone();
		} else {
			CriterionProgress criterionProgress = progress.getCriterion(criterion);
			return criterionProgress != null && criterionProgress.isDone();
		}
	}
}
