package dev.ftb.mods.ftbquests.quest.task;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.IdentifierException;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.UnknownNullability;

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
	public void writeData(Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);
		Json5Util.store(json, "advancement", Identifier.CODEC, advancement);
		json.addProperty("criterion", criterion);
	}

	@Override
	public void readData(Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);
		advancement = Json5Util.fetch(json, "advancement", Identifier.CODEC).orElseThrow();
		criterion = Json5Util.getString(json, "criterion").orElseThrow();
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
	public void fillConfigGroup(EditableConfigGroup config) {
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
			try {
				config.addString("advancement", advancement.toString(), v -> advancement = Identifier.parse(v), "minecraft:story/root").setNameKey("ftbquests.task.ftbquests.advancement");
			} catch (IdentifierException ignored) {
			}
		}

		config.addString("criterion", criterion, v -> criterion = v, "");
	}

	@Override
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
	public Icon<?> getAltIcon() {
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
