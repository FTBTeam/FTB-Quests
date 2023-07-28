package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.Component;

public class OtherButtonsPanelTop extends OtherButtonsPanel {
	public OtherButtonsPanelTop(Panel panel) {
		super(panel);
	}

	@Override
	public void addWidgets() {
		add(new CollectRewardsButton(this));

		if (Platform.isModLoaded("ftbguides")) {
			add(new OpenGuidesButton(this));
		}

		if (!questScreen.file.getEmergencyItems().isEmpty() && (questScreen.file.selfTeamData != null || questScreen.file.canEdit())) {
			add(new EmergencyItemsButton(this));
		}

		if (!ThemeProperties.WIKI_URL.get().equals("-")) {
			add(new WikiButton(this));
		}

		if (Platform.isModLoaded("ftbmoney")) {
			add(new OpenShopButton(this));
		}
	}

	@Override
	public void alignWidgets() {
		setPosAndSize(questScreen.width - width, 1, width, align(WidgetLayout.VERTICAL));
	}

    /**
     * @author LatvianModder
     */
    public static class WikiButton extends TabButton {
        public WikiButton(Panel panel) {
            super(panel, Component.translatable("ftbquests.gui.wiki"), ThemeProperties.WIKI_ICON.get());
        }

        @Override
        public void onClicked(MouseButton button) {
            playClickSound();
            handleClick(ThemeProperties.WIKI_URL.get());
        }
    }
}