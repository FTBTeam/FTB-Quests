package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.ContainerBase;
import com.feed_the_beast.ftbquests.block.TileQuest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

/**
 * @author LatvianModder
 */
public class ContainerQuestBlock extends ContainerBase
{
	public final TileQuest tile;

	public ContainerQuestBlock(EntityPlayer player, TileQuest t)
	{
		super(player);
		tile = t;
		addSlotToContainer(new SlotItemHandler(t, 0, 80, 34));
		addPlayerSlots(8, 84);
	}

	@Override
	public int getNonPlayerSlots()
	{
		return 1;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		BlockPos pos = tile.getPos();
		return player.world.getTileEntity(pos) == tile && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64D;
	}
}
