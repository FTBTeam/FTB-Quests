package com.feed_the_beast.ftbquests.tile;

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
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.block.BlockScreen;
import com.feed_the_beast.ftbquests.net.MessageOpenTask;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
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
	public final ConfigString team = new ConfigString("");
	public final ConfigQuestObject quest = new ConfigQuestObject("").addType(QuestObjectType.QUEST);
	public final ConfigInt taskIndex = new ConfigInt(0, 0, 255);
	public int size = 0;
	public final ConfigEnum<ProgressDisplayMode> progressDisplayMode = new ConfigEnum<>(ProgressDisplayMode.NAME_MAP);
	public final ConfigBoolean indestructible = new ConfigBoolean(false);
	public final ConfigBlockState skin = new ConfigBlockState(BlockUtils.AIR_STATE);

	private IProgressData cTeam;
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
		if (!team.isEmpty())
		{
			nbt.setString("Team", team.getString());
		}

		cTask = getTask();

		if (cTask != null)
		{
			quest.setString(cTask.quest.getID());
		}

		nbt.setString("Quest", quest.getString());

		if (taskIndex.getInt() > 0)
		{
			nbt.setByte("TaskIndex", (byte) taskIndex.getInt());
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
		String t = nbt.getString("Team");

		if (t.isEmpty())
		{
			t = nbt.getString("Owner");
		}

		team.setString(t);
		quest.setString(nbt.getString("Quest"));
		taskIndex.setInt(nbt.getByte("TaskIndex") & 0xFF);
		size = nbt.getByte("Size");
		progressDisplayMode.setValue(nbt.getString("ProgressDisplayMode"));
		indestructible.setBoolean(nbt.getBoolean("Indestructible"));
		skin.setValueFromString(null, nbt.getString("Skin"), false);
		updateContainingBlockInfo();
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
		cTeam = null;
		cTaskData = null;
	}

	@Nullable
	public IProgressData getTeam()
	{
		if (team.isEmpty())
		{
			return null;
		}
		else if (cTeam == null)
		{
			cTeam = FTBQuests.PROXY.getQuestList(world).getData(team.getString());
		}

		return cTeam;
	}

	@Nullable
	public QuestTask getTask()
	{
		if (quest.isEmpty())
		{
			return null;
		}
		else if (cTask == null || cTask.invalid)
		{
			Quest q = FTBQuests.PROXY.getQuestList(world).getQuest(quest.getString());
			cTask = q == null || q.tasks.isEmpty() ? null : q.getTask(taskIndex.getInt());
		}

		return cTask;
	}

	@Nullable
	public QuestTaskData getTaskData()
	{
		if (quest.isEmpty() || team.isEmpty())
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

			cTeam = getTeam();

			if (cTeam == null)
			{
				return null;
			}

			cTaskData = cTeam.getQuestTaskData(cTask);
		}

		return cTaskData;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return BlockScreen.getScreenAABB(pos, getFacing(), size);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		double d = 32D * (2 + size);
		return d * d;
	}

	public boolean isOwner(EntityPlayer player)
	{
		return team.isEmpty() || FTBLibAPI.getTeam(player.getUniqueID()).equals(team.getString());
	}

	public void onClicked(EntityPlayerMP player, EnumHand hand, double x, double y)
	{
		boolean editor = FTBQuests.canEdit(player);

		if (player.isSneaking())
		{
			if (editor || isOwner(player))
			{
				cTask = getTask();

				if (cTask != null)
				{
					quest.setString(cTask.quest.getID());
				}

				boolean editorOrDestructible = editor || !indestructible.getBoolean();
				ConfigGroup group0 = ConfigGroup.newGroup("tile");
				group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.screen.name"));
				ConfigGroup group = group0.getGroup("ftbquests.screen");

				group.add("quest", quest, ConfigNull.INSTANCE).setCanEdit(editorOrDestructible).setDisplayName(new TextComponentTranslation("ftbquests.quest"));
				group.add("task_index", taskIndex, new ConfigInt(0)).setCanEdit(editorOrDestructible);

				if (editor)
				{
					group.add("team", team, new ConfigString("")).setDisplayName(new TextComponentTranslation("ftbquests.team"));
				}

				group.add("skin", skin, new ConfigBlockState(BlockUtils.AIR_STATE)).setCanEdit(editorOrDestructible);
				group.add("progress_display_mode", progressDisplayMode, new ConfigEnum<>(ProgressDisplayMode.NAME_MAP));

				if (editor)
				{
					group.add("indestructible", indestructible, new ConfigBoolean(false));
				}

				FTBLibAPI.editServerConfig(player, group0, this);
			}

			return;
		}

		if (y >= 0.81D)
		{
			if (editor || isOwner(player))
			{
				progressDisplayMode.onClicked(MouseButton.LEFT);
				markDirty();
			}

			return;
		}

		cTaskData = getTaskData();

		if (cTaskData == null)
		{
			return;
		}

		if (y >= 0D && y <= 0.17D && !indestructible.getBoolean() && cTaskData.task.quest.tasks.size() > 1)
		{
			if (!editor && !isOwner(player))
			{
				return;
			}

			Class currentCore = cTaskData.task.getScreenCoreClass();
			Class currentPart = cTaskData.task.getScreenPartClass();

			if (taskIndex.getInt() >= cTaskData.task.quest.tasks.size() - 1)
			{
				taskIndex.setInt(0);
			}
			else
			{
				taskIndex.setInt(taskIndex.getInt() + 1);
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
			return;
		}

		if (!isOwner(player))
		{
			return;
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
						ItemStack stack1 = cTaskData.insertItem(stack, false, false);

						if (stack != stack1)
						{
							player.setHeldItem(hand, stack1);
							return;
						}
					}
				}

				MessageOpenTask.openGUI(cTaskData, player, this);
			}
		}
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		updateContainingBlockInfo();
		markDirty();
		BlockUtils.notifyBlockUpdate(world, pos, getBlockState());
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