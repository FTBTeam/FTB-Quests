package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.block.BlockFlags;
import com.feed_the_beast.ftblib.lib.config.ConfigBlockState;
import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.block.BlockScreen;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileProgressScreenCore extends TileProgressScreenBase implements IConfigCallback
{
	public EnumFacing facing;
	public final ConfigString team = new ConfigString("");
	public final ConfigString object = new ConfigString("");
	public int size = 0;
	public final ConfigBoolean indestructible = new ConfigBoolean(false);
	public final ConfigBlockState skin = new ConfigBlockState(CommonUtils.AIR_STATE);

	private IProgressData cOwner;
	private ProgressingQuestObject cObject;

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

		cObject = getObject();

		if (cObject != null)
		{
			object.setString(cObject.getID());
		}

		nbt.setString("Quest", object.getString());

		if (size > 0)
		{
			nbt.setByte("Size", (byte) size);
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
		team.setString(nbt.getString("Owner"));
		object.setString(nbt.getString("Quest"));
		size = nbt.getByte("Size");
		indestructible.setBoolean(nbt.getBoolean("Indestructible"));
		skin.setValueFromString(nbt.getString("Skin"), false);
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

	public EnumFacing getFacing()
	{
		if (facing == null)
		{
			IBlockState state = getBlockState();

			if (state.getBlock() == FTBQuestsItems.PROGRESS_SCREEN)
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
	public TileProgressScreenCore getScreen()
	{
		return this;
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		facing = null;
		cObject = null;
		cOwner = null;
	}

	@Nullable
	public IProgressData getTeam()
	{
		if (team.isEmpty())
		{
			return null;
		}
		else if (cOwner == null)
		{
			cOwner = FTBQuests.PROXY.getQuestList(world).getData(team.getString());
		}

		return cOwner;
	}

	@Nullable
	public ProgressingQuestObject getObject()
	{
		if (object.isEmpty())
		{
			return null;
		}
		else if (cObject == null || cObject.invalid)
		{
			QuestObject q = FTBQuests.PROXY.getQuestList(world).get(object.getString());
			cObject = q instanceof ProgressingQuestObject && !q.invalid ? cObject : null;
		}

		return cObject;
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

	public void onClicked(EntityPlayerMP player, double x, double y)
	{
		boolean editor = FTBQuests.canEdit(player);

		if (player.isSneaking())
		{
			if (editor || isOwner(player))
			{
				cObject = getObject();

				if (cObject != null)
				{
					object.setString(cObject.getID());
				}

				boolean editorOrDestructible = editor || !indestructible.getBoolean();
				ConfigGroup group0 = ConfigGroup.newGroup("tile");
				group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.screen.name"));
				ConfigGroup group = group0.getGroup("ftbquests.screen");

				group.add("quest", object, ConfigNull.INSTANCE).setCanEdit(editorOrDestructible).setDisplayName(new TextComponentTranslation("ftbquests.quest"));

				if (editor)
				{
					group.add("team", team, new ConfigString("")).setDisplayName(new TextComponentTranslation("ftbquests.owner"));
				}

				group.add("skin", skin, new ConfigBlockState(CommonUtils.AIR_STATE)).setCanEdit(editorOrDestructible);

				if (editor)
				{
					group.add("indestructible", indestructible, new ConfigBoolean(false));
				}

				FTBLibAPI.editServerConfig(player, group0, this);
			}
		}
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