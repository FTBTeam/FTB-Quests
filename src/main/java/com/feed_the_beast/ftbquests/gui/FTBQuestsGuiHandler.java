package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.misc.BlockGuiHandler;
import com.feed_the_beast.ftblib.lib.gui.misc.BlockGuiSupplier;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.gui.chest.ContainerQuestChest;
import com.feed_the_beast.ftbquests.gui.chest.GuiQuestChest;
import com.feed_the_beast.ftbquests.tile.TileQuestChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

/**
 * @author LatvianModder
 */
public class FTBQuestsGuiHandler extends BlockGuiHandler
{
	public static final BlockGuiSupplier CHEST = new BlockGuiSupplier(FTBQuests.MOD, 1)
	{
		@Override
		public Container getContainer(EntityPlayer player, TileEntity tileEntity)
		{
			return new ContainerQuestChest(player, ((TileQuestChest) tileEntity));
		}

		@Override
		public Object getGui(Container container)
		{
			return getGui0(container);
		}

		private Object getGui0(Container container)
		{
			return new GuiQuestChest((ContainerQuestChest) container).getWrapper();
		}
	};

	public FTBQuestsGuiHandler()
	{
		add(CHEST);
	}
}