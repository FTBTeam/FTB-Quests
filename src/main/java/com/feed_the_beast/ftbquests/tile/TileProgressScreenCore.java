package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigBlockState;
import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.ConfigTeam;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsBlocks;
import com.feed_the_beast.ftbquests.block.BlockProgressScreen;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
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

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileProgressScreenCore extends TileProgressScreenBase implements IConfigCallback
{
	public EnumFacing facing;
	public String team = "";
	public String chapter = "";
	public int width = 0, height = 0;
	public boolean indestructible = false;
	public IBlockState skin = BlockUtils.AIR_STATE;
	public boolean fullscreen = false;
	public boolean hideIcons = false;

	private ITeamData cOwner;
	private QuestChapter cChapter;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!type.item)
		{
			nbt.setString("Facing", getFacing().getName());
		}

		if (!team.isEmpty())
		{
			nbt.setString("Team", team);
		}

		cChapter = getChapter();

		if (cChapter != null)
		{
			chapter = cChapter.getID();
		}

		nbt.setString("Chapter", chapter);

		if (width > 0)
		{
			nbt.setByte("Width", (byte) width);
		}

		if (height > 0)
		{
			nbt.setByte("Height", (byte) height);
		}

		if (indestructible)
		{
			nbt.setBoolean("Indestructible", true);
		}

		if (skin != BlockUtils.AIR_STATE)
		{
			nbt.setString("Skin", BlockUtils.getNameFromState(skin));
		}

		if (fullscreen)
		{
			nbt.setBoolean("Fullscreen", true);
		}

		if (hideIcons)
		{
			nbt.setBoolean("HideIcons", true);
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!type.item)
		{
			facing = EnumFacing.byName(nbt.getString("Facing"));
		}

		team = nbt.getString("Team");
		chapter = nbt.getString("Chapter");
		width = nbt.getByte("Width");
		height = nbt.getByte("Height");
		indestructible = nbt.getBoolean("Indestructible");
		skin = BlockUtils.getStateFromName(nbt.getString("Skin"));
		fullscreen = nbt.getBoolean("Fullscreen");
		hideIcons = nbt.getBoolean("HideIcons");
		updateContainingBlockInfo();
	}

	@Override
	public void writeToItem(ItemStack stack)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeData(nbt, EnumSaveType.ITEM);

		if (!nbt.isEmpty())
		{
			stack.setTagCompound(nbt);
		}
	}

	@Override
	public void readFromItem(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		readData(nbt == null ? new NBTTagCompound() : nbt, EnumSaveType.ITEM);
	}

	public EnumFacing getFacing()
	{
		if (facing == null)
		{
			IBlockState state = getBlockState();

			if (state.getBlock() == FTBQuestsBlocks.PROGRESS_SCREEN)
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
		cChapter = null;
		cOwner = null;
	}

	@Nullable
	public ITeamData getTeam()
	{
		if (team.isEmpty())
		{
			return null;
		}
		else if (cOwner == null)
		{
			cOwner = FTBQuests.PROXY.getQuestFile(world).getData(team);
		}

		return cOwner;
	}

	@Nullable
	public QuestChapter getChapter()
	{
		if (chapter.isEmpty())
		{
			return null;
		}
		else if (cChapter == null || cChapter.invalid)
		{
			cChapter = FTBQuests.PROXY.getQuestFile(world).getChapter(chapter);
		}

		return cChapter;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return BlockProgressScreen.getScreenAABB(pos, getFacing(), width, height);
	}

	@Override
	public double getMaxRenderDistanceSquared()
	{
		double d = 32D * (2 + height);
		return d * d;
	}

	public boolean isOwner(EntityPlayer player)
	{
		return team.isEmpty() || FTBLibAPI.getTeam(player.getUniqueID()).equals(team);
	}

	public void onClicked(EntityPlayerMP player, double x, double y)
	{
		boolean editor = FTBQuests.canEdit(player);

		if (player.isSneaking())
		{
			if (editor || isOwner(player))
			{
				cChapter = getChapter();

				if (cChapter != null)
				{
					chapter = cChapter.getID();
				}

				boolean editorOrDestructible = editor || !indestructible;
				ConfigGroup group0 = ConfigGroup.newGroup("tile");
				group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.progress_screen.name"));
				ConfigGroup group = group0.getGroup("ftbquests.screen");

				if (editor)
				{
					group.add("team", new ConfigTeam(team)
					{
						@Override
						public void setString(String v)
						{
							team = v;
						}
					}, ConfigNull.INSTANCE).setDisplayName(new TextComponentTranslation("ftbquests.team"));
				}

				group.add("chapter", new ConfigQuestObject(chapter)
				{
					@Override
					public void setString(String v)
					{
						chapter = v;
					}
				}.addType(QuestObjectType.CHAPTER), ConfigNull.INSTANCE).setCanEdit(editorOrDestructible).setDisplayName(new TextComponentTranslation("ftbquests.chapter"));

				group.add("skin", new ConfigBlockState(skin)
				{
					@Override
					public void setBlockState(IBlockState v)
					{
						skin = v;
					}
				}, new ConfigBlockState(BlockUtils.AIR_STATE)).setCanEdit(editorOrDestructible);

				group.add("fullscreen", new ConfigBoolean(fullscreen)
				{
					@Override
					public void setBoolean(boolean v)
					{
						fullscreen = v;
					}
				}, new ConfigBoolean(false)).setCanEdit(editorOrDestructible);

				group.add("hide_icons", new ConfigBoolean(hideIcons)
				{
					@Override
					public void setBoolean(boolean v)
					{
						hideIcons = v;
					}
				}, new ConfigBoolean(false)).setCanEdit(editorOrDestructible);

				if (editor)
				{
					group.add("indestructible", new ConfigBoolean(indestructible)
					{
						@Override
						public void setBoolean(boolean v)
						{
							indestructible = v;
						}
					}, new ConfigBoolean(false));
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
		BlockUtils.notifyBlockUpdate(world, pos, getBlockState());
	}

	@Override
	public void onLoad()
	{
		if (getBlockType() != FTBQuestsBlocks.PROGRESS_SCREEN)
		{
			boolean xaxis = getFacing().getAxis() == EnumFacing.Axis.X;

			for (int y = 0; y < height + 1; y++)
			{
				for (int x = -width; x <= width; x++)
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