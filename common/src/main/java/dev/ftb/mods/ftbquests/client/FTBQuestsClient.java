package dev.ftb.mods.ftbquests.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ImageConfig;
import dev.ftb.mods.ftblibrary.config.IntConfig;
import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.FTBQuestsCommon;
import dev.ftb.mods.ftbquests.block.FTBQuestsBlocks;
import dev.ftb.mods.ftbquests.net.SetCustomImageMessage;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.reward.XPLevelsReward;
import dev.ftb.mods.ftbquests.quest.reward.XPReward;
import dev.ftb.mods.ftbquests.quest.task.CheckmarkTask;
import dev.ftb.mods.ftbquests.quest.task.DimensionTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.LocationTask;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.quest.theme.ThemeLoader;
import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent;
import me.shedaniel.architectury.registry.KeyBindings;
import me.shedaniel.architectury.registry.ReloadListeners;
import me.shedaniel.architectury.registry.RenderTypes;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class FTBQuestsClient extends FTBQuestsCommon {
	public static KeyMapping KEY_QUESTS;

	@Override
	public void init() {
		ClientLifecycleEvent.CLIENT_SETUP.register(this::setup);
		ReloadListeners.registerReloadListener(PackType.CLIENT_RESOURCES, new QuestFileCacheReloader());
		ReloadListeners.registerReloadListener(PackType.CLIENT_RESOURCES, new ThemeLoader());
		new FTBQuestsClientEventHandler().init();
	}

	private void setup(Minecraft minecraft) {
		KeyBindings.registerKeyBinding(KEY_QUESTS = new KeyMapping("key.ftbquests.quests", InputConstants.Type.KEYSYM, -1, "key.categories.ftbquests"));
		RenderTypes.register(RenderType.translucent(), FTBQuestsBlocks.BARRIER.get());
		RenderTypes.register(RenderType.translucent(), FTBQuestsBlocks.STAGE_BARRIER.get());
		setTaskGuiProviders();
		setRewardGuiProviders();
	}

	@Override
	@Nullable
	public QuestFile getClientQuestFile() {
		return ClientQuestFile.INSTANCE;
	}

	@Override
	public QuestFile getQuestFile(boolean isClient) {
		if (isClient) {
			QuestFile f = getClientQuestFile();

			if (f == null) {
				throw new NullPointerException("Client quest file not loaded!");
			}

			return f;
		}

		return ServerQuestFile.INSTANCE;
	}

	@Override
	public void setTaskGuiProviders() {
		TaskTypes.ITEM.setGuiProvider((gui, quest, callback) -> {
			ItemStackConfig c = new ItemStackConfig(false, false);
			c.defaultValue = ItemStack.EMPTY;
			c.value = ItemStack.EMPTY;

			new SelectItemStackScreen(c, accepted -> {
				gui.run();
				if (accepted) {
					ItemTask itemTask = new ItemTask(quest);
					itemTask.item = c.value.copy();
					itemTask.item.setCount(1);
					itemTask.count = c.value.getCount();
					callback.accept(itemTask);
				}
			}).openGui();
		});

		TaskTypes.CHECKMARK.setGuiProvider((gui, quest, callback) -> {
			StringConfig c = new StringConfig(null);

			EditConfigFromStringScreen.open(c, "", "", accepted -> {
				if (accepted) {
					CheckmarkTask checkmarkTask = new CheckmarkTask(quest);
					checkmarkTask.title = c.value;
					callback.accept(checkmarkTask);
				}

				gui.run();
			});
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

			if (mc.hitResult instanceof BlockHitResult) {
				BlockEntity tileEntity = mc.level.getBlockEntity(((BlockHitResult) mc.hitResult).getBlockPos());

				if (tileEntity instanceof StructureBlockEntity) {
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
				if (accepted) {
					callback.accept(task);
				}
			};

			new EditConfigScreen(group).openGui();
		});
	}

	@Override
	public void setRewardGuiProviders() {
		RewardTypes.ITEM.setGuiProvider((gui, quest, callback) -> {
			ItemStackConfig c = new ItemStackConfig(false, false);
			c.defaultValue = ItemStack.EMPTY;
			c.value = ItemStack.EMPTY;

			new SelectItemStackScreen(c, accepted -> {
				if (accepted) {
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
			IntConfig c = new IntConfig(1, Integer.MAX_VALUE);

			EditConfigFromStringScreen.open(c, 100, 100, accepted -> {
				if (accepted) {
					callback.accept(new XPReward(quest, c.value));
				}

				gui.run();
			});
		});

		RewardTypes.XP_LEVELS.setGuiProvider((gui, quest, callback) -> {
			IntConfig c = new IntConfig(1, Integer.MAX_VALUE);

			EditConfigFromStringScreen.open(c, 5, 5, accepted -> {
				if (accepted) {
					callback.accept(new XPLevelsReward(quest, c.value));
				}

				gui.run();
			});
		});
	}

	@Override
	public Player getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	@Override
	public boolean isClientDataLoaded() {
		return ClientQuestFile.INSTANCE != null;
	}

	@Override
	public TeamData getClientPlayerData() {
		return ClientQuestFile.INSTANCE.self;
	}

	@Override
	public QuestFile createClientQuestFile() {
		return new ClientQuestFile();
	}

	@Override
	public void openGui() {
		if (ClientQuestFile.exists()) {
			ClientQuestFile.INSTANCE.openQuestGui();
		} else {
			Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, new TextComponent("Error?! Server doesn't have FTB Quests!"), null));
		}
	}

	@Override
	public void openCustomIconGui(Player player, InteractionHand hand) {
		ImageConfig config = new ImageConfig();
		config.onClicked(MouseButton.LEFT, b -> {
			if (b) {
				if (config.value.isEmpty()) {
					player.getItemInHand(hand).removeTagKey("Icon");
				} else {
					player.getItemInHand(hand).addTagElement("Icon", StringTag.valueOf(config.value));
				}

				new SetCustomImageMessage(hand, config.value).sendToServer();
			}

			Minecraft.getInstance().setScreen(null);
		});
	}
}