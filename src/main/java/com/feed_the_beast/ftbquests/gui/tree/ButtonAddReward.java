package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectItemStack;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
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
					NBTTagCompound nbt = new NBTTagCompound();
					ItemReward itemReward = new ItemReward(quest, 0, nbt);
					itemReward.stack = stack;
					itemReward.writeData(nbt);
					nbt.setString("type", QuestRewardType.getTypeForNBT(ItemReward.class));
					nbt.setString("id", StringUtils.toSnakeCase(stack.getDisplayName()));
					//FIXME: new MessageCreateObject(QuestObjectType.REWARD, quest.getID(), nbt).sendToServer();
				}
			}).openGui();
			return;
		}

		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (QuestRewardType type : QuestRewardType.getRegistry())
		{
			QuestReward reward = type.provider.create(quest, 0, new NBTTagCompound());

			if (reward != null)
			{
				contextMenu.add(new ContextMenuItem(type.getDisplayName().getFormattedText(), reward.getIcon(), () -> {
					GuiHelper.playClickSound();

					ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
					ConfigGroup g = group.getGroup("reward").getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());
					reward.getConfig(g);
					reward.getExtraConfig(g);

					new GuiEditConfig(group, (g1, sender) -> {
						NBTTagCompound nbt = new NBTTagCompound();
						reward.writeData(nbt);
						nbt.setString("type", type.getTypeForNBT());
						nbt.setString("id", StringUtils.toSnakeCase(reward.getDisplayName().getUnformattedText()));
						//FIXME: new MessageCreateObject(QuestObjectType.REWARD, quest.getID(), nbt).sendToServer();
						//FIXME: GuiQuest.this.openGui();
						//questTreeGui.questFile.refreshGui();
					}).openGui();
				}));
			}
		}

		getGui().openContextMenu(contextMenu);
	}
}