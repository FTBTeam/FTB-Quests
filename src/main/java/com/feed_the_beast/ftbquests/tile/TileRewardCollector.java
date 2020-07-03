package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class TileRewardCollector extends TileBase implements ITickable, IConfigCallback
{
	public UUID owner = null;
	public int reward = 0;
	public ItemStackHandler inventory = new ItemStackHandler(9);

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		nbt.setInteger("reward", reward);

		if (!type.save)
		{
			return;
		}

		nbt.setString("owner", StringUtils.fromUUID(owner));
		nbt.setTag("inventory", inventory.serializeNBT());
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		reward = nbt.getInteger("reward");

		if (!type.save)
		{
			return;
		}

		owner = nbt.hasKey("owner") ? StringUtils.fromString(nbt.getString("owner")) : null;
		inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) inventory : super.getCapability(capability, facing);
	}

	@Override
	public void markDirty()
	{
		sendDirtyUpdate();
	}

	private static long longHashCode(Object... objects)
	{
		long result = 1L;

		for (Object element : objects)
		{
			result = 31L * result + (element == null ? 0L : element instanceof Number ? ((Number) element).longValue() : element.hashCode());
		}

		return result;
	}

	@Override
	public void update()
	{
		if (owner == null || reward == 0 || world == null || world.isRemote)
		{
			return;
		}

		Reward r = ServerQuestFile.INSTANCE.getReward(reward);

		if (r == null || !r.quest.canRepeat)
		{
			return;
		}

		ServerQuestData data = ServerQuestFile.INSTANCE.getData(owner);

		if (data != null)
		{
			EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);

			if (!data.isRewardClaimed(owner, r) && r.quest.isComplete(data))
			{
				List<ItemStack> stacks = new ArrayList<>();

				if (r.automatedClaimPre(this, stacks, world.rand, owner, player))
				{
					ItemStackHandler handler1 = new ItemStackHandler(inventory.getSlots());

					for (int i = 0; i < inventory.getSlots(); i++)
					{
						handler1.setStackInSlot(i, inventory.getStackInSlot(i));
					}

					for (ItemStack stack : stacks)
					{
						if (!ItemHandlerHelper.insertItem(handler1, stack, false).isEmpty())
						{
							return;
						}
					}

					for (ItemStack stack : stacks)
					{
						ItemHandlerHelper.insertItem(inventory, stack, false);
					}

					r.automatedClaimPost(this, owner, player);
					data.setRewardClaimed(owner, r);
				}
			}
		}
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
	}

	public void onRightClick(EntityPlayerMP player)
	{
		if (!owner.equals(player.getUniqueID()))
		{
			return;
		}

		ConfigGroup group0 = ConfigGroup.newGroup("tile");
		group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.reward_collector.name"));
		ConfigGroup config = group0.getGroup("ftbquests.reward_collector");

		config.add("reward", new ConfigQuestObject(ServerQuestFile.INSTANCE, reward, o -> o instanceof Reward && ((Reward) o).quest.canRepeat && ((Reward) o).automatedClaimPre(this, new ArrayList<>(), world.rand, owner, player))
		{
			@Override
			public void setObject(int v)
			{
				reward = v;
			}
		}, ConfigNull.INSTANCE).setDisplayName(new TextComponentTranslation("ftbquests.reward"));

		FTBLibAPI.editServerConfig(player, group0, this);
	}
}