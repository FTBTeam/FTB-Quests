package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigCallback;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class SelectImageScreen extends GuiButtonListBase {
	private final ImageConfig imageConfig;
	private final ConfigCallback callback;
	private final List<ResourceLocation> images;

	public SelectImageScreen(ImageConfig i, ConfigCallback c) {
		imageConfig = i;
		callback = c;
		setTitle(new TextComponent("Select Image"));
		setHasSearchBox(true);
		focus();
		setBorder(1, 1, 1);

		images = new ArrayList<>();

		try {
			images.addAll(Minecraft.getInstance().getResourceManager().listResources("textures", t -> t.endsWith(".png") && ResourceLocation.isValidResourceLocation(t)));
		} catch (Exception ex) {
			FTBQuests.LOGGER.error("A mod has broken resource preventing this list from loading: " + ex);
		}

		images.sort(null);
	}

	@Override
	public void addButtons(Panel panel) {
		panel.add(new SimpleTextButton(panel, new TextComponent("None"), Icon.EMPTY) {
			@Override
			public void onClicked(MouseButton mouseButton) {
				playClickSound();
				imageConfig.setCurrentValue("");
				callback.save(true);
			}
		});

		for (ResourceLocation res : images) {
			if (res.getPath().startsWith("textures/font/")) {
				continue;
			}

			panel.add(new SimpleTextButton(panel, new TextComponent("").append(new TextComponent(res.getNamespace()).withStyle(ChatFormatting.GOLD)).append(":").append(new TextComponent(res.getPath().substring(9, res.getPath().length() - 4)).withStyle(ChatFormatting.YELLOW)), Icon.getIcon(res.toString())) {
				@Override
				public void onClicked(MouseButton mouseButton) {
					playClickSound();
					imageConfig.setCurrentValue(res.toString());
					callback.save(true);
				}
			});
		}
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			callback.save(false);
			return false;
		}

		return false;
	}
}
