package dev.ftb.mods.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class WikiButton extends TabButton {
	public WikiButton(Panel panel) {
		super(panel, new TranslatableComponent("ftbquests.gui.wiki"), ThemeProperties.WIKI_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		handleClick(ThemeProperties.WIKI_URL.get());
	}
}