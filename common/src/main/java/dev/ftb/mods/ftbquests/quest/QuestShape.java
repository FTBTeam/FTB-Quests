package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import dev.ftb.mods.ftblibrary.util.Lazy;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.QuestShapeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.*;

public final class QuestShape extends Icon<QuestShape> {
	private static final Map<String, QuestShape> MAP = new LinkedHashMap<>();
	private static final String DEF_SHAPE = "circle";
	private static QuestShape defaultShape = new QuestShape(DEF_SHAPE);

	public static NameMap<String> idMap = NameMap.of(DEF_SHAPE, List.of(DEF_SHAPE)).create();
	public static NameMap<String> idMapWithDefault = NameMap.of(DEF_SHAPE, List.of(DEF_SHAPE, "default")).create();

	private final String id;
	private final ImageIcon background, outline, shape;
	private final boolean shouldDraw;
	private final Lazy<PixelBuffer> shapePixels = Lazy.of(this::makeShapePixels);

	public QuestShape(String id) {
		this.id = id;
		background = new ImageIcon(FTBQuestsAPI.id("textures/shapes/" + this.id + "/background.png"));
		outline = new ImageIcon(FTBQuestsAPI.id("textures/shapes/" + this.id + "/outline.png"));
		shape = new ImageIcon(FTBQuestsAPI.id("textures/shapes/" + this.id + "/shape.png"));
		shouldDraw = !id.equals("none");
	}

	public static void reload(List<String> list) {
		MAP.clear();

		list.forEach(s -> MAP.put(s, new QuestShape(s)));

		defaultShape = MAP.values().iterator().next();
		idMap = NameMap.of(list.getFirst(), list).baseNameKey("ftbquests.quest.shape").create();
		list.addFirst("default");
		idMapWithDefault = NameMap.of(list.getFirst(), list).baseNameKey("ftbquests.quest.shape").create();
	}

	public static QuestShape get(String id) {
		return MAP.getOrDefault(id, defaultShape);
	}

	public String toString() {
		return "quest_shape:" + id;
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

	@Override
	public IconRenderer<QuestShape> getRenderer() {
		return QuestShapeRenderer.INSTANCE;
	}

	public PixelBuffer getShapePixels() {
		return shapePixels.get();
	}

	public boolean shouldDraw() {
		return shouldDraw;
	}

	public static Map<String,QuestShape> map() {
		return Collections.unmodifiableMap(MAP);
	}

	private PixelBuffer makeShapePixels() {
		try {
			Identifier shapeLoc = FTBQuestsAPI.id("textures/shapes/" + id + "/shape.png");
			Optional<Resource> opt = Minecraft.getInstance().getResourceManager().getResource(shapeLoc);
			if (opt.isPresent()) {
				try (InputStream stream = opt.get().open()) {
					return PixelBuffer.from(stream);
				}
			} else {
				return emptyBuffer();
			}
		} catch (Exception ex) {
			return emptyBuffer();
		}
	}

	private static PixelBuffer emptyBuffer() {
		PixelBuffer res = new PixelBuffer(1, 1);
		res.setRGB(0, 0, 0xFFFFFFFF);
		return res;
	}
}
