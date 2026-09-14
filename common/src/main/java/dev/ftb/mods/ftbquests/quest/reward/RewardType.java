package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.client.GuiProviders;
import dev.ftb.mods.ftbquests.quest.RegisteredQuestObjectType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public final class RewardType extends RegisteredQuestObjectType<Reward> {
	private boolean excludedFromClaimAll = false;

	public RewardType(Identifier typeId, Provider provider, Supplier<Icon<?>> iconSupplier, boolean availableByDefault, int internalId) {
		super(typeId, provider, iconSupplier, internalId,
				Component.translatable(typeId.toLanguageKey("ftbquests.reward")),
				availableByDefault ? GuiProviders.defaultRewardGuiProvider(provider) : null
		);
	}

	public RewardType(Identifier typeId, Provider provider, Supplier<Icon<?>> iconSupplier, int internalId) {
		this(typeId, provider, iconSupplier, true, internalId);
	}

	@Nullable
	public static RewardType get(String typeStr) {
		return RegisteredQuestObjectType.get(typeStr, RewardTypes.TYPES, "item");
	}

	public static RewardType getOrThrow(String typeStr) {
		return RegisteredQuestObjectType.getOrThrow(typeStr, RewardTypes.TYPES, "item");
	}

	public RewardType setExcludedFromClaimAll(boolean v) {
		excludedFromClaimAll = v;
		return this;
	}

	public boolean isExcludedFromClaimAll() {
		return excludedFromClaimAll;
	}

	public interface Provider extends RegisteredQuestObjectType.Provider<Reward> {
	}

	public interface GuiProvider extends RegisteredQuestObjectType.GuiProvider<Reward> {
	}
}
