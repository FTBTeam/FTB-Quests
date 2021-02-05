package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsCommon;
import com.feed_the_beast.ftbquests.gui.ImageConfig;
import com.feed_the_beast.ftbquests.net.MessageSetCustomImage;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.feed_the_beast.ftbquests.quest.reward.RewardTypes;
import com.feed_the_beast.ftbquests.quest.reward.XPLevelsReward;
import com.feed_the_beast.ftbquests.quest.reward.XPReward;
import com.feed_the_beast.ftbquests.quest.task.DimensionTask;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import com.feed_the_beast.ftbquests.quest.task.LocationTask;
import com.feed_the_beast.ftbquests.quest.task.TaskTypes;
import com.feed_the_beast.ftbquests.quest.theme.ThemeLoader;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigInt;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigItemStack;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectItemStack;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent;
import me.shedaniel.architectury.registry.KeyBindings;
import me.shedaniel.architectury.registry.ReloadListeners;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Function;

public class FTBQuestsClient extends FTBQuestsCommon
{
	public static final Function<String, Component> DEFAULT_STRING_TO_COMPONENT = FTBQuestsClient::stringToComponent;

	private static Component stringToComponent(String s)
	{
		if (s.startsWith("open_url:"))
		{
			String[] s1 = s.substring(9).split("\\|", 2);
			return new TextComponent(s1[0]).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, s1[1])));
		}

		return new TextComponent(I18n.get(s));
	}

	public static KeyMapping KEY_QUESTS;

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
		TaskTypes.ITEM.setGuiProvider((gui, quest, callback) -> {
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

		TaskTypes.DIMENSION.setGuiProvider((gui, quest, callback) -> {
			DimensionTask task = new DimensionTask(quest);
			task.dimension = Minecraft.getInstance().level.dimension();
			callback.accept(task);
		});

		TaskTypes.LOCATION.setGuiProvider((gui, quest, callback) -> {
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
		RewardTypes.ITEM.setGuiProvider((gui, quest, callback) -> {
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

		RewardTypes.XP.setGuiProvider((gui, quest, callback) -> {
			ConfigInt c = new ConfigInt(1, Integer.MAX_VALUE);

			GuiEditConfigFromString.open(c, 100, 100, accepted -> {
				if (accepted)
				{
					callback.accept(new XPReward(quest, c.value));
				}

				gui.run();
			});
		});

		RewardTypes.XP_LEVELS.setGuiProvider((gui, quest, callback) -> {
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

	@Override
	public void openGui()
	{
		ClientQuestFile.INSTANCE.openQuestGui();
	}

	@Override
	public void openCustomIconGui(Player player, InteractionHand hand)
	{
		ImageConfig config = new ImageConfig();
		config.onClicked(MouseButton.LEFT, b -> {
			if (b)
			{
				if (config.value.isEmpty())
				{
					player.getItemInHand(hand).removeTagKey("Icon");
				}
				else
				{
					player.getItemInHand(hand).addTagElement("Icon", StringTag.valueOf(config.value));
				}

				new MessageSetCustomImage(hand, config.value).sendToServer();
			}

			Minecraft.getInstance().setScreen(null);
		});
	}
}