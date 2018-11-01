package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectItemStack;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.net.edit.MessageAddReward;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonAddReward extends SimpleTextButton
{
	private final Quest quest;

	public ButtonAddReward(Panel panel, Quest q)
	{
		super(panel, I18n.format("gui.add"), QuestsTheme.ADD);
		quest = q;
	}

	@Override
	public String getTitle()
	{
		return isCtrlKeyDown() ? (super.getTitle() + ": " + I18n.format("ftbquests.reward.ftbquests.item")) : super.getTitle();
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();

		if (isCtrlKeyDown())
		{
			new GuiSelectItemStack(this, stack -> {
				if (!stack.isEmpty())
				{
					ItemReward reward = new ItemReward(quest);
					reward.stack = stack;
					reward.team = ClientQuestFile.INSTANCE.defaultRewardTeam;
					NBTTagCompound nbt = new NBTTagCompound();
					reward.writeData(nbt);
					reward.writeCommonData(nbt);
					new MessageAddReward(quest.getID(), nbt).sendToServer();
				}
			}).openGui();
			return;
		}

		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (QuestRewardType type : QuestRewardType.getRegistry())
		{
			QuestReward reward = type.provider.create(quest);

			if (reward != null)
			{
				reward.team = ClientQuestFile.INSTANCE.defaultRewardTeam;

				contextMenu.add(new ContextMenuItem(type.getDisplayName().getFormattedText(), reward.getIcon(), () -> {
					GuiHelper.playClickSound();

					ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
					ConfigGroup g = group.getGroup("reward").getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());
					reward.getConfig(g);
					reward.getExtraConfig(g);

					new GuiEditConfig(group, (g1, sender) -> {
						NBTTagCompound nbt = new NBTTagCompound();
						reward.writeData(nbt);
						reward.writeCommonData(nbt);
						nbt.setString("type", type.getTypeForNBT());
						ButtonAddReward.this.getGui().openGui();
						new MessageAddReward(quest.getID(), nbt).sendToServer();
					}).openGui();
				}));
			}
		}

		getGui().openContextMenu(contextMenu);
	}
}