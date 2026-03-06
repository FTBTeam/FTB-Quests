package dev.ftb.mods.ftbquests.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableEntityFace;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableImageResource;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableItemStack;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectEntityFaceScreen;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.BaseScreen;
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.icon.EntityIconLoader;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;

import java.util.List;

public class EditableIconItemStack extends EditableItemStack {
	public EditableIconItemStack() {
		super(false, true);
	}

	@Override
	public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
		if (getCanEdit()) {
			if (BaseScreen.isShiftKeyDown()) {
				new SelectItemStackScreen(this, callback).openGui();
			} else if (BaseScreen.isCtrlKeyDown()) {
				openImageSelector(callback);
			} else if (Minecraft.getInstance().hasAltDown()) {
				openEntitySelector(callback);
			} else {
				clickedWidget.getGui().openContextMenu(makeMenu(callback));
			}
		}
	}

	private void openImageSelector(ConfigCallback callback) {
		EditableImageResource imageConfig = new EditableImageResource();
		FTBQuests.getComponent(getValue(), ModDataComponents.CUSTOM_ICON)
				.ifPresent(imageConfig::setValue);

		new SelectImageResourceScreen(imageConfig, accepted -> {
			if (accepted) {
				if (!imageConfig.getValue().equals(EditableImageResource.NONE)) {
					setValue(Util.make(new ItemStack(ModItems.CUSTOM_ICON.get()),
							s -> CustomIconItem.setIcon(s, imageConfig.getValue())));
				} else {
					setValue(ItemStack.EMPTY);
				}
			}
			callback.save(accepted);
		}).openGui();
	}

	private void openEntitySelector(ConfigCallback callback) {
		EditableEntityFace faceConfig = new EditableEntityFace();
		FTBQuests.getComponent(getValue(), ModDataComponents.ENTITY_FACE_ICON)
				.flatMap(BuiltInRegistries.ENTITY_TYPE::get)
				.ifPresent(value -> faceConfig.setValue(value.value()));

		new SelectEntityFaceScreen(faceConfig, accepted -> {
			if (accepted) {
				if (!faceConfig.getValue().equals(EditableEntityFace.NONE)) {
					setValue(Util.make(new ItemStack(ModItems.CUSTOM_ICON.get()),
							s -> CustomIconItem.setFaceIcon(s, faceConfig.getValue())));
				} else {
					setValue(ItemStack.EMPTY);
				}
			}
			callback.save(accepted);
		}).openGui();
	}

	private List<ContextMenuItem> makeMenu(ConfigCallback callback) {
		return List.of(
				new ContextMenuItem(Component.translatable("ftbquests.gui.icon_menu.item"),
						ItemIcon.ofItem(Items.DIAMOND),b -> new SelectItemStackScreen(this, callback).openGui()),
				new ContextMenuItem(Component.translatable("ftbquests.gui.icon_menu.image"),
						Icons.ART, b -> openImageSelector(callback)),
				new ContextMenuItem(Component.translatable("ftbquests.gui.icon_menu.entity"),
						EntityIconLoader.getIcon(EntityType.PIG), b -> openEntitySelector(callback))
		);
	}
}
