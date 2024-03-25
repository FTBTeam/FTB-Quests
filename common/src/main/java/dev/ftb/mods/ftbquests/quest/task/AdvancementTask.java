package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class AdvancementTask extends AbstractBooleanTask {
	private ResourceLocation advancement = new ResourceLocation("minecraft:story/root");
	private String criterion = "";

	public AdvancementTask(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.ADVANCEMENT;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("advancement", advancement.toString());
		nbt.putString("criterion", criterion);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		advancement = new ResourceLocation(nbt.getString("advancement"));
		criterion = nbt.getString("criterion");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeResourceLocation(advancement);
		buffer.writeUtf(criterion, Short.MAX_VALUE);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		advancement = buffer.readResourceLocation();
		criterion = buffer.readUtf(Short.MAX_VALUE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		if (KnownServerRegistries.client != null && !KnownServerRegistries.client.advancements.isEmpty()) {
			Map<ResourceLocation, KnownServerRegistries.AdvancementInfo> advancements = KnownServerRegistries.client.advancements;
			KnownServerRegistries.AdvancementInfo def = advancements.values().iterator().next();
			config.addEnum("advancement", advancement, v -> advancement = v, NameMap.of(def.id, advancements.keySet().toArray(new ResourceLocation[0]))
					.icon(id -> ItemIcon.getItemIcon(KnownServerRegistries.client.advancements.getOrDefault(id, def).icon))
					.name(id -> KnownServerRegistries.client.advancements.getOrDefault(id, def).name)
					.create()).setNameKey("ftbquests.task.ftbquests.advancement");
		} else {
			config.addString("advancement", advancement.toString(), v -> advancement = new ResourceLocation(v), "minecraft:story/root").setNameKey("ftbquests.task.ftbquests.advancement");
		}

		config.addString("criterion", criterion, v -> criterion = v, "");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		KnownServerRegistries.AdvancementInfo info = KnownServerRegistries.client == null ? null : KnownServerRegistries.client.advancements.get(advancement);

		if (info != null && info.name.getContents() != PlainTextContents.EMPTY) {
			return Component.translatable("ftbquests.task.ftbquests.advancement").append(": ").append(Component.literal("").append(info.name).withStyle(ChatFormatting.YELLOW));
		}

		return super.getAltTitle();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		KnownServerRegistries.AdvancementInfo info = KnownServerRegistries.client == null ? null : KnownServerRegistries.client.advancements.get(advancement);

		if (info != null && !info.icon.isEmpty()) {
			return ItemIcon.getItemIcon(info.icon);
		}

		return super.getAltIcon();
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 5;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		AdvancementHolder advancementHolder = player.server.getAdvancements().get(advancement);
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
