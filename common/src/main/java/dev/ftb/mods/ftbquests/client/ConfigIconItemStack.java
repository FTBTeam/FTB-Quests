package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.EntityFaceConfig;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftblibrary.config.ui.resource.SelectEntityFaceScreen;
import dev.ftb.mods.ftblibrary.config.ui.resource.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.config.ui.resource.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.icon.EntityIconLoader;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class ConfigIconItemStack extends ItemStackConfig {
	public ConfigIconItemStack() {
		super(false, true);
	}

	@Override
	public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
		if (getCanEdit()) {
			if (BaseScreen.isShiftKeyDown()) {
				new SelectItemStackScreen(this, callback).openGui();
			} else if (BaseScreen.isCtrlKeyDown()) {
				openImageSelector(callback);
			} else if (ScreenWrapper.hasAltDown()) {
				openEntitySelector(callback);
			} else {
				clickedWidget.getGui().openContextMenu(makeMenu(callback));
			}
		}
	}

	private void openImageSelector(ConfigCallback callback) {
		ImageResourceConfig imageConfig = new ImageResourceConfig();
		FTBQuests.getComponent(getValue(), ModDataComponents.CUSTOM_ICON)
				.ifPresent(imageConfig::setCurrentValue);

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
	}

	private void openEntitySelector(ConfigCallback callback) {
		EntityFaceConfig faceConfig = new EntityFaceConfig();
		FTBQuests.getComponent(getValue(), ModDataComponents.ENTITY_FACE_ICON)
				.ifPresent(rl -> faceConfig.setCurrentValue(BuiltInRegistries.ENTITY_TYPE.get(rl)));

		new SelectEntityFaceScreen(faceConfig, accepted -> {
			if (accepted) {
				if (!faceConfig.getValue().equals(EntityFaceConfig.NONE)) {
					setCurrentValue(Util.make(new ItemStack(ModItems.CUSTOM_ICON.get()),
							s -> CustomIconItem.setFaceIcon(s, faceConfig.getValue())));
				} else {
					setCurrentValue(ItemStack.EMPTY);
				}
			}
			callback.save(accepted);
		}).openGui();
	}

	private List<ContextMenuItem> makeMenu(ConfigCallback callback) {
		return List.of(
				new ContextMenuItem(Component.translatable("ftbquests.gui.icon_menu.item"),
						ItemIcon.getItemIcon(Items.DIAMOND),b -> new SelectItemStackScreen(this, callback).openGui()),
				new ContextMenuItem(Component.translatable("ftbquests.gui.icon_menu.image"),
						Icons.ART, b -> openImageSelector(callback)),
				new ContextMenuItem(Component.translatable("ftbquests.gui.icon_menu.entity"),
						EntityIconLoader.getIcon(EntityType.PIG), b -> openEntitySelector(callback))
		);
	}
}
