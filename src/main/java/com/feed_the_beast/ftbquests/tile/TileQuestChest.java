package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.gui.FTBQuestsGuiHandler;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileQuestChest extends TileWithTeam implements IItemHandler, IConfigCallback
{
	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return true;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return (T) this;
		}

		return super.getCapability(capability, facing);
	}

	public void openGui(EntityPlayerMP player)
	{
		if (!player.isSneaking())
		{
			if (isOwner(player))
			{
				FTBQuestsGuiHandler.CHEST.open(player, pos);
			}

			return;
		}

		boolean editor = FTBQuests.canEdit(player);

		ConfigGroup group0 = ConfigGroup.newGroup("tile");
		group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.chest.name"));
		ConfigGroup group = group0.getGroup("ftbquests.chest");

		group.add("team", createTeamConfig(), ConfigNull.INSTANCE).setDisplayName(new TextComponentTranslation("ftbquests.team")).setCanEdit(editor);

		if (editor)
		{
			group.addBool("indestructible", () -> indestructible, v -> indestructible = v, false).setDisplayName(new TextComponentTranslation("tile.ftbquests.screen.indestructible"));
		}

		FTBLibAPI.editServerConfig(player, group0, this);
	}

	@Override
	public int getSlots()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		return insert(stack, simulate, null);
	}

	public ItemStack insert(ItemStack stack, boolean simulate, @Nullable EntityPlayer player)
	{
		QuestFile file = FTBQuests.PROXY.getQuestFile(world);

		cTeam = getTeam();

		if (cTeam != null)
		{
			for (QuestChapter chapter : file.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (QuestTask task : quest.tasks)
					{
						if (task.canInsertItem() && !task.isComplete(cTeam) && task.quest.canStartTasks(cTeam))
						{
							stack = cTeam.getQuestTaskData(task).insertItem(stack, false, simulate, player);

							if (stack.isEmpty())
							{
								return ItemStack.EMPTY;
							}
						}
					}
				}
			}
		}

		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public void markDirty()
	{
		sendDirtyUpdate();
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		markDirty();
	}
}