package dev.ftb.mods.ftbquests.quest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ImageConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public final class ChapterImage implements Movable {
	private static final Pattern COLOR_PATTERN = Pattern.compile("^#[a-fA-F0-9]{6}$");

	public Chapter chapter;
	public double x, y;
	public double width, height;
	public double rotation;
	private Icon image;
	private Color4I color;
	private int alpha;
	public List<String> hover;
	public String click;
	public boolean dev;
	public boolean corner;
	public Quest dependency;
	private double aspectRatio;
	private boolean needAspectRecalc;
	private int order;

	public ChapterImage(Chapter c) {
		chapter = c;
		x = y = 0D;
		width = 1D;
		height = 1D;
		rotation = 0D;
		image = Icon.EMPTY; //getIcon("minecraft:textures/gui/presets/isles.png");
		color = Color4I.WHITE;
		alpha = 255;
		needAspectRecalc = true;
		hover = new ArrayList<>();
		click = "";
		dev = false;
		corner = false;
		dependency = null;
		order = 0;
	}

	public Icon getImage() {
		return image;
	}

	public ChapterImage setImage(Icon image) {
		this.image = image;
		needAspectRecalc = true;
		return this;
	}

	public Color4I getColor() {
		return color;
	}

	public int getAlpha() {
		return alpha;
	}

	public int getOrder() {
		return order;
	}

	public void writeData(CompoundTag nbt) {
		nbt.putDouble("x", x);
		nbt.putDouble("y", y);
		nbt.putDouble("width", width);
		nbt.putDouble("height", height);
		nbt.putDouble("rotation", rotation);
		nbt.putString("image", image.toString());
		if (!color.equals(Color4I.WHITE)) {
			nbt.putInt("color", color.rgb());
		}
		if (alpha != 255) {
			nbt.putInt("alpha", alpha);
		}
		if (order != 0) {
			nbt.putInt("order", order);
		}

		ListTag hoverTag = new ListTag();

		for (String s : hover) {
			hoverTag.add(StringTag.valueOf(s));
		}

		nbt.put("hover", hoverTag);
		nbt.putString("click", click);
		nbt.putBoolean("dev", dev);
		nbt.putBoolean("corner", corner);

		if (dependency != null) {
			nbt.putString("dependency", dependency.getCodeString());
		}
	}

	public void readData(CompoundTag nbt) {
		x = nbt.getDouble("x");
		y = nbt.getDouble("y");
		width = nbt.getDouble("width");
		height = nbt.getDouble("height");
		rotation = nbt.getDouble("rotation");
		setImage(Icon.getIcon(nbt.getString("image")));
		color = nbt.contains("color") ? Color4I.rgb(nbt.getInt("color")) : Color4I.WHITE;
		alpha = nbt.contains("alpha") ? nbt.getInt("alpha") : 255;
		order = nbt.getInt("order");

		hover.clear();
		ListTag hoverTag = nbt.getList("hover", Tag.TAG_STRING);

		for (int i = 0; i < hoverTag.size(); i++) {
			hover.add(hoverTag.getString(i));
		}

		click = nbt.getString("click");
		dev = nbt.getBoolean("dev");
		corner = nbt.getBoolean("corner");

		dependency = nbt.contains("dependency") ? chapter.file.getQuest(chapter.file.getID(nbt.get("dependency"))) : null;
	}

	public void writeNetData(FriendlyByteBuf buffer) {
		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeDouble(width);
		buffer.writeDouble(height);
		buffer.writeDouble(rotation);
		NetUtils.writeIcon(buffer, image);
		buffer.writeInt(color.rgb());
		buffer.writeInt(alpha);
		buffer.writeInt(order);
		NetUtils.writeStrings(buffer, hover);
		buffer.writeUtf(click, Short.MAX_VALUE);
		buffer.writeBoolean(dev);
		buffer.writeBoolean(corner);
		buffer.writeLong(dependency == null ? 0L : dependency.id);
	}

	public void readNetData(FriendlyByteBuf buffer) {
		x = buffer.readDouble();
		y = buffer.readDouble();
		width = buffer.readDouble();
		height = buffer.readDouble();
		rotation = buffer.readDouble();
		setImage(NetUtils.readIcon(buffer));
		color = Color4I.rgb(buffer.readInt());
		alpha = buffer.readInt();
		order = buffer.readInt();
		NetUtils.readStrings(buffer, hover);
		click = buffer.readUtf(Short.MAX_VALUE);
		dev = buffer.readBoolean();
		corner = buffer.readBoolean();
		dependency = chapter.file.getQuest(buffer.readLong());
	}

	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		config.addDouble("x", x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("y", y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("width", width, v -> width = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("height", height, v -> height = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("rotation", rotation, v -> rotation = v, 0, -180, 180);
		config.add("image", new ImageConfig(), image instanceof Color4I ? "" : image.toString(), v -> setImage(Icon.getIcon(v)), "minecraft:textures/gui/presets/isles.png");
		config.addString("color", color.toString(), v -> color = Color4I.fromString(v), "#FFFFFF", COLOR_PATTERN);
		config.addInt("order", order, v -> order = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		config.addInt("alpha", alpha, v -> alpha = v, 255, 0, 255);
		config.addList("hover", hover, new StringConfig(), "");
		config.addString("click", click, v -> click = v, "");
		config.addBool("dev", dev, v -> dev = v, false);
		config.addBool("corner", corner, v -> corner = v, false);

		Predicate<QuestObjectBase> depTypes = object -> object == null || object instanceof Quest;
		config.add("dependency", new ConfigQuestObject<>(depTypes), dependency, v -> dependency = v, null).setNameKey("ftbquests.dependency");
	}

	@Override
	public long getMovableID() {
		return 0L;
	}

	@Override
	public Chapter getChapter() {
		return chapter;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public String getShape() {
		return "square";
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void move(Chapter to, double _x, double _y) {
		x = _x;
		y = _y;

		if (to != chapter) {
			chapter.images.remove(this);
			new EditObjectMessage(chapter).sendToServer();

			chapter = to;
			chapter.images.add(this);
		}

		new EditObjectMessage(chapter).sendToServer();
	}

	@Override
	public void onMoved(double x, double y, long chapterId) {
		// do nothing; image moving is handled via EditObjectMessage
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void drawMoved(PoseStack matrixStack) {
		matrixStack.pushPose();

		if (corner) {
			matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) rotation));
			image.withColor(Color4I.WHITE.withAlpha(50)).draw(matrixStack, 0, 0, 1, 1);
		} else {
			matrixStack.translate(0.5D, 0.5D, 0);
			matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) rotation));
			matrixStack.scale(0.5F, 0.5F, 1);
			image.withColor(Color4I.WHITE.withAlpha(50)).draw(matrixStack, -1, -1, 2, 2);
		}

		matrixStack.popPose();

		QuestShape.get(getShape()).outline.withColor(Color4I.WHITE.withAlpha(30)).draw(matrixStack, 0, 0, 1, 1);
	}

	public boolean isAspectRatioOff() {
		return image.hasPixelBuffer() && !Mth.equal(getAspectRatio(), width / height);
	}

	public void fixupAspectRatio(boolean adjustWidth) {
		if (isAspectRatioOff()) {
			if (adjustWidth) {
				width = height * getAspectRatio();
			} else {
				height = width / getAspectRatio();
			}
			new EditObjectMessage(chapter).sendToServer();
		}
	}

	private double getAspectRatio() {
		if (needAspectRecalc) {
			PixelBuffer buffer = image.createPixelBuffer();
			if (buffer != null) {
				aspectRatio = (double) buffer.getWidth() / (double) buffer.getHeight();
			} else {
				aspectRatio = 1d;
			}
			needAspectRecalc = false;
		}
		return aspectRatio;
	}
}
