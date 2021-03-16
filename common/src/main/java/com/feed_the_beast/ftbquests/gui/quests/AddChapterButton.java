package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.net.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class AddChapterButton extends TabButton {
	public AddChapterButton(Panel panel) {
		super(panel, new TranslatableComponent("gui.add"), Icon.getIcon("ftbquests:textures/gui/burger.png"));
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();

		ConfigString c = new ConfigString(Pattern.compile("^.+$"));
		GuiEditConfigFromString.open(c, "", "", accepted -> {
			questScreen.openGui();

			if (accepted && !c.value.isEmpty()) {
				Chapter chapter = new Chapter(questScreen.file, questScreen.file.defaultChapterGroup);
				chapter.title = c.value;
				CompoundTag extra = new CompoundTag();
				extra.putLong("group", 0L);
				new MessageCreateObject(chapter, extra).sendToServer();
			}

			run();
		});
	}
}