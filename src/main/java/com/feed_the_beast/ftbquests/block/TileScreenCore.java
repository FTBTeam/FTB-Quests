package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigEnum;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageOpenTask;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.util.ProgressDisplayMode;
import com.feed_the_beast.ftbquests.util.RedstoneOutputMode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileScreenCore extends TileScreenBase implements ITickable, IConfigCallback
{
	public EnumFacing facing;
	public short quest = 0;
	public int taskIndex = 0;
	public ConfigString owner = new ConfigString("");
	public int size = 0;
	public ConfigEnum<ProgressDisplayMode> progressDisplayMode = new ConfigEnum<>(ProgressDisplayMode.NAME_MAP);
	public ConfigEnum<RedstoneOutputMode> redstoneOutputMode = new ConfigEnum<>(RedstoneOutputMode.NAME_MAP);
	public int redstoneOutput = 0;
	public ConfigBoolean indestructible = new ConfigBoolean(false);

	private IProgressData cOwner;
	private QuestTask cTask;
	private QuestTaskData cTaskData;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		nbt.setString("Facing", getFacing().getName());

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

		if (!redstoneOutputMode.isDefault())
		{
			nbt.setString("RedstoneOutputMode", redstoneOutputMode.getString());
		}

		if (redstoneOutput > 0)
		{
			nbt.setByte("RedstoneOutput", (byte) redstoneOutput);
		}

		if (indestructible.getBoolean())
		{
			nbt.setBoolean("Indestructible", true);
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		facing = EnumFacing.byName(nbt.getString("Facing"));
		owner.setString(nbt.getString("Owner"));
		quest = nbt.getShort("Quest");
		taskIndex = nbt.getByte("TaskIndex") & 0xFF;
		size = nbt.getByte("Size");
		progressDisplayMode.setValue(nbt.getString("ProgressDisplayMode"));
		redstoneOutputMode.setValue(nbt.getString("RedstoneOutputMode"));
		redstoneOutput = nbt.getByte("RedstoneOutput");
		indestructible.setBoolean(nbt.getBoolean("Indestructible"));
	}

	@Override
	public void writeToItem(ItemStack stack)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("Owner", owner.getString());
		nbt.setShort("Quest", quest);
		nbt.setByte("TaskIndex", (byte) taskIndex);
		nbt.setByte("Size", (byte) size);

		if (!nbt.isEmpty())
		{
			stack.setTagCompound(nbt);
		}
	}

	@Override
	public void readFromItem(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt != null)
		{
			owner.setString(nbt.getString("Owner"));
			quest = nbt.getShort("Quest");
			taskIndex = nbt.getByte("TaskIndex") & 0xFF;
			size = nbt.getByte("Size");
		}

		updateContainingBlockInfo();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		cTaskData = getTaskData();
		return cTaskData != null && cTaskData.task.getMaxProgress() > 0 && cTaskData.hasCapability(capability, facing) || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		cTaskData = getTaskData();
		return cTaskData != null && cTaskData.task.getMaxProgress() > 0 ? cTaskData.getCapability(capability, facing) : super.getCapability(capability, facing);
	}

	public EnumFacing getFacing()
	{
		if (facing == null)
		{
			IBlockState state = getBlockState();

			if (state.getBlock() instanceof BlockScreen)
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
		Block block = getBlockType();
		return BlockScreen.getScreenAABB(this, block instanceof BlockScreen && ((BlockScreen) block).flat);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		double d = 32D * (2 + size);
		return d * d;
	}

	public boolean onClicked(EntityPlayer player, double x, double y)
	{
		if (player.isSneaking())
		{
			if (!world.isRemote)
			{
				boolean editor = FTBQuests.canEdit((EntityPlayerMP) player);
				boolean editorOrDestructible = editor || !indestructible.getBoolean();
				ConfigGroup group0 = ConfigGroup.newGroup("tile");
				ConfigGroup group = group0.getGroup("ftbquests.screen");
				group.setDisplayName(new TextComponentTranslation("tile.ftbquests.screen.name"));

				group.add("quest", new ConfigInt(quest & 0xFFFF), ConfigNull.INSTANCE).setCanEdit(editorOrDestructible).setDisplayName(new TextComponentTranslation("ftbquests.quest"));
				group.add("task_index", new ConfigInt(taskIndex, 0, 255), new ConfigInt(0)).setCanEdit(editorOrDestructible);

				if (editor)
				{
					group.add("size", new ConfigInt(size, 0, 4), new ConfigInt(0));
					group.add("owner", owner, new ConfigString("")).setDisplayName(new TextComponentTranslation("ftbquests.owner"));
				}

				group.add("progress_display_mode", progressDisplayMode, new ConfigEnum<>(ProgressDisplayMode.NAME_MAP));
				group.add("redstone_output_mode", redstoneOutputMode, new ConfigEnum<>(RedstoneOutputMode.NAME_MAP)).setCanEdit(editorOrDestructible);

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
				MessageOpenTask.openGUI(cTaskData, (EntityPlayerMP) player, this);
			}

			return true;
		}

		return false;
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		markDirty();
	}

	@Override
	public void update()
	{
		if (world.getTotalWorldTime() % 20 == 0L)
		{
			int rout = redstoneOutput;
			redstoneOutput = 0;

			if (!redstoneOutputMode.isDefault())
			{
				cTaskData = getTaskData();

				if (cTaskData != null)
				{
					int progress = cTaskData.getProgress();

					if (progress > 0)
					{
						int max = cTaskData.task.getMaxProgress();

						if (progress >= max)
						{
							redstoneOutput = 15;
						}
						else if (redstoneOutputMode.getValue() == RedstoneOutputMode.LEVEL)
						{
							redstoneOutput = (int) MathUtils.map(0, max, 1, 15, progress);
						}
					}
				}
			}

			if (rout != redstoneOutput)
			{
				EnumFacing opposite = getFacing().getOpposite();
				boolean xaxis = opposite.getAxis() == EnumFacing.Axis.X;

				for (int y = 0; y < size * 2 + 1; y++)
				{
					for (int x = -size; x <= size; x++)
					{
						int offX = xaxis ? 0 : x;
						int offZ = xaxis ? x : 0;
						BlockPos pos1 = new BlockPos(pos.getX() + offX, pos.getY() + y, pos.getZ() + offZ);

						BlockPos pos2 = pos1.offset(opposite);

						if (world.isBlockLoaded(pos2))
						{
							IBlockState state = world.getBlockState(pos2);

							state.getBlock().onNeighborChange(world, pos2, pos1);
							if (state.getBlock().isNormalCube(state, world, pos2))
							{
								pos2 = pos2.offset(opposite);
								state = world.getBlockState(pos2);

								if (state.getBlock().getWeakChanges(world, pos2))
								{
									state.getBlock().onNeighborChange(world, pos2, pos1);
								}
							}
						}
					}
				}

				markDirty();
			}
		}

		checkIfDirty();
	}
}