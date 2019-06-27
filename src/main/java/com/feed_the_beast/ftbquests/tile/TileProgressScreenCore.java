package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigBlockState;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.block.BlockProgressScreen;
import com.feed_the_beast.ftbquests.block.FTBQuestsBlocks;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import com.latmod.mods.itemfilters.api.PaintAPI;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class TileProgressScreenCore extends TileWithTeam implements IConfigCallback, IProgressScreen
{
	public EnumFacing facing;
	public int chapter = 0;
	public int width = 0, height = 0;
	public IBlockState skin = BlockUtils.AIR_STATE;
	public boolean fullscreen = false;
	public boolean hideIcons = false;

	private QuestChapter cChapter;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		super.writeData(nbt, type);

		if (!type.item)
		{
			nbt.setString("Facing", getFacing().getName());
		}

		cChapter = getChapter();

		if (cChapter != null)
		{
			chapter = cChapter.id;
		}

		if (chapter != 0)
		{
			nbt.setInteger("Chapter", chapter);
		}

		if (width > 0)
		{
			nbt.setByte("Width", (byte) width);
		}

		if (height > 0)
		{
			nbt.setByte("Height", (byte) height);
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
		super.readData(nbt, type);

		if (!type.item)
		{
			facing = EnumFacing.byName(nbt.getString("Facing"));
		}

		chapter = nbt.getInteger("Chapter");
		width = nbt.getByte("Width");
		height = nbt.getByte("Height");
		skin = BlockUtils.getStateFromName(nbt.getString("Skin"));
		fullscreen = nbt.getBoolean("Fullscreen");
		hideIcons = nbt.getBoolean("HideIcons");
		updateContainingBlockInfo();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if (capability == PaintAPI.CAPABILITY)
		{
			return true;
		}

		return super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if (capability == PaintAPI.CAPABILITY)
		{
			return (T) this;
		}

		return super.getCapability(capability, facing);
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
	}

	@Override
	protected boolean notifyBlock()
	{
		return !world.isRemote;
	}

	@Nullable
	public QuestChapter getChapter()
	{
		if (chapter == 0)
		{
			return null;
		}
		else if (cChapter == null || cChapter.invalid)
		{
			QuestFile file = FTBQuests.PROXY.getQuestFile(world);

			if (file == null)
			{
				return null;
			}

			cChapter = file.getChapter(chapter);
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

	public void onClicked(EntityPlayerMP player, double x, double y)
	{
		boolean editor = FTBQuests.canEdit(player);

		if (player.isSneaking())
		{
			if (editor || isOwner(player))
			{
				boolean editorOrDestructible = editor || !indestructible;
				ConfigGroup group0 = ConfigGroup.newGroup("tile");
				group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.progress_screen.name"));
				ConfigGroup config = group0.getGroup("ftbquests.screen");

				if (editor)
				{
					config.add("team", createTeamConfig(), ConfigNull.INSTANCE).setDisplayName(new TextComponentTranslation("ftbquests.team"));
				}

				config.add("chapter", new ConfigQuestObject(ServerQuestFile.INSTANCE, chapter, QuestObjectType.CHAPTER)
				{
					@Override
					public void setObject(int v)
					{
						chapter = v;
						cChapter = file.getChapter(chapter);
					}
				}, ConfigNull.INSTANCE).setCanEdit(editorOrDestructible).setDisplayName(new TextComponentTranslation("ftbquests.chapter"));

				config.add("skin", new ConfigBlockState(skin)
				{
					@Override
					public void setBlockState(IBlockState v)
					{
						skin = v;
					}
				}, new ConfigBlockState(BlockUtils.AIR_STATE)).setCanEdit(editorOrDestructible);

				config.addBool("fullscreen", () -> fullscreen, v -> fullscreen = v, false).setCanEdit(editorOrDestructible);
				config.addBool("hide_icons", () -> hideIcons, v -> hideIcons = v, false).setCanEdit(editorOrDestructible);

				if (editor)
				{
					config.addBool("indestructible", () -> indestructible, v -> indestructible = v, false);
				}

				FTBLibAPI.editServerConfig(player, group0, this);
			}
		}
		else if (!indestructible)
		{
			cChapter = getChapter();
			cTeam = getTeam();

			if (cChapter != null && cTeam != null)
			{
				List<QuestChapter> chapters = cChapter.file.getVisibleChapters(cTeam, true);
				chapter = chapters.get((chapters.indexOf(cChapter) + 1) % chapters.size()).id;
				updateContainingBlockInfo();
				markDirty();
				BlockUtils.notifyBlockUpdate(world, pos, getBlockState());
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