package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class QuestShape extends Icon {
	private static final Map<String, QuestShape> MAP = new LinkedHashMap<>();
	private static QuestShape defaultShape;

	public static NameMap<String> idMap;
	public static NameMap<String> idMapWithDefault;

	private final String id;
	private final ImageIcon background, outline, shape;
	private PixelBuffer shapePixels;

	public QuestShape(String id) {
		this.id = id;
		background = new ImageIcon(FTBQuestsAPI.rl("textures/shapes/" + this.id + "/background.png"));
		outline = new ImageIcon(FTBQuestsAPI.rl("textures/shapes/" + this.id + "/outline.png"));
		shape = new ImageIcon(FTBQuestsAPI.rl("textures/shapes/" + this.id + "/shape.png"));
	}

	public static void reload(List<String> list) {
		MAP.clear();

		list.forEach(s -> MAP.put(s, new QuestShape(s)));

		defaultShape = MAP.values().iterator().next();
		idMap = NameMap.of(list.get(0), list).baseNameKey("ftbquests.quest.shape").create();
		list.add(0, "default");
		idMapWithDefault = NameMap.of(list.get(0), list).baseNameKey("ftbquests.quest.shape").create();
	}

	public static QuestShape get(String id) {
		return MAP.getOrDefault(id, defaultShape);
	}

	public String toString() {
		return "quest_shape:" + id;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
		background.draw(graphics, x, y, w, h);
		outline.draw(graphics, x, y, w, h);
	}

	public ImageIcon getBackground() {
		return background;
	}

	public ImageIcon getOutline() {
		return outline;
	}

	public ImageIcon getShape() {
		return shape;
	}

	public int hashCode() {
		return id.hashCode();
	}

	public boolean equals(Object o) {
		return o == this;
	}

	public PixelBuffer getShapePixels() {
		if (shapePixels == null) {
			try {
				ResourceLocation shapeLoc = FTBQuestsAPI.rl("textures/shapes/" + id + "/shape.png");
				Resource resource = Minecraft.getInstance().getResourceManager().getResource(shapeLoc).get();
				try (InputStream stream = resource.open()) {
					shapePixels = PixelBuffer.from(stream);
				}
			} catch (Exception ex) {
				shapePixels = new PixelBuffer(1, 1);
				shapePixels.setRGB(0, 0, 0xFFFFFFFF);
			}
		}

		return shapePixels;
	}

	public static Map<String,QuestShape> map() {
		return Collections.unmodifiableMap(MAP);
	}
}
