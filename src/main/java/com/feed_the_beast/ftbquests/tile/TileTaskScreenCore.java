package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigBlockState;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.block.BlockTaskScreen;
import com.feed_the_beast.ftbquests.block.FTBQuestsBlocks;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import com.latmod.mods.itemfilters.api.PaintAPI;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileTaskScreenCore extends TileWithTeam implements IConfigCallback, ITaskScreen
{
	public EnumFacing facing;
	public int task = 0;
	public int size = 0;
	public IBlockState skin = BlockUtils.AIR_STATE;
	public boolean inputOnly = false;
	public ItemStack inputModeIcon = ItemStack.EMPTY;

	private Class currentCoreClass, currentPartClass;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		super.writeData(nbt, type);

		if (!type.item)
		{
			nbt.setString("Facing", getFacing().getName());
		}

		if (task != 0)
		{
			nbt.setInteger("Task", task);
		}

		if (size > 0)
		{
			nbt.setByte("Size", (byte) size);
		}

		if (skin != BlockUtils.AIR_STATE)
		{
			nbt.setString("Skin", BlockUtils.getNameFromState(skin));
		}

		if (inputOnly)
		{
			nbt.setBoolean("InputOnly", true);
		}

		if (!inputModeIcon.isEmpty())
		{
			nbt.setTag("InputModeIcon", inputModeIcon.serializeNBT());
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		super.readData(nbt, type);

		if (!type.item)
		{
			facing = EnumFacing.byName(nbt.getString("Facing"));
		}

		task = nbt.getInteger("Task");
		size = nbt.getByte("Size");
		skin = BlockUtils.getStateFromName(nbt.getString("Skin"));
		inputOnly = nbt.getBoolean("InputOnly");
		inputModeIcon = new ItemStack(nbt.getCompoundTag("InputModeIcon"));

		if (inputModeIcon.isEmpty())
		{
			inputModeIcon = ItemStack.EMPTY;
		}

		updateContainingBlockInfo();
	}

	public final void resetData()
	{
		team = "";
		indestructible = false;
		facing = null;
		task = 0;
		size = 0;
		skin = BlockUtils.AIR_STATE;
		inputOnly = false;
		inputModeIcon = ItemStack.EMPTY;
		updateContainingBlockInfo();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if (capability == PaintAPI.CAPABILITY)
		{
			return true;
		}

		if (world != null && !world.isRemote)
		{
			TaskData t = getTaskData();

			if (t == null || !t.task.quest.canStartTasks(t.data))
			{
				return false;
			}

			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && t.task.canInsertItem())
			{
				return true;
			}
			else if (t.hasCapability(capability, facing))
			{
				return true;
			}
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if (capability == PaintAPI.CAPABILITY)
		{
			return (T) this;
		}

		if (world != null && !world.isRemote)
		{
			TaskData t = getTaskData();

			if (t == null || !t.task.quest.canStartTasks(t.data))
			{
				return null;
			}

			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && t.task.canInsertItem())
			{
				return (T) t;
			}
			else
			{
				Object object = t.getCapability(capability, facing);

				if (object != null)
				{
					return (T) object;
				}
			}
		}

		return super.getCapability(capability, facing);
	}

	public EnumFacing getFacing()
	{
		if (facing == null)
		{
			IBlockState state = getBlockState();

			if (state.getBlock() == FTBQuestsBlocks.SCREEN)
			{
				facing = getBlockState().getValue(BlockHorizontal.FACING);
			}
			else
			{
				facing = EnumFacing.NORTH;
			}
		}

		return facing;
	}

	@Override
	public TileTaskScreenCore getScreen()
	{
		return this;
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		facing = null;
	}

	@Override
	protected boolean notifyBlock()
	{
		return !world.isRemote;
	}

	@Nullable
	public TaskData getTaskData()
	{
		if (task == 0 || team.isEmpty())
		{
			return null;
		}

		QuestFile file = FTBQuests.PROXY.getQuestFile(world);

		if (file == null)
		{
			return null;
		}

		Task t = file.getTask(task);

		if (t == null)
		{
			return null;
		}

		QuestData data = file.getData(team);

		if (data == null)
		{
			return null;
		}

		return data.getTaskData(t);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return BlockTaskScreen.getScreenAABB(pos, getFacing(), size);
	}

	@Override
	public double getMaxRenderDistanceSquared()
	{
		double d = 32D * (2 + size);
		return d * d;
	}

	public void onClicked(EntityPlayerMP player, EnumHand hand, double x, double y)
	{
		boolean editor = FTBQuests.canEdit(player);

		if (editor || isOwner(player))
		{
			TaskData taskData = getTaskData();

			if (taskData != null)
			{
				task = taskData.task.id;
				currentCoreClass = taskData.task.getScreenCoreClass();
				currentPartClass = taskData.task.getScreenPartClass();
			}
			else
			{
				currentCoreClass = TileTaskScreenCore.class;
				currentPartClass = TileTaskScreenPart.class;
			}

			boolean editorOrDestructible = editor || !indestructible;
			ConfigGroup group0 = ConfigGroup.newGroup("tile");
			group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.screen.name"));
			ConfigGroup config = group0.getGroup("ftbquests.screen");

			if (editor)
			{
				config.add("team", createTeamConfig(), ConfigNull.INSTANCE).setDisplayName(new TextComponentTranslation("ftbquests.team"));
			}

			config.add("task", new ConfigQuestObject(ServerQuestFile.INSTANCE, task, o -> o instanceof Task && ((Task) o).consumesResources())
			{
				@Override
				public void setObject(int v)
				{
					task = v;
				}
			}, ConfigNull.INSTANCE).setCanEdit(editorOrDestructible).setDisplayName(new TextComponentTranslation("ftbquests.task"));

			config.add("skin", new ConfigBlockState(skin)
			{
				@Override
				public void setBlockState(IBlockState v)
				{
					skin = v;
				}
			}, new ConfigBlockState(BlockUtils.AIR_STATE)).setCanEdit(editorOrDestructible);

			if (editor)
			{
				config.addBool("indestructible", () -> indestructible, v -> indestructible = v, false);
			}

			config.addBool("input_only", () -> inputOnly, v -> inputOnly = v, false);
			config.add("input_mode_icon", new ConfigItemStack.SimpleStack(() -> inputModeIcon, v -> inputModeIcon = v), new ConfigItemStack(ItemStack.EMPTY));

			FTBLibAPI.editServerConfig(player, group0, this);
		}
	}

	public void updateTiles(@Nullable Task task)
	{
		boolean xaxis = getFacing().getAxis() == EnumFacing.Axis.X;

		for (int by = 0; by < size * 2 + 1; by++)
		{
			for (int bx = -size; bx <= size; bx++)
			{
				int offX = xaxis ? 0 : bx;
				int offZ = xaxis ? bx : 0;
				BlockPos pos1 = new BlockPos(pos.getX() + offX, pos.getY() + by, pos.getZ() + offZ);
				world.removeTileEntity(pos1);

				if (bx == 0 && by == 0)
				{
					TileTaskScreenCore core = task == null ? new TileTaskScreenCore() : task.createScreenCore(world);
					core.setWorld(world);
					core.setPos(pos1);
					NBTTagCompound nbt = new NBTTagCompound();
					writeData(nbt, EnumSaveType.SAVE);
					core.readData(nbt, EnumSaveType.SAVE);
					core.validate();
					world.setTileEntity(pos1, core);
				}
				else
				{
					TileTaskScreenPart part = task == null ? new TileTaskScreenPart() : task.createScreenPart(world);
					part.setWorld(world);
					part.setPos(pos1);
					part.setOffset(offX, by, offZ);
					part.validate();
					world.setTileEntity(pos1, part);
				}
			}
		}
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		updateContainingBlockInfo();
		markDirty();
		BlockUtils.notifyBlockUpdate(world, pos, getBlockState());

		TaskData taskData = getTaskData();

		if (taskData != null && (currentCoreClass != taskData.task.getScreenCoreClass() || currentPartClass != taskData.task.getScreenPartClass()))
		{
			updateTiles(taskData.task);
		}
	}

	@Override
	public void onLoad()
	{
		if (getBlockType() != FTBQuestsBlocks.SCREEN)
		{
			boolean xaxis = getFacing().getAxis() == EnumFacing.Axis.X;

			for (int y = 0; y < size * 2 + 1; y++)
			{
				for (int x = -size; x <= size; x++)
				{
					int offX = xaxis ? 0 : x;
					int offZ = xaxis ? x : 0;
					world.setBlockToAir(new BlockPos(pos.getX() + offX, pos.getY() + y, pos.getZ() + offZ));
				}
			}
		}

		markDirty();
	}

	@Override
	public void markDirty()
	{
		sendDirtyUpdate();
	}
}