package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.client.PixelBuffer;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ImageIcon;
import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
public final class QuestShape extends Icon implements IWithID
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
		idMap = NameMap.createWithBaseTranslationKey(list.get(0), "ftbquests.quest.shape", list.toArray(new String[0]));
		list.add(0, "default");
		idMapWithDefault = NameMap.createWithBaseTranslationKey(list.get(0), "ftbquests.quest.shape", list.toArray(new String[0]));
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

	@Override
	public String getId()
	{
		return id;
	}

	public String toString()
	{
		return "quest_shape:" + id;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(int x, int y, int w, int h)
	{
		background.draw(x, y, w, h);
		outline.draw(x, y, w, h);
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
				IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(FTBQuests.MOD_ID, "textures/shapes/" + id + "/shape.png"));

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