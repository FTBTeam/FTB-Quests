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
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectFluid;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectItemStack;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTBQuestsClient extends FTBQuestsCommon
{
	private static final Pattern I18N_PATTERN = Pattern.compile("\\{([a-zA-Z0-9\\._\\-]*?)\\}", Pattern.MULTILINE);
	public static KeyBinding KEY_QUESTS;

	public static String addI18nAndColors(String text)
	{
		if (text.isEmpty())
		{
			return text;
		}

		Matcher i18nMatcher = I18N_PATTERN.matcher(text);

		while (i18nMatcher.find())
		{
			i18nMatcher.reset();

			StringBuffer sb = new StringBuffer(text.length());

			while (i18nMatcher.find())
			{
				i18nMatcher.appendReplacement(sb, I18n.format(i18nMatcher.group(1)));
			}

			i18nMatcher.appendTail(sb);
			text = sb.toString();
			i18nMatcher = I18N_PATTERN.matcher(text);
		}

		text = StringUtils.addFormatting(text.trim());

		if (StringUtils.unformatted(text).isEmpty())
		{
			return "";
		}

		return text;
	}

	@Override
	public void init()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(new QuestFileCacheReloader());
		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(new ThemeLoader());
		new FTBQuestsClientEventHandler().init();
	}

	private void setup(FMLClientSetupEvent event)
	{
		ClientRegistry.registerKeyBinding(KEY_QUESTS = new KeyBinding("key.ftbquests.quests", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM, -1, "key.categories.ftbquests"));
	}

	@Override
	public QuestFile getQuestFile(IWorld world)
	{
		return world.isRemote() ? ClientQuestFile.INSTANCE : ServerQuestFile.INSTANCE;
	}

	@Override
	@Nullable
	public QuestFile getQuestFile(Tristate clientSide)
	{
		if (clientSide.isDefault())
		{
			return getQuestFile(EffectiveSide.get().isClient() ? Tristate.TRUE : Tristate.FALSE);
		}

		return clientSide.isTrue() ? ClientQuestFile.INSTANCE : ServerQuestFile.INSTANCE;
	}

	@Override
	public void setTaskGuiProviders()
	{
		FTBQuestsTasks.ITEM.setGuiProvider((gui, quest, callback) -> {
			ConfigItemStack c = new ConfigItemStack(false, false);
			c.defaultValue = ItemStack.EMPTY;
			c.value = ItemStack.EMPTY;

			new GuiSelectItemStack(c, accepted -> {
				gui.run();
				if (accepted)
				{
					ItemTask itemTask = new ItemTask(quest);
					itemTask.item = ItemHandlerHelper.copyStackWithSize(c.value, 1);
					itemTask.count = c.value.getCount();
					callback.accept(itemTask);
				}
			}).openGui();
		});

		FTBQuestsTasks.FLUID.setGuiProvider((gui, quest, callback) -> {
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
		});

		FTBQuestsTasks.DIMENSION.setGuiProvider((gui, quest, callback) -> {
			DimensionTask task = new DimensionTask(quest);
			task.dimension = Minecraft.getInstance().world.getDimension().getType();
			callback.accept(task);
		});

		FTBQuestsTasks.LOCATION.setGuiProvider((gui, quest, callback) -> {
			LocationTask task = new LocationTask(quest);
			Minecraft mc = Minecraft.getInstance();

			if (mc.objectMouseOver instanceof BlockRayTraceResult)
			{
				TileEntity tileEntity = mc.world.getTileEntity(((BlockRayTraceResult) mc.objectMouseOver).getPos());

				if (tileEntity instanceof StructureBlockTileEntity)
				{
					BlockPos pos = ((StructureBlockTileEntity) tileEntity).getPosition();
					BlockPos size = ((StructureBlockTileEntity) tileEntity).getStructureSize();
					task.dimension = mc.world.getDimension().getType();
					task.x = pos.getX() + tileEntity.getPos().getX();
					task.y = pos.getY() + tileEntity.getPos().getY();
					task.z = pos.getZ() + tileEntity.getPos().getZ();
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
		FTBQuestsRewards.ITEM.setGuiProvider((gui, quest, callback) -> {
			ConfigItemStack c = new ConfigItemStack(false, false);
			c.defaultValue = ItemStack.EMPTY;
			c.value = ItemStack.EMPTY;

			new GuiSelectItemStack(c, accepted -> {
				if (accepted)
				{
					callback.accept(new ItemReward(quest, c.value));
				}
				gui.run();
			}).openGui();
		});

		FTBQuestsRewards.XP.setGuiProvider((gui, quest, callback) -> {
			ConfigInt c = new ConfigInt(1, Integer.MAX_VALUE);

			GuiEditConfigFromString.open(c, 100, 100, accepted -> {
				if (accepted)
				{
					callback.accept(new XPReward(quest, c.value));
				}

				gui.run();
			});
		});

		FTBQuestsRewards.XP_LEVELS.setGuiProvider((gui, quest, callback) -> {
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
}