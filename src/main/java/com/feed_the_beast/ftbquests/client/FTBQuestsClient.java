package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectFluid;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectItemStack;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsCommon;
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
import com.latmod.mods.itemfilters.filters.NBTMatchingMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.lwjgl.input.Keyboard;

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
	public void preInit()
	{
		ClientRegistry.registerKeyBinding(KEY_QUESTS = new KeyBinding("key.ftbquests.quests", KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_NONE, FTBLib.KEY_CATEGORY));

		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener) (manager, predicate) -> {
			if (ClientQuestFile.exists())
			{
				ClientQuestFile.INSTANCE.clearCachedData();
			}
		});
	}

	@Override
	@Nullable
	public QuestFile getQuestFile(@Nullable World world)
	{
		return getQuestFile(world == null ? FMLCommonHandler.instance().getEffectiveSide().isClient() : world.isRemote);
	}

	@Override
	@Nullable
	public QuestFile getQuestFile(boolean clientSide)
	{
		return clientSide ? ClientQuestFile.INSTANCE : ServerQuestFile.INSTANCE;
	}

	@Override
	public void setTaskGuiProviders()
	{
		FTBQuestsTasks.ITEM.setGuiProvider((gui, quest, callback) -> new GuiSelectItemStack(gui, stack -> {
			if (!stack.isEmpty())
			{
				ItemTask itemTask = new ItemTask(quest);
				itemTask.items.add(ItemHandlerHelper.copyStackWithSize(stack, 1));
				itemTask.count = stack.getCount();

				if (!stack.isStackable())
				{
					itemTask.nbtMode = NBTMatchingMode.IGNORE;
					itemTask.ignoreDamage = !stack.getHasSubtypes();
				}

				callback.accept(itemTask);
			}
		}).openGui());

		FTBQuestsTasks.FLUID.setGuiProvider((gui, quest, callback) -> new GuiSelectFluid(gui, () -> FluidRegistry.WATER, fluid -> {
			if (fluid != null)
			{
				FluidTask fluidTask = new FluidTask(quest);
				fluidTask.fluid = fluid;
				callback.accept(fluidTask);
			}
		}).openGui());

		FTBQuestsTasks.DIMENSION.setGuiProvider((gui, quest, callback) -> {
			DimensionTask task = new DimensionTask(quest);
			task.dimension = Minecraft.getMinecraft().world.provider.getDimension();
			callback.accept(task);
		});

		FTBQuestsTasks.LOCATION.setGuiProvider((gui, quest, callback) -> {
			LocationTask task = new LocationTask(quest);
			Minecraft mc = Minecraft.getMinecraft();

			if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
			{
				TileEntity tileEntity = mc.world.getTileEntity(mc.objectMouseOver.getBlockPos());

				if (tileEntity instanceof TileEntityStructure)
				{
					BlockPos pos = ((TileEntityStructure) tileEntity).getPosition();
					BlockPos size = ((TileEntityStructure) tileEntity).getStructureSize();
					task.dimension = mc.world.provider.getDimension();
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

			ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
			task.getConfig(task.createSubGroup(group));
			new GuiEditConfig(group, (g1, sender) -> callback.accept(task)).openGui();
		});
	}

	@Override
	public void setRewardGuiProviders()
	{
		FTBQuestsRewards.ITEM.setGuiProvider((gui, quest, callback) -> new GuiSelectItemStack(gui, stack -> {
			if (!stack.isEmpty())
			{
				ItemReward reward = new ItemReward(quest);
				reward.stack = stack;
				callback.accept(reward);
			}
		}).openGui());

		FTBQuestsRewards.XP.setGuiProvider((gui, quest, callback) -> new GuiEditConfigValue("xp", new ConfigInt(100, 1, Integer.MAX_VALUE), (value, set) -> {
			gui.openGui();
			if (set)
			{
				XPReward reward = new XPReward(quest);
				reward.xp = value.getInt();
				callback.accept(reward);
			}
		}).openGui());

		FTBQuestsRewards.XP_LEVELS.setGuiProvider((gui, quest, callback) -> new GuiEditConfigValue("xp_levels", new ConfigInt(1, 1, Integer.MAX_VALUE), (value, set) -> {
			gui.openGui();
			if (set)
			{
				XPLevelsReward reward = new XPLevelsReward(quest);
				reward.xpLevels = value.getInt();
				callback.accept(reward);
			}
		}).openGui());
	}
}