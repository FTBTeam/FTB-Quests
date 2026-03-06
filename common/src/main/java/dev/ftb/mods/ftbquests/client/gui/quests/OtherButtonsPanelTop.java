package dev.ftb.mods.ftbquests.client.gui.quests;

import net.minecraft.network.chat.Component;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.layout.WidgetLayout;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.TogglePinnedMessage;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;

public class OtherButtonsPanelTop extends OtherButtonsPanel {
	public OtherButtonsPanelTop(Panel panel) {
		super(panel);
	}

	@Override
	public void addWidgets() {
		add(new CollectRewardsButton(this));

		add(new AutopinButton(this));

		add(new KeyReferenceButton(this));

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

	public static class AutopinButton extends TabButton {
		public AutopinButton(Panel panel) {
			super(panel,
					Component.translatable(isAutoPin() ? "ftbquests.gui.autopin.on" : "ftbquests.gui.autopin.off"),
					isAutoPin() ? ThemeProperties.PIN_ICON_ON.get() : ThemeProperties.PIN_ICON_OFF.get()
			);
		}

		private static boolean isAutoPin() {
			return ClientQuestFile.isQuestPinned(TeamData.AUTO_PIN_ID);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			NetworkManager.sendToServer(new TogglePinnedMessage(TeamData.AUTO_PIN_ID));
		}
	}

	private static class KeyReferenceButton extends TabButton {
		public KeyReferenceButton(Panel panel) {
			super(panel, Component.translatable("ftblibrary.gui.key_reference"), Icons.KEYBOARD);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();

			if (ClientQuestFile.getInstance().canEdit()) {
				new QuestKeyReferenceScreen("ftbquests.gui.key_reference.player", "ftbquests.gui.key_reference.editor").openGui();
			} else {
				new QuestKeyReferenceScreen("ftbquests.gui.key_reference.player").openGui();
			}
		}
	}
}