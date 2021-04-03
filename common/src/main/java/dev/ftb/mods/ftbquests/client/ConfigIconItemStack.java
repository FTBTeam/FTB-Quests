package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftbguilibrary.config.ConfigCallback;
import dev.ftb.mods.ftbguilibrary.config.ItemStackConfig;
import dev.ftb.mods.ftbguilibrary.config.gui.SelectItemStackScreen;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import dev.ftb.mods.ftbquests.gui.ImageConfig;
import dev.ftb.mods.ftbquests.gui.SelectImageScreen;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ConfigIconItemStack extends ItemStackConfig {
	public ConfigIconItemStack() {
		super(false, true);
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		if (getCanEdit()) {
			if (button.isRight()) {
				ImageConfig imageConfig = new ImageConfig();
				new SelectImageScreen(imageConfig, b -> {
					if (b) {
						if (!imageConfig.value.isEmpty()) {
							ItemStack stack = new ItemStack(FTBQuestsItems.CUSTOM_ICON.get());
							stack.addTagElement("Icon", StringTag.valueOf(imageConfig.value));
							setCurrentValue(stack);
						} else {
							setCurrentValue(ItemStack.EMPTY);
						}
					}

					callback.save(b);
				}).openGui();
			} else {
				new SelectItemStackScreen(this, callback).openGui();
			}
		}
	}
}
