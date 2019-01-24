package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.icon.AtlasSpriteIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.InputStream;

/**
 * @author LatvianModder
 */
public class IconWrapper
{
	@Nullable
	public static Icon from(@Nullable com.feed_the_beast.ftblib.lib.icon.Icon icon)
	{
		if (icon == null)
		{
			return null;
		}
		else if (icon instanceof com.feed_the_beast.ftblib.lib.icon.ImageIcon)
		{
			try (InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(((com.feed_the_beast.ftblib.lib.icon.ImageIcon) icon).texture).getInputStream())
			{
				return new ImageIcon(ImageIO.read(stream));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else if (icon instanceof AtlasSpriteIcon)
		{
			ResourceLocation rl = new ResourceLocation(((AtlasSpriteIcon) icon).name);

			try (InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(rl.getNamespace(), "textures/" + rl.getPath() + ".png")).getInputStream())
			{
				return new ImageIcon(ImageIO.read(stream));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		return null;
	}
}