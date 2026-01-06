package dev.ftb.mods.ftbquests.quest;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class ChapterImage implements Movable {
	// magic string which goes in the clipboard if an image has been copied
	public static final String FTBQ_IMAGE = "<ftbq-image>";

	public static WeakReference<ChapterImage> clipboard = new WeakReference<>(null);

	public static StreamCodec<FriendlyByteBuf, ChapterImage> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ChapterImage decode(FriendlyByteBuf buf) {
			long chapterId = buf.readLong();
            ChapterImage img = new ChapterImage(ServerQuestFile.INSTANCE.getChapter(chapterId));
            img.readNetData(buf);
            return img;
        }

        @Override
        public void encode(FriendlyByteBuf buf, ChapterImage chapterImage) {
			buf.writeLong(chapterImage.getChapter().id);
            chapterImage.writeNetData(buf);
        }
    };

	private Chapter chapter;
	private double x, y;
	private double width, height;
	private double rotation;
	private Icon image;
	private Color4I color;
	private int alpha;
	private final List<String> hover;
	private String click;
	private boolean editorsOnly;
	private boolean alignToCorner;
	private Quest dependency;
	private int order;

	public ChapterImage(Chapter c) {
		chapter = c;
		x = y = 0D;
		width = 1D;
		height = 1D;
		rotation = 0D;
		image = Color4I.empty();
		color = Color4I.WHITE;
		alpha = 255;
		hover = new ArrayList<>();
		click = "";
		editorsOnly = false;
		alignToCorner = false;
		dependency = null;
		order = 0;
	}

	public Icon getImage() {
		return image;
	}

	public ChapterImage setImage(Icon image) {
		this.image = image;
		return this;
	}

	public ChapterImage setPosition(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public static ChapterImage fromNet(Chapter parent, FriendlyByteBuf buf) {
		return Util.make(new ChapterImage(parent), img -> img.readNetData(buf));
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

	@Override
	public double getRotation() {
		return rotation;
	}

	@Override
	public boolean isAlignToCorner() {
		return alignToCorner;
	}

	public String getClick() {
		return click;
	}

	public void addHoverText(TooltipList list) {
		hover.forEach(list::translate);
	}

	public CompoundTag writeData(CompoundTag nbt) {
		nbt.putDouble("x", x);
		nbt.putDouble("y", y);
		nbt.putDouble("width", width);
		nbt.putDouble("height", height);
		nbt.putDouble("rotation", rotation);
		nbt.putString("image", image.toString());
		if (!color.equals(Color4I.WHITE)) nbt.putInt("color", color.rgb());
		if (alpha != 255) nbt.putInt("alpha", alpha);
		if (order != 0) nbt.putInt("order", order);
		if (!hover.isEmpty()) {
			nbt.put("hover", Util.make(new ListTag(), l -> hover.forEach(s -> l.add(StringTag.valueOf(s)))));
		}
		if (!click.isEmpty()) nbt.putString("click", click);
		if (editorsOnly) nbt.putBoolean("dev", true);
		if (alignToCorner) nbt.putBoolean("corner", true);
		if (dependency != null) nbt.putString("dependency", dependency.getCodeString());

		return nbt;
	}

	public void readData(CompoundTag nbt) {
		x = nbt.getDouble("x").orElseThrow();
		y = nbt.getDouble("y").orElseThrow();
		width = nbt.getDouble("width").orElseThrow();
		height = nbt.getDouble("height").orElseThrow();
		rotation = nbt.getDouble("rotation").orElseThrow();
		setImage(Icon.getIcon(nbt.getString("image").orElseThrow()));
		color = nbt.getInt("color").map(Color4I::rgb).orElse(Color4I.WHITE);
		alpha = nbt.getInt("alpha").orElse(255);
		order = nbt.getInt("order").orElse(0);

		hover.clear();
		nbt.getList("hover").ifPresent(hoverList -> {
			for (Tag t : hoverList) {
				t.asString().ifPresent(hover::add);
			}
		});

		click = nbt.getStringOr("click", "");
		editorsOnly = nbt.getBooleanOr("dev", false);
		alignToCorner = nbt.getBooleanOr("corner", false);
		dependency = nbt.getString("dependency")
				.map(dependency -> chapter.file.getQuest(chapter.file.getID(dependency)))
				.orElse(null);
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
		buffer.writeBoolean(editorsOnly);
		buffer.writeBoolean(alignToCorner);
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
		editorsOnly = buffer.readBoolean();
		alignToCorner = buffer.readBoolean();
		dependency = chapter.file.getQuest(buffer.readLong());
	}

	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		config.addDouble("x", x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("y", y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("width", width, v -> width = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("height", height, v -> height = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("rotation", rotation, v -> rotation = v, 0, -180, 180);
		config.add("image", new ImageResourceConfig(), ImageResourceConfig.getIdentifier(image),
				v -> setImage(Icon.getIcon(v)), Identifier.withDefaultNamespace("textures/gui/presets/isles.png"));
		config.addColor("color", color, v -> color = v, Color4I.WHITE);
		config.addInt("order", order, v -> order = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		config.addInt("alpha", alpha, v -> alpha = v, 255, 0, 255);
		config.addList("hover", hover, new StringConfig(), "");
		config.addString("click", click, v -> click = v, "");
		config.addBool("dev", editorsOnly, v -> editorsOnly = v, false);
		config.addBool("corner", alignToCorner, v -> alignToCorner = v, false);

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
	public void initiateMoveClientSide(Chapter to, double _x, double _y) {
		x = _x;
		y = _y;

		if (to != chapter) {
			chapter.removeImage(this);
			NetworkManager.sendToServer(EditObjectMessage.forQuestObject(chapter));

			chapter = to;
			chapter.addImage(this);
		}

		NetworkManager.sendToServer(EditObjectMessage.forQuestObject(chapter));
	}

	@Override
	public void onMoved(double x, double y, long chapterId) {
		// do nothing; image moving is handled via EditObjectMessage
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void drawMoved(GuiGraphics graphics) {
		var poseStack = graphics.pose();

		poseStack.pushMatrix();

		if (alignToCorner) {
			image.withColor(Color4I.WHITE.withAlpha(50)).draw(graphics, 0, 0, 1, 1);
		} else {
			poseStack.translate(0.5f, 0.5f);
			poseStack.scale(0.5F, 0.5F);
			image.withColor(Color4I.WHITE.withAlpha(50)).draw(graphics, -1, -1, 2, 2);
		}

//		QuestShape.get(getShape()).getOutline()
//				.withColor(Color4I.WHITE.withAlpha(30))
//				.draw(graphics, 0, 0, 1, 1);

		poseStack.popMatrix();
	}

	@Override
	public void copyToClipboard() {
		clipboard = new WeakReference<>(this);
		Widget.setClipboardString(ChapterImage.FTBQ_IMAGE);
	}

	@Override
	public Component getTitle() {
		return Component.literal(image.toString());
	}

	public boolean isAspectRatioOff() {
		return !Mth.equal(image.aspectRatio(), width / height);
	}

	public void fixupAspectRatio(boolean adjustWidth) {
		if (isAspectRatioOff()) {
			if (adjustWidth) {
				width = height * image.aspectRatio();
			} else {
				height = width / image.aspectRatio();
			}
			NetworkManager.sendToServer(EditObjectMessage.forQuestObject(chapter));
		}
	}

	public ChapterImage copy(Chapter newChapter, double newX, double newY) {
		ChapterImage copy = new ChapterImage(newChapter);
		copy.readData(writeData(new CompoundTag()));
		copy.setPosition(newX, newY);
		return copy;
	}

	public boolean shouldShowImage(TeamData teamData) {
		return !editorsOnly && (dependency == null || teamData.isCompleted(dependency));
	}

	public static boolean isImageInClipboard() {
		return Widget.getClipboardString().equals(FTBQ_IMAGE);
	}
}
