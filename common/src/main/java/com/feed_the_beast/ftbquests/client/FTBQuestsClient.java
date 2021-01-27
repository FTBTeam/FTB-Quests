package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsCommon;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.feed_the_beast.ftbquests.quest.reward.XPLevelsReward;
import com.feed_the_beast.ftbquests.quest.reward.XPReward;
import com.feed_the_beast.ftbquests.quest.task.DimensionTask;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.FluidTask;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import com.feed_the_beast.ftbquests.quest.task.LocationTask;
import com.feed_the_beast.ftbquests.quest.theme.ThemeLoader;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigFluid;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigInt;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigItemStack;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectFluid;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectItemStack;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent;
import me.shedaniel.architectury.registry.KeyBindings;
import me.shedaniel.architectury.registry.ReloadListeners;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTBQuestsClient extends FTBQuestsCommon
{
	private static final Pattern I18N_PATTERN = Pattern.compile("\\{([a-zA-Z0-9\\._\\-]*?)\\}", Pattern.MULTILINE);
	public static KeyMapping KEY_QUESTS;

	public static MutableComponent addI18nAndColors(String text)
	{
		if (text.isEmpty())
		{
			return (TextComponent) TextComponent.EMPTY;
		}

		Matcher i18nMatcher = I18N_PATTERN.matcher(text);

		while (i18nMatcher.find())
		{
			i18nMatcher.reset();

			StringBuffer sb = new StringBuffer(text.length());

			while (i18nMatcher.find())
			{
				i18nMatcher.appendReplacement(sb, I18n.get(i18nMatcher.group(1)));
			}

			i18nMatcher.appendTail(sb);
			text = sb.toString();
			i18nMatcher = I18N_PATTERN.matcher(text);
		}

		text = StringUtils.addFormatting(text.trim());

		if (StringUtils.unformatted(text).isEmpty())
		{
			return (TextComponent) TextComponent.EMPTY;
		}

		return new TextComponent(text);
	}

	@Override
	public void init()
	{
		ClientLifecycleEvent.CLIENT_SETUP.register(this::setup);
		ReloadListeners.registerReloadListener(PackType.CLIENT_RESOURCES, new QuestFileCacheReloader());
        ReloadListeners.registerReloadListener(PackType.CLIENT_RESOURCES, new ThemeLoader());
		new FTBQuestsClientEventHandler().init();
	}

	private void setup(Minecraft minecraft)
	{
		KeyBindings.registerKeyBinding(KEY_QUESTS = new KeyMapping("key.ftbquests.quests", InputConstants.Type.KEYSYM, -1, "key.categories.ftbquests"));
		setTaskGuiProviders();
		setRewardGuiProviders();
	}

	@Override
	public QuestFile getQuestFile(boolean isClient)
	{
		return isClient ? ClientQuestFile.INSTANCE : ServerQuestFile.INSTANCE;
	}

	@Override
	public void setTaskGuiProviders()
	{
		FTBQuestsTasks.ITEM.get().setGuiProvider((gui, quest, callback) -> {
			ConfigItemStack c = new ConfigItemStack(false, false);
			c.defaultValue = ItemStack.EMPTY;
			c.value = ItemStack.EMPTY;

			new GuiSelectItemStack(c, accepted -> {
				gui.run();
				if (accepted)
				{
					ItemTask itemTask = new ItemTask(quest);
					itemTask.item = c.value.copy();
					itemTask.item.setCount(1);
					itemTask.count = c.value.getCount();
					callback.accept(itemTask);
				}
			}).openGui();
		});

		/*FTBQuestsTasks.FLUID.get().setGuiProvider((gui, quest, callback) -> {
			ConfigFluid c = new ConfigFluid(false);
			c.defaultValue = Fluids.EMPTY;
			c.value = Fluids.EMPTY;

			new GuiSelectFluid(c, accepted -> {
				gui.run();
				if (accepted)
				{
					FluidTask fluidTask = new FluidTask(quest);
					fluidTask.fluid = c.value;
					callback.accept(fluidTask);
				}
			}).openGui();
		});*/

		FTBQuestsTasks.DIMENSION.get().setGuiProvider((gui, quest, callback) -> {
			DimensionTask task = new DimensionTask(quest);
			task.dimension = Minecraft.getInstance().level.dimension();
			callback.accept(task);
		});

		FTBQuestsTasks.LOCATION.get().setGuiProvider((gui, quest, callback) -> {
			LocationTask task = new LocationTask(quest);
			Minecraft mc = Minecraft.getInstance();

			if (mc.hitResult instanceof BlockHitResult)
			{
				BlockEntity tileEntity = mc.level.getBlockEntity(((BlockHitResult) mc.hitResult).getBlockPos());

				if (tileEntity instanceof StructureBlockEntity)
				{
					BlockPos pos = ((StructureBlockEntity) tileEntity).getStructurePos();
					BlockPos size = ((StructureBlockEntity) tileEntity).getStructureSize();
					task.dimension = mc.level.dimension();
					task.x = pos.getX() + tileEntity.getBlockPos().getX();
					task.y = pos.getY() + tileEntity.getBlockPos().getY();
					task.z = pos.getZ() + tileEntity.getBlockPos().getZ();
					task.w = Math.max(1, size.getX());
					task.h = Math.max(1, size.getY());
					task.d = Math.max(1, size.getZ());
					callback.accept(task);
					return;
				}
			}

			ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
			task.getConfig(task.createSubGroup(group));

			group.savedCallback = accepted -> {
				gui.run();
				if (accepted)
				{
					callback.accept(task);
				}
			};

			new GuiEditConfig(group).openGui();
		});
	}

	@Override
	public void setRewardGuiProviders()
	{
		FTBQuestsRewards.ITEM.get().setGuiProvider((gui, quest, callback) -> {
			ConfigItemStack c = new ConfigItemStack(false, false);
			c.defaultValue = ItemStack.EMPTY;
			c.value = ItemStack.EMPTY;

			new GuiSelectItemStack(c, accepted -> {
				if (accepted)
				{
                    ItemStack copy = c.value.copy();
                    copy.setCount(1);
                    ItemReward reward = new ItemReward(quest, copy);
					reward.count = c.value.getCount();
					callback.accept(reward);
				}
				gui.run();
			}).openGui();
		});

		FTBQuestsRewards.XP.get().setGuiProvider((gui, quest, callback) -> {
			ConfigInt c = new ConfigInt(1, Integer.MAX_VALUE);

			GuiEditConfigFromString.open(c, 100, 100, accepted -> {
				if (accepted)
				{
					callback.accept(new XPReward(quest, c.value));
				}

				gui.run();
			});
		});

		FTBQuestsRewards.XP_LEVELS.get().setGuiProvider((gui, quest, callback) -> {
			ConfigInt c = new ConfigInt(1, Integer.MAX_VALUE);

			GuiEditConfigFromString.open(c, 5, 5, accepted -> {
				if (accepted)
				{
					callback.accept(new XPLevelsReward(quest, c.value));
				}

				gui.run();
			});
		});
	}

	@Override
	public PlayerData getClientPlayerData()
	{
		return ClientQuestFile.INSTANCE.getData(Minecraft.getInstance().player);
	}

	@Override
	public QuestFile createClientQuestFile()
	{
		return new ClientQuestFile();
	}
}