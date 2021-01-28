package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.gui.GuiSelectQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import me.shedaniel.architectury.core.RegistryEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public final class RewardType extends RegistryEntry<RewardType>
{
	@Nullable
	public static Reward createReward(Quest quest, String id)
	{
		if (id.isEmpty())
		{
			id = FTBQuests.MOD_ID + ":item";
		}
		else if (id.indexOf(':') == -1)
		{
			id = FTBQuests.MOD_ID + ':' + id;
		}

		RewardType type = RewardTypes.TYPES.get(new ResourceLocation(id));

		if (type == null)
		{
			return null;
		}

		return type.provider.create(quest);
	}

	@FunctionalInterface
	public interface Provider
	{
		Reward create(Quest quest);
	}

	public interface GuiProvider
	{
		@Environment(EnvType.CLIENT)
		void openCreationGui(Runnable gui, Quest quest, Consumer<Reward> callback);
	}

	public final ResourceLocation id;
	public final Provider provider;
	private MutableComponent displayName;
	private Icon icon;
	private GuiProvider guiProvider;
	private boolean excludeFromListRewards;
	public int intId;

	public RewardType(ResourceLocation i, Provider p)
	{
		id = i;
		provider = p;
		displayName = null;
		icon = GuiIcons.MONEY_BAG;
		guiProvider = new GuiProvider()
		{
			@Override
			@Environment(EnvType.CLIENT)
			public void openCreationGui(Runnable gui, Quest quest, Consumer<Reward> callback)
			{
				Reward reward = provider.create(quest);

				if (reward instanceof RandomReward)
				{
					ConfigQuestObject<RewardTable> config = new ConfigQuestObject<>(QuestObjectType.REWARD_TABLE);
					new GuiSelectQuestObject<>(config, accepted -> {
						if (accepted)
						{
							((RandomReward) reward).table = config.value;
							callback.accept(reward);
						}
						gui.run();
					}).openGui();
					return;
				}

				ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
				reward.getConfig(reward.createSubGroup(group));
				group.savedCallback = accepted -> {
					if (accepted)
					{
						callback.accept(reward);
					}
					gui.run();
				};
				new GuiEditConfig(group).openGui();
			}
		};
	}

	public String getTypeForNBT()
	{
		return id.getNamespace().equals(FTBQuests.MOD_ID) ? id.getPath() : id.toString();
	}

	public RewardType setDisplayName(MutableComponent name)
	{
		displayName = name;
		return this;
	}

	public MutableComponent getDisplayName()
	{
		if (displayName == null)
		{
			displayName = new TranslatableComponent("ftbquests.reward." + id.getNamespace() + '.' + id.getPath());
		}

		return displayName;
	}

	public RewardType setIcon(Icon i)
	{
		icon = i;
		return this;
	}

	public Icon getIcon()
	{
		return icon;
	}

	public RewardType setGuiProvider(GuiProvider p)
	{
		guiProvider = p;
		return this;
	}

	public GuiProvider getGuiProvider()
	{
		return guiProvider;
	}

	public RewardType setExcludeFromListRewards(boolean v)
	{
		excludeFromListRewards = v;
		return this;
	}

	public boolean getExcludeFromListRewards()
	{
		return excludeFromListRewards;
	}
}