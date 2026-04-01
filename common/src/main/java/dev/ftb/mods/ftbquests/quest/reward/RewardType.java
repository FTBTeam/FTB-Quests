package dev.ftb.mods.ftbquests.quest.reward;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.GuiProviders;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RewardType {
	private final Identifier typeId;
	private final Provider provider;
	private final Supplier<Icon<?>> iconSupplier;
	private final Component displayName;
	@Nullable
	private GuiProvider guiProvider;
	private boolean excludeFromListRewards = false;
	public int internalId;

	public RewardType(Identifier typeId, Provider provider, Supplier<Icon<?>> iconSupplier, boolean availableByDefault) {
		this.typeId = typeId;
		this.provider = provider;
		this.iconSupplier = iconSupplier;

		displayName = Component.translatable(typeId.toLanguageKey("ftbquests.reward"));
		guiProvider = availableByDefault ? GuiProviders.defaultRewardGuiProvider(provider) : null;
	}

	public RewardType(Identifier typeId, Provider provider, Supplier<Icon<?>> iconSupplier) {
		this(typeId, provider, iconSupplier, true);
	}

	public static Reward createReward(long id, Quest quest, String typeId) {
		if (typeId.isEmpty()) {
			typeId = FTBQuestsAPI.MOD_ID + ":item";
		} else if (typeId.indexOf(':') == -1) {
			typeId = FTBQuestsAPI.MOD_ID + ':' + typeId;
		}

		RewardType type = RewardTypes.TYPES.get(Identifier.tryParse(typeId));
		Validate.isTrue(type != null, "Unknown reward type: " + type);
        return type.provider.create(id, quest);
    }

	public Identifier getTypeId() {
		return typeId;
	}

	public Reward createReward(long id, Quest quest) {
		return provider.create(id, quest);
	}

	public String getTypeForSerialization() {
		return typeId.getNamespace().equals(FTBQuestsAPI.MOD_ID) ? typeId.getPath() : typeId.toString();
	}

	public Json5Object makeExtraJson() {
		return Util.make(new Json5Object(), t -> t.addProperty("type", getTypeForSerialization()));
	}

	public Component getDisplayName() {
		return displayName;
	}

	public Icon<?> getIconSupplier() {
		return iconSupplier.get();
	}

	public RewardType setGuiProvider(GuiProvider p) {
		guiProvider = p;
		return this;
	}

	@Nullable
	public GuiProvider getGuiProvider() {
		return guiProvider;
	}

	public RewardType setExcludeFromListRewards(boolean v) {
		excludeFromListRewards = v;
		return this;
	}

	public boolean getExcludeFromListRewards() {
		return excludeFromListRewards;
	}

	@FunctionalInterface
	public interface Provider {
		Reward create(long id, Quest quest);
	}

	@FunctionalInterface
	public interface GuiProvider {
		void openCreationGui(Panel panel, Quest quest, Consumer<Reward> callback);
	}
}
