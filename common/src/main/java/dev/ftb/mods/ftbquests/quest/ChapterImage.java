package dev.ftb.mods.ftbquests.quest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class ChapterImage extends QuestObjectBase implements Movable {
	private Chapter chapter;
	private double x, y;
	private double width, height;
	private double rotation;
	private Icon image;
	private Color4I color;
	private int alpha;
	private ImageClickAction clickAction;
	private boolean editorsOnly;
	private boolean alignToCorner;
	private Quest dependency;
	private int order;
	private boolean positionLocked;
	private boolean textOnImage;
	private TextAlign textHorizAlign;
	private TextAlign textVertAlign;
	private int textInset;
	private boolean textShadow;

	public ChapterImage(long id, Chapter chapter) {
		super(id);

		this.chapter = chapter;

		x = y = 0D;
		width = 1D;
		height = 1D;
		rotation = 0D;
		image = Color4I.empty();
		color = Color4I.WHITE;
		alpha = 255;
		clickAction = ImageClickAction.NONE;
		editorsOnly = false;
		alignToCorner = false;
		dependency = null;
		order = 0;
		positionLocked = false;
		textOnImage = false;
		textHorizAlign = TextAlign.MIDDLE;
		textVertAlign = TextAlign.MIDDLE;
		textInset = 0;
		textShadow = false;
	}

	public Icon getImage() {
		return image;
	}

	public ChapterImage setImage(Icon image) {
		this.image = image;
		return this;
	}

	@Override
	public ChapterImage setPosition(double x, double y) {
		this.x = x;
		this.y = y;
		getQuestFile().markDirty();
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

	@Override
	public double getRotation() {
		return rotation;
	}

	@Override
	public boolean isAlignToCorner() {
		return alignToCorner;
	}

	public ImageClickAction getClickAction() {
		return clickAction;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);

		nbt.putDouble("x", x);
		nbt.putDouble("y", y);
		nbt.putDouble("width", width);
		nbt.putDouble("height", height);
		nbt.putDouble("rotation", rotation);
		nbt.putString("image", image.toString());
		if (!color.equals(Color4I.WHITE)) nbt.putInt("color", color.rgb());
		if (alpha != 255) nbt.putInt("alpha", alpha);
		if (order != 0) nbt.putInt("order", order);
		if (clickAction != ImageClickAction.NONE) {
			nbt.putString("click_action", clickAction.toString());
		}
		if (editorsOnly) nbt.putBoolean("dev", true);
		if (alignToCorner) nbt.putBoolean("corner", true);
		if (dependency != null) nbt.putString("dependency", dependency.getCodeString());

		if (positionLocked) nbt.putBoolean("position_locked", true);
		if (textOnImage) nbt.putBoolean("text_on_image", true);
		if (textShadow) nbt.putBoolean("text_shadow", true);
		if (textInset != 0) nbt.putInt("text_inset", textInset);
		if (textHorizAlign != TextAlign.NAME_MAP.defaultValue) nbt.putString("text_h_align", textHorizAlign.id);
		if (textVertAlign != TextAlign.NAME_MAP.defaultValue) nbt.putString("text_v_align", textVertAlign.id);

	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);

		x = nbt.getDouble("x");
		y = nbt.getDouble("y");
		width = nbt.getDouble("width");
		height = nbt.getDouble("height");
		rotation = nbt.getDouble("rotation");
		setImage(Icon.getIcon(nbt.getString("image")));
		color = nbt.contains("color") ? Color4I.rgb(nbt.getInt("color")) : Color4I.WHITE;
		alpha = nbt.contains("alpha") ? nbt.getInt("alpha") : 255;
		order = nbt.getInt("order");

		if (nbt.contains("hover")) {
			// legacy - now using the title instead
			ListTag hoverTag = nbt.getList("hover", Tag.TAG_STRING);
			List<String> hover = new ArrayList<>();
			for (int i = 0; i < hoverTag.size(); i++) {
				hover.add(hoverTag.getString(i));
			}
			setRawTitle(String.join("\\n", hover));
		}

		if (nbt.contains("click")) {
			// legacy
			clickAction = ImageClickAction.fromLegacy(nbt.getString("click"));
		} else {
			clickAction = ImageClickAction.fromString(nbt.getString("click_action"));
		}

		editorsOnly = nbt.getBoolean("dev");
		alignToCorner = nbt.getBoolean("corner");

		dependency = nbt.contains("dependency") ? chapter.file.getQuest(chapter.file.getID(nbt.get("dependency"))) : null;

		positionLocked = nbt.getBoolean("position_locked");
		textOnImage = nbt.getBoolean("text_on_image");
		textShadow = nbt.getBoolean("text_shadow");
		textInset = nbt.getInt("text_inset");
		textHorizAlign = TextAlign.NAME_MAP.get(nbt.getString("text_h_align"));
		textVertAlign = TextAlign.NAME_MAP.get(nbt.getString("text_v_align"));
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);

		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeDouble(width);
		buffer.writeDouble(height);
		buffer.writeDouble(rotation);
		NetUtils.writeIcon(buffer, image);
		buffer.writeInt(color.rgb());
		buffer.writeInt(alpha);
		buffer.writeInt(order);
		ImageClickAction.STREAM_CODEC.encode(buffer, clickAction);
		buffer.writeLong(dependency == null ? 0L : dependency.id);

		int flags = 0;
		flags = Bits.setFlag(flags,0x01, editorsOnly);
		flags = Bits.setFlag(flags,0x02, alignToCorner);
		flags = Bits.setFlag(flags,0x04, positionLocked);
		flags = Bits.setFlag(flags,0x08, textOnImage);
		flags = Bits.setFlag(flags,0x10, textShadow);
		buffer.writeByte(flags);

		if (textOnImage) buffer.writeVarInt(textInset);
		buffer.writeEnum(textHorizAlign);
		buffer.writeEnum(textVertAlign);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);

		x = buffer.readDouble();
		y = buffer.readDouble();
		width = buffer.readDouble();
		height = buffer.readDouble();
		rotation = buffer.readDouble();
		setImage(NetUtils.readIcon(buffer));
		color = Color4I.rgb(buffer.readInt());
		alpha = buffer.readInt();
		order = buffer.readInt();
		clickAction = ImageClickAction.STREAM_CODEC.decode(buffer);
		dependency = chapter.file.getQuest(buffer.readLong());

		int flags = buffer.readByte();
		editorsOnly = Bits.getFlag(flags, 0x01);
		alignToCorner = Bits.getFlag(flags, 0x02);
		positionLocked = Bits.getFlag(flags, 0x04);
		textOnImage = Bits.getFlag(flags, 0x08);
		textShadow = Bits.getFlag(flags, 0x10);

		textInset = textOnImage ? buffer.readVarInt() : 0;
		textHorizAlign = buffer.readEnum(TextAlign.class);
		textVertAlign = buffer.readEnum(TextAlign.class);
	}

	@Override
	public QuestObjectType getObjectType() {
		return QuestObjectType.IMAGE;
	}

	@Override
	public BaseQuestFile getQuestFile() {
		return chapter.file;
	}

	@Override
	protected boolean hasIconConfig() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		config.addDouble("x", x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("y", y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("width", width, v -> width = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("height", height, v -> height = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("rotation", rotation, v -> rotation = v, 0, -180, 180);
		config.add("image", new ImageResourceConfig(), ImageResourceConfig.getResourceLocation(image),
				v -> setImage(Icon.getIcon(v)), ResourceLocation.withDefaultNamespace("textures/gui/presets/isles.png"));
		config.addColor("color", color, v -> color = v, Color4I.WHITE);
		config.addInt("order", order, v -> order = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		config.addInt("alpha", alpha, v -> alpha = v, 255, 0, 255);
		config.addEnum("click_action_type", clickAction.actionType(), v -> clickAction = clickAction.withType(v), ImageClickAction.ActionType.NAME_MAP);
		config.addString("click_action_data", clickAction.actionData(), v -> clickAction = clickAction.withData(v), "");
		config.addBool("dev", editorsOnly, v -> editorsOnly = v, false);
		config.addBool("corner", alignToCorner, v -> alignToCorner = v, false);
		config.addBool("position_locked", positionLocked, v -> positionLocked = v, false);

		ConfigGroup text = config.getOrCreateSubgroup("text");
		var editable = text.addBool("text_on_image", textOnImage, v -> textOnImage = v, false);
		text.addEnum("text_h_align", textHorizAlign, v -> textHorizAlign = v, TextAlign.NAME_MAP).setCanEdit(editable::getValue);
		text.addEnum("text_v_align", textVertAlign, v -> textVertAlign = v, TextAlign.NAME_MAP).setCanEdit(editable::getValue);
		text.addBool("text_shadow", textShadow, v -> textShadow = v, false).setCanEdit(editable::getValue);
		text.addInt("text_inset", textInset, v -> textInset = v, 0, 0, 50).setCanEdit(editable::getValue);

		Predicate<QuestObjectBase> depTypes = object -> object == null || object instanceof Quest;
		config.add("dependency", new ConfigQuestObject<>(depTypes), dependency, v -> dependency = v, null).setNameKey("ftbquests.dependency");
	}

	@Override
	public void onCreated() {
		super.onCreated();

		chapter.addImage(this);
	}

	@Override
	public void deleteSelf() {
		super.deleteSelf();

		chapter.removeImage(this);
	}

	@Override
	public long getParentID() {
		return chapter.getId();
	}

	@Override
	public Component getAltTitle() {
		return Component.empty();
	}

	@Override
	public Icon getAltIcon() {
		return image;
	}

	@Override
	public long getMovableID() {
		return id;
	}

	@Override
	public Chapter getChapter() {
		return chapter;
	}

	@Override
	public void setChapter(Chapter newChapter) {
		if (!Objects.equals(newChapter, getChapter())) {
			getChapter().removeImage(this);
			newChapter.addImage(this);
			this.chapter = newChapter;
		}
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
	public boolean isPositionLocked() {
		return positionLocked;
	}

	public boolean shouldDrawTextOnImage() {
		return textOnImage;
	}

	public TextAlign getHorizontalTextAlign() {
		return textHorizAlign;
	}

	public TextAlign getVerticalTextAlign() {
		return textVertAlign;
	}

	public float getTextInset() {
		return (float) textInset;
	}

	public boolean isTextShadow() {
		return textShadow;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void drawMoved(GuiGraphics graphics) {
		PoseStack poseStack = graphics.pose();

		poseStack.pushPose();

		if (alignToCorner) {
			image.withColor(Color4I.WHITE.withAlpha(50)).draw(graphics, 0, 0, 1, 1);
		} else {
			poseStack.translate(0.5D, 0.5D, 0);
			poseStack.scale(0.5F, 0.5F, 1);
			image.withColor(Color4I.WHITE.withAlpha(50)).draw(graphics, -1, -1, 2, 2);
		}

		poseStack.popPose();
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
			NetworkManager.sendToServer(EditObjectMessage.forQuestObject(this));
		}
	}

	public boolean shouldShowImage(TeamData teamData) {
		return !editorsOnly && (dependency == null || teamData.isCompleted(dependency));
	}

	public enum TextAlign implements StringRepresentable {
		START("start"),
		MIDDLE("middle"),
		END("end");

		public static final NameMap<TextAlign> NAME_MAP = NameMap.of(MIDDLE, TextAlign.values())
				.baseNameKey("ftbquests.image.text_align")
				.create();
		public static final Codec<TextAlign> CODEC = StringRepresentable.fromEnum(TextAlign::values);

		private final String id;

		TextAlign(String id) {
			this.id = id;
		}

		@Override
		public String getSerializedName() {
			return id;
		}
	}
}
