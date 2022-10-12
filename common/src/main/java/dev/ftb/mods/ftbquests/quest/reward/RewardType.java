package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.gui.SelectQuestObjectScreen;
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
	@Nullable
	public static Reward createReward(Quest quest, String id) {
		if (id.isEmpty()) {
			id = FTBQuests.MOD_ID + ":item";
		} else if (id.indexOf(':') == -1) {
			id = FTBQuests.MOD_ID + ':' + id;
		}

		RewardType type = RewardTypes.TYPES.get(new ResourceLocation(id));

		if (type == null) {
			return null;
		}

		return type.provider.create(quest);
	}

	@FunctionalInterface
	public interface Provider {
		Reward create(Quest quest);
	}

	public interface GuiProvider {
		@Environment(EnvType.CLIENT)
		void openCreationGui(Runnable gui, Quest quest, Consumer<Reward> callback);
	}

	public final ResourceLocation id;
	public final Provider provider;
	private final Supplier<Icon> icon;
	private Component displayName;
	private GuiProvider guiProvider;
	private boolean excludeFromListRewards;
	public int intId;

	public RewardType(ResourceLocation i, Provider p, Supplier<Icon> ic) {
		id = i;
		provider = p;
		icon = ic;
		displayName = null;
		guiProvider = (gui, quest, callback) -> {
			Reward reward = provider.create(quest);

			if (reward instanceof RandomReward rnd) {
				ConfigQuestObject<RewardTable> config = new ConfigQuestObject<>(QuestObjectType.REWARD_TABLE);
				SelectQuestObjectScreen<?> s = new SelectQuestObjectScreen<>(config, accepted -> {
					if (accepted) {
						rnd.table = config.value;
						callback.accept(reward);
					}
					gui.run();
				});
				s.setTitle(new TranslatableComponent("ftbquests.gui.select_reward_table"));
				s.setHasSearchBox(true);
				s.openGui();
			} else {
				ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
				reward.getConfig(reward.createSubGroup(group));
				group.savedCallback = accepted -> {
					if (accepted) {
						callback.accept(reward);
					}
					gui.run();
				};
				new EditConfigScreen(group).openGui();
			}
		};
	}

	public String getTypeForNBT() {
		return id.getNamespace().equals(FTBQuests.MOD_ID) ? id.getPath() : id.toString();
	}

	public RewardType setDisplayName(Component name) {
		displayName = name;
		return this;
	}

	public Component getDisplayName() {
		if (displayName == null) {
			displayName = Component.translatable("ftbquests.reward." + id.getNamespace() + '.' + id.getPath());
		}

		return displayName;
	}

	public Icon getIcon() {
		return icon.get();
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
}
