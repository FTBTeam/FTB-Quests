package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.GuiProviders;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RewardType {
	private final ResourceLocation typeId;
	private final Provider provider;
	private final Supplier<Icon> iconSupplier;
	private Component displayName;
	private GuiProvider guiProvider;
	private boolean excludeFromListRewards = false;
	public int intId;

	public RewardType(ResourceLocation typeId, Provider provider, Supplier<Icon> iconSupplier) {
		this.typeId = typeId;
		this.provider = provider;
		this.iconSupplier = iconSupplier;

		displayName = null;
		guiProvider = GuiProviders.defaultRewardGuiProvider(provider);
	}

	@Nullable
	public static Reward createReward(long id, Quest quest, String typeId) {
		if (typeId.isEmpty()) {
			typeId = FTBQuestsAPI.MOD_ID + ":item";
		} else if (typeId.indexOf(':') == -1) {
			typeId = FTBQuestsAPI.MOD_ID + ':' + typeId;
		}

		RewardType type = RewardTypes.TYPES.get(new ResourceLocation(typeId));

        return type == null ? null : type.provider.create(id, quest);
    }

	public ResourceLocation getTypeId() {
		return typeId;
	}

	public Reward createReward(long id, Quest quest) {
		return provider.create(id, quest);
	}

	public String getTypeForNBT() {
		return typeId.getNamespace().equals(FTBQuestsAPI.MOD_ID) ? typeId.getPath() : typeId.toString();
	}

	public CompoundTag makeExtraNBT() {
		return Util.make(new CompoundTag(), t -> t.putString("type", getTypeForNBT()));
	}

	public RewardType setDisplayName(Component name) {
		displayName = name;
		return this;
	}

	public Component getDisplayName() {
		if (displayName == null) {
			displayName = Component.translatable("ftbquests.reward." + typeId.getNamespace() + '.' + typeId.getPath());
		}

		return displayName;
	}

	public Icon getIconSupplier() {
		return iconSupplier.get();
	}

	public RewardType setGuiProvider(GuiProvider p) {
		guiProvider = p;
		return this;
	}

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
		@Environment(EnvType.CLIENT)
		void openCreationGui(Panel panel, Quest quest, Consumer<Reward> callback);
	}
}
