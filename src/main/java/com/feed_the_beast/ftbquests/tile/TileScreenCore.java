package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.block.BlockFlags;
import com.feed_the_beast.ftblib.lib.config.ConfigBlockState;
import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigEnum;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.block.BlockScreen;
import com.feed_the_beast.ftbquests.net.MessageOpenTask;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.util.ProgressDisplayMode;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileScreenCore extends TileScreenBase implements IConfigCallback
{
	public EnumFacing facing;
	public short quest = 0;
	public int taskIndex = 0;
	public final ConfigString owner = new ConfigString("");
	public int size = 0;
	public final ConfigEnum<ProgressDisplayMode> progressDisplayMode = new ConfigEnum<>(ProgressDisplayMode.NAME_MAP);
	public final ConfigBoolean indestructible = new ConfigBoolean(false);
	public final ConfigBlockState skin = new ConfigBlockState(CommonUtils.AIR_STATE);

	private IProgressData cOwner;
	private QuestTask cTask;
	private QuestTaskData cTaskData;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		nbt.setString("Facing", getFacing().getName());
		writeScreenData(nbt);
	}

	private void writeScreenData(NBTTagCompound nbt)
	{
		if (!owner.isEmpty())
		{
			nbt.setString("Owner", owner.getString());
		}

		if (quest != 0)
		{
			nbt.setShort("Quest", quest);
		}

		if (taskIndex > 0)
		{
			nbt.setByte("TaskIndex", (byte) taskIndex);
		}

		if (size > 0)
		{
			nbt.setByte("Size", (byte) size);
		}

		if (!progressDisplayMode.isDefault())
		{
			nbt.setString("ProgressDisplayMode", progressDisplayMode.getString());
		}

		if (indestructible.getBoolean())
		{
			nbt.setBoolean("Indestructible", true);
		}

		if (!skin.isEmpty())
		{
			nbt.setString("Skin", skin.getString());
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		facing = EnumFacing.byName(nbt.getString("Facing"));
		readScreenData(nbt);
	}

	private void readScreenData(NBTTagCompound nbt)
	{
		owner.setString(nbt.getString("Owner"));
		quest = nbt.getShort("Quest");
		taskIndex = nbt.getByte("TaskIndex") & 0xFF;
		size = nbt.getByte("Size");
		progressDisplayMode.setValue(nbt.getString("ProgressDisplayMode"));
		indestructible.setBoolean(nbt.getBoolean("Indestructible"));
		skin.setValueFromString(nbt.getString("Skin"), false);
	}

	@Override
	public void writeToItem(ItemStack stack)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeScreenData(nbt);

		if (!nbt.isEmpty())
		{
			stack.setTagCompound(nbt);
		}
	}

	@Override
	public void readFromItem(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		readScreenData(nbt == null ? new NBTTagCompound() : nbt);
		updateContainingBlockInfo();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		cTaskData = getTaskData();

		if (cTaskData != null && cTaskData.task.getMaxProgress() > 0)
		{
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			{
				if (cTaskData.canInsertItem())
				{
					return true;
				}
			}
			else
			{
				if (cTaskData.hasCapability(capability, facing) && cTaskData.task.quest.canStartTasks(cTaskData.data))
				{
					return true;
				}
			}
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		cTaskData = getTaskData();

		if (cTaskData != null && cTaskData.task.getMaxProgress() > 0)
		{
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			{
				if (cTaskData.canInsertItem())
				{
					return (T) cTaskData;
				}
			}
			else
			{
				T object = cTaskData.getCapability(capability, facing);

				if (object != null && cTaskData.task.quest.canStartTasks(cTaskData.data))
				{
					return object;
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

			if (state.getBlock() == FTBQuestsItems.SCREEN)
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
	public TileScreenCore getScreen()
	{
		return this;
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		facing = null;
		cTask = null;
		cOwner = null;
		cTaskData = null;
	}

	@Nullable
	public IProgressData getOwner()
	{
		if (owner.isEmpty())
		{
			return null;
		}
		else if (cOwner == null)
		{
			cOwner = FTBQuests.PROXY.getQuestList(world.isRemote).getData(owner.getString());
		}

		return cOwner;
	}

	@Nullable
	public QuestTask getTask()
	{
		if (quest == 0)
		{
			return null;
		}
		else if (cTask == null || cTask.invalid)
		{
			Quest q = FTBQuests.PROXY.getQuestList(world.isRemote).getQuest(quest);
			cTask = q == null || q.invalid || q.tasks.isEmpty() ? null : q.getTask(taskIndex);
		}

		return cTask;
	}

	@Nullable
	public QuestTaskData getTaskData()
	{
		if (quest == 0 || owner.isEmpty())
		{
			return null;
		}
		else if (cTaskData == null || cTaskData.task.invalid)
		{
			cTask = getTask();

			if (cTask == null)
			{
				return null;
			}

			cOwner = getOwner();

			if (cOwner == null)
			{
				return null;
			}

			cTaskData = cOwner.getQuestTaskData(cTask.id);
		}

		return cTaskData;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return BlockScreen.getScreenAABB(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		double d = 32D * (2 + size);
		return d * d;
	}

	public boolean onClicked(EntityPlayer player, EnumHand hand, double x, double y)
	{
		if (player.isSneaking())
		{
			if (!world.isRemote)
			{
				boolean editor = FTBQuests.canEdit((EntityPlayerMP) player);
				boolean editorOrDestructible = editor || !indestructible.getBoolean();
				ConfigGroup group0 = ConfigGroup.newGroup("tile");
				group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.screen.name"));
				ConfigGroup group = group0.getGroup("ftbquests.screen");

				group.add("quest", new ConfigInt(quest & 0xFFFF), ConfigNull.INSTANCE).setCanEdit(editorOrDestructible).setDisplayName(new TextComponentTranslation("ftbquests.quest"));
				group.add("task_index", new ConfigInt(taskIndex, 0, 255), new ConfigInt(0)).setCanEdit(editorOrDestructible);

				if (editor)
				{
					group.add("owner", owner, new ConfigString("")).setDisplayName(new TextComponentTranslation("ftbquests.owner"));
				}

				group.add("skin", skin, new ConfigBlockState(CommonUtils.AIR_STATE)).setCanEdit(editorOrDestructible);
				group.add("progress_display_mode", progressDisplayMode, new ConfigEnum<>(ProgressDisplayMode.NAME_MAP));

				if (editor)
				{
					group.add("indestructible", indestructible, new ConfigBoolean(false));
				}

				FTBLibAPI.editServerConfig((EntityPlayerMP) player, group0, this);
			}

			return true;
		}

		if (y >= 0.81D)
		{
			progressDisplayMode.onClicked(MouseButton.LEFT);
			markDirty();
			return true;
		}

		cTaskData = getTaskData();

		if (cTaskData == null)
		{
			return false;
		}

		if (y >= 0D && y <= 0.17D && !indestructible.getBoolean() && cTaskData.task.quest.tasks.size() > 1)
		{
			Class currentCore = cTaskData.task.getScreenCoreClass();
			Class currentPart = cTaskData.task.getScreenPartClass();

			taskIndex++;

			if (taskIndex >= cTaskData.task.quest.tasks.size())
			{
				taskIndex = 0;
			}

			updateContainingBlockInfo();
			QuestTask task = getTaskData().task;

			if (currentCore != task.getScreenCoreClass() || currentPart != task.getScreenPartClass())
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
							TileScreenCore core = task.createScreenCore(world);
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
							TileScreenPart part = task.createScreenPart(world);
							part.setWorld(world);
							part.setPos(pos1);
							part.setOffset(offX, by, offZ);
							part.validate();
							world.setTileEntity(pos1, part);
						}
					}
				}
			}

			markDirty();
			return true;
		}

		String top1 = cTaskData.task.quest.getDisplayName().getUnformattedText();
		String top2 = cTaskData.task.getDisplayName().getUnformattedText();
		double iconY = 0.5D;

		if (!top1.isEmpty() && !top1.equalsIgnoreCase(top2))
		{
			iconY = 0.54D;
		}

		if (y >= iconY - 0.25D && y <= iconY + 0.25D)
		{
			if (!world.isRemote)
			{
				if (cTaskData.canInsertItem() && cTaskData.task.getMaxProgress() > 0L && cTaskData.getProgress() < cTaskData.task.getMaxProgress())
				{
					ItemStack stack = player.getHeldItem(hand);

					if (!stack.isEmpty())
					{
						ItemStack stack1 = cTaskData.insertItem(stack, false);

						if (stack != stack1)
						{
							player.setHeldItem(hand, stack1);
							return true;
						}
					}
				}

				MessageOpenTask.openGUI(cTaskData, (EntityPlayerMP) player, this);
			}

			return true;
		}

		return false;
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		updateContainingBlockInfo();
		markDirty();
		world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), BlockFlags.DEFAULT_AND_RERENDER);
	}

	@Override
	public void onLoad()
	{
		if (getBlockType() != FTBQuestsItems.SCREEN)
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