package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectFluid;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectItemStack;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.ServerUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
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
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.DimensionType;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.items.ItemHandlerHelper;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;

public class FTBQuestsClient extends FTBQuestsCommon
{
	public static KeyBinding KEY_QUESTS;

	@Override
	public void preInit()
	{
		ClientRegistry.registerKeyBinding(KEY_QUESTS = new KeyBinding("key.ftbquests.quests", KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_NONE, FTBLib.KEY_CATEGORY));
	}

	@Override
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
			GuiButtonListBase g = new GuiButtonListBase()
			{
				@Override
				public void addButtons(Panel panel)
				{
					for (DimensionType type : DimensionType.values())
					{
						panel.add(new SimpleTextButton(panel, ServerUtils.getDimensionName(type.getId()).getFormattedText(), Icon.EMPTY)
						{
							@Override
							public void onClicked(MouseButton button)
							{
								gui.openGui();
								DimensionTask task = new DimensionTask(quest);
								task.dimension = type.getId();
								callback.accept(task);
							}
						});
					}
				}
			};

			g.focus();
			g.openGui();
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

	@Override
	@Nullable
	public Advancement getAdvancement(@Nullable MinecraftServer server, String id)
	{
		if (server == null && !id.isEmpty() && Minecraft.getMinecraft().player != null)
		{
			return Minecraft.getMinecraft().player.connection.getAdvancementManager().getAdvancementList().getAdvancement(new ResourceLocation(id));
		}

		return super.getAdvancement(server, id);
	}
}