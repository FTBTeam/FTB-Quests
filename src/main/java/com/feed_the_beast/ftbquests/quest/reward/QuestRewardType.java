package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public final class QuestRewardType extends IForgeRegistryEntry.Impl<QuestRewardType>
{
	private static ForgeRegistry<QuestRewardType> REGISTRY;

	public static void createRegistry()
	{
		if (REGISTRY == null)
		{
			ResourceLocation registryName = new ResourceLocation(FTBQuests.MOD_ID, "rewards");
			REGISTRY = (ForgeRegistry<QuestRewardType>) new RegistryBuilder<QuestRewardType>().setType(QuestRewardType.class).setName(registryName).create();
			MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<>(registryName, REGISTRY));
		}
	}

	public static ForgeRegistry<QuestRewardType> getRegistry()
	{
		return REGISTRY;
	}

	@Nullable
	public static QuestReward createReward(Quest quest, String id)
	{
		if (id.isEmpty())
		{
			id = FTBQuests.MOD_ID + ":item";
		}
		else if (id.indexOf(':') == -1)
		{
			id = FTBQuests.MOD_ID + ':' + id;
		}

		QuestRewardType type = REGISTRY.getValue(new ResourceLocation(id));

		if (type == null)
		{
			return null;
		}

		QuestReward reward = type.provider.create(quest);

		if (reward == null)
		{
			return null;
		}

		return reward;
	}

	public interface Provider
	{
		@Nullable
		QuestReward create(Quest quest);
	}

	public interface GuiProvider
	{
		@SideOnly(Side.CLIENT)
		void openCreationGui(IOpenableGui gui, Quest quest);
	}

	public final Class typeClass;
	public final Provider provider;
	private ITextComponent displayName;
	private Icon icon;
	private GuiProvider guiProvider;

	public QuestRewardType(Class<? extends QuestReward> c, Provider p)
	{
		typeClass = c;
		provider = p;
		displayName = null;
		icon = GuiIcons.MONEY_BAG;
		guiProvider = new GuiProvider()
		{
			@Override
			@SideOnly(Side.CLIENT)
			public void openCreationGui(IOpenableGui gui, Quest quest)
			{
				QuestReward reward = provider.create(quest);

				if (reward == null)
				{
					return;
				}

				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
				ConfigGroup g = reward.createSubGroup(group);
				reward.getConfig(g);
				reward.getExtraConfig(g);

				new GuiEditConfig(group, (g1, sender) -> {
					NBTTagCompound extra = new NBTTagCompound();
					extra.setString("type", getTypeForNBT());
					new MessageCreateObject(quest.uid, reward, extra).sendToServer();
				}).openGui();
			}
		};
	}

	public String getTypeForNBT()
	{
		return getRegistryName().getNamespace().equals(FTBQuests.MOD_ID) ? getRegistryName().getPath() : toString();
	}

	public QuestRewardType setDisplayName(ITextComponent name)
	{
		displayName = name;
		return this;
	}

	public ITextComponent getDisplayName()
	{
		if (displayName != null)
		{
			return displayName.createCopy();
		}

		ResourceLocation id = getRegistryName();
		return new TextComponentTranslation("ftbquests.reward." + id.getNamespace() + '.' + id.getPath());
	}

	public QuestRewardType setIcon(Icon i)
	{
		icon = i;
		return this;
	}

	public Icon getIcon()
	{
		return icon;
	}

	public QuestRewardType setGuiProvider(GuiProvider p)
	{
		guiProvider = p;
		return this;
	}

	public GuiProvider getGuiProvider()
	{
		return guiProvider;
	}
}