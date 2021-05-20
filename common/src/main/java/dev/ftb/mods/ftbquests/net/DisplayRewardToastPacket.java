package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class DisplayRewardToastPacket extends BaseS2CPacket {
	private final long id;
	private final Component text;
	private final Icon icon;

	DisplayRewardToastPacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		text = buffer.readComponent();
		icon = NetUtils.readIcon(buffer);
	}

	public DisplayRewardToastPacket(long _id, Component t, Icon i) {
		id = _id;
		text = t;
		icon = i;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.DISPLAY_REWARD_TOAST;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeComponent(text);
		NetUtils.writeIcon(buffer, icon);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.displayRewardToast(id, text, icon);
	}
}