package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ImageIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.PixelBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
public final class QuestShape extends Icon
{
	public static final Map<String, QuestShape> MAP = new LinkedHashMap<>();
	private static QuestShape defaultShape;
	public static NameMap<String> idMap, idMapWithDefault;

	public static void reload(List<String> list)
	{
		MAP.clear();

		for (String s : list)
		{
			MAP.put(s, new QuestShape(s));
		}

		defaultShape = MAP.values().iterator().next();
		idMap = NameMap.of(list.get(0), list.toArray(new String[0])).baseNameKey("ftbquests.quest.shape").create();
		list.add(0, "default");
		idMapWithDefault = NameMap.of(list.get(0), list.toArray(new String[0])).baseNameKey("ftbquests.quest.shape").create();
	}

	public static QuestShape get(String id)
	{
		return MAP.getOrDefault(id, defaultShape);
	}

	public final String id;
	public final ImageIcon background, outline, shape;
	private PixelBuffer shapePixels;

	public QuestShape(String i)
	{
		id = i;
		background = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/shapes/" + id + "/background.png"));
		outline = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/shapes/" + id + "/outline.png"));
		shape = new ImageIcon(new ResourceLocation(FTBQuests.MOD_ID, "textures/shapes/" + id + "/shape.png"));
	}

	public String toString()
	{
		return "quest_shape:" + id;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(PoseStack matrixStack, int x, int y, int w, int h)
	{
		background.draw(matrixStack, x, y, w, h);
		outline.draw(matrixStack, x, y, w, h);
	}

	public int hashCode()
	{
		return id.hashCode();
	}

	public boolean equals(Object o)
	{
		return o == this;
	}

	public PixelBuffer getShapePixels()
	{
		if (shapePixels == null)
		{
			try
			{
				Resource resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(FTBQuests.MOD_ID, "textures/shapes/" + id + "/shape.png"));

				try (InputStream stream = resource.getInputStream())
				{
					shapePixels = PixelBuffer.from(stream);
				}
			}
			catch (Exception ex)
			{
				shapePixels = new PixelBuffer(1, 1);
				shapePixels.setRGB(0, 0, 0xFFFFFFFF);
			}

		}

		return shapePixels;
	}
}