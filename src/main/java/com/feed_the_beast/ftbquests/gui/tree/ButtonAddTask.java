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
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonAddTask extends SimpleTextButton
{
	public final Quest quest;

	public ButtonAddTask(Panel panel, Quest q)
	{
		super(panel, I18n.format("gui.add"), QuestsTheme.ADD);
		quest = q;
	}

	@Override
	public String getTitle()
	{
		return isCtrlKeyDown() ? (super.getTitle() + ": " + I18n.format("ftbquests.task.ftbquests.item")) : super.getTitle();
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
					ItemTask itemTask = new ItemTask(quest);
					itemTask.items.add(ItemHandlerHelper.copyStackWithSize(stack, 1));
					itemTask.count = stack.getCount();
					NBTTagCompound nbt = new NBTTagCompound();
					itemTask.writeData(nbt);
					nbt.setString("type", QuestTaskType.getType(ItemTask.class).getTypeForNBT());
					nbt.setString("id", StringUtils.toSnakeCase(stack.getDisplayName()));
					new MessageCreateObject(QuestObjectType.TASK, quest.uid, nbt).sendToServer();
				}
			}).openGui();
			return;
		}

		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (QuestTaskType type : QuestTaskType.getRegistry())
		{
			QuestTask task = type.provider.create(quest);

			if (task != null)
			{
				contextMenu.add(new ContextMenuItem(type.getDisplayName().getFormattedText(), task.getIcon(), () -> {
					GuiHelper.playClickSound();

					ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
					ConfigGroup g = group.getGroup("task." + type.getRegistryName().getNamespace() + '.' + type.getRegistryName().getPath());
					task.getConfig(g);
					task.getExtraConfig(g);

					new GuiEditConfig(group, (g1, sender) -> {
						NBTTagCompound nbt = new NBTTagCompound();
						task.writeData(nbt);
						nbt.setString("type", type.getTypeForNBT());
						nbt.setString("id", StringUtils.toSnakeCase(task.getDisplayName().getUnformattedText()));
						new MessageCreateObject(QuestObjectType.TASK, quest.uid, nbt).sendToServer();
						//FIXME: GuiQuest.this.openGui();
						//questTreeGui.questFile.refreshGui();
					}).openGui();
				}));
			}
		}

		getGui().openContextMenu(contextMenu);
	}
}