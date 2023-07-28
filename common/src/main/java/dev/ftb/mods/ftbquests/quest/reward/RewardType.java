package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
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
		guiProvider = (gui, quest, callback) -> {
			Reward reward = this.provider.create(0L, quest);

			if (reward instanceof RandomReward randomReward) {
				ConfigQuestObject<RewardTable> config = new ConfigQuestObject<>(QuestObjectType.REWARD_TABLE);
				SelectQuestObjectScreen<?> s = new SelectQuestObjectScreen<>(config, accepted -> {
					if (accepted) {
						randomReward.setTable(config.getValue());
						callback.accept(reward);
					}
					gui.run();
				});
				s.setTitle(Component.translatable("ftbquests.gui.select_reward_table"));
				s.setHasSearchBox(true);
				s.openGui();
			} else {
				ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID, accepted -> {
					if (accepted) {
						callback.accept(reward);
					}
					gui.run();
				});
				reward.fillConfigGroup(reward.createSubGroup(group));
				new EditConfigScreen(group).openGui();
			}
		};
	}

	@Nullable
	public static Reward createReward(long id, Quest quest, String typeId) {
		if (typeId.isEmpty()) {
			typeId = FTBQuests.MOD_ID + ":item";
		} else if (typeId.indexOf(':') == -1) {
			typeId = FTBQuests.MOD_ID + ':' + typeId;
		}

		RewardType type = RewardTypes.TYPES.get(new ResourceLocation(typeId));

		if (type == null) {
			return null;
		}

		return type.provider.create(id, quest);
	}

	public ResourceLocation getTypeId() {
		return typeId;
	}

	public Reward createReward(long id, Quest quest) {
		return provider.create(id, quest);
	}

	public String getTypeForNBT() {
		return typeId.getNamespace().equals(FTBQuests.MOD_ID) ? typeId.getPath() : typeId.toString();
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
		void openCreationGui(Runnable gui, Quest quest, Consumer<Reward> callback);
	}
}
