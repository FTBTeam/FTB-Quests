package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftblibrary.config.ui.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;

public class ConfigIconItemStack extends ItemStackConfig {
	public ConfigIconItemStack() {
		super(false, true);
	}

	@Override
	public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
		if (getCanEdit()) {
			if (button.isRight()) {
				ImageResourceConfig imageConfig = new ImageResourceConfig();
				new SelectImageResourceScreen(imageConfig, accepted -> {
					if (accepted) {
						if (!imageConfig.getValue().equals(ImageResourceConfig.NONE)) {
							setCurrentValue(Util.make(new ItemStack(ModItems.CUSTOM_ICON.get()),
									s -> CustomIconItem.setIcon(s, imageConfig.getValue())));
						} else {
							setCurrentValue(ItemStack.EMPTY);
						}
					}
					callback.save(accepted);
				}).openGui();
			} else {
				new SelectItemStackScreen(this, callback).openGui();
			}
		}
	}
}
