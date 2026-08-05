package dev.ftb.mods.ftbquests.quest;

import com.mojang.serialization.Codec;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableImageResource;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbquests.client.config.EditableQuestObject;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public final class ChapterImage extends QuestObjectBase implements Movable {
	private Chapter chapter;
	private double x, y;
	private double width, height;
	private double rotation;
	private Icon<?> image;
	private Color4I color;
	private int alpha;
	private ImageClickAction clickAction;
	private boolean editorsOnly;
	private boolean alignToCorner;
	@Nullable
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

	public Icon<?> getImage() {
		return image;
	}

	public ChapterImage setImage(Icon<?> image) {
		this.image = image;
		return this;
	}

	@Override
	public ChapterImage setPosition(double x, double y) {
		this.x = x;
		this.y = y;
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
	public void writeData(Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);

		json.addProperty("x", x);
		json.addProperty("y", y);
		json.addProperty("width", width);
		json.addProperty("height", height);
		json.addProperty("rotation", rotation);
		json.addProperty("image", image.toString());
		if (!color.equals(Color4I.WHITE)) json.addProperty("color", color.rgb());
		if (alpha != 255) json.addProperty("alpha", alpha);
		if (order != 0) json.addProperty("order", order);
		if (!clickAction.isNone()) {
			Json5Util.store(json, "click_action", ImageClickAction.CODEC, clickAction);
		}
		if (editorsOnly) json.addProperty("dev", true);
		if (alignToCorner) json.addProperty("corner", true);
		if (dependency != null) json.addProperty("dependency", dependency.getCodeString());
		if (positionLocked) json.addProperty("position_locked", true);
		if (textOnImage) json.addProperty("text_on_image", true);
		if (textShadow) json.addProperty("text_shadow", true);
		if (textInset != 0) json.addProperty("text_inset", textInset);
		if (textHorizAlign != TextAlign.NAME_MAP.defaultValue) Json5Util.store(json, "text_h_align", TextAlign.CODEC, textHorizAlign);
		if (textVertAlign != TextAlign.NAME_MAP.defaultValue) Json5Util.store(json, "text_v_align", TextAlign.CODEC, textVertAlign);
	}

	@Override
	public void readData(Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);

		x = Json5Util.getDouble(json, "x").orElseThrow();
		y = Json5Util.getDouble(json, "y").orElseThrow();
		width = Json5Util.getDouble(json, "width").orElseThrow();
		height = Json5Util.getDouble(json, "height").orElseThrow();
		rotation = Json5Util.getDouble(json, "rotation").orElseThrow();
		setImage(Icon.getIcon(Json5Util.getString(json, "image").orElseThrow()));
		color = Json5Util.getInt(json, "color").map(Color4I::rgb).orElse(Color4I.WHITE);
		alpha = Json5Util.getInt(json, "alpha").orElse(255);
		order = Json5Util.getInt(json, "order").orElse(0);
		if (json.has("click")) {
			// TODO legacy, remove in 26.2+
			clickAction = ImageClickAction.fromLegacy(Json5Util.getString(json, "click").orElse(""));
		} else {
			clickAction = Json5Util.fetch(json, "click_action", ImageClickAction.CODEC).orElse(ImageClickAction.NONE);
		}
		editorsOnly = Json5Util.getBoolean(json,"dev").orElse(false);
		alignToCorner = Json5Util.getBoolean(json,"corner").orElse(false);
		dependency = Json5Util.getString(json, "dependency")
				.map(dependency -> chapter.file.getQuest(chapter.file.getID(dependency)))
				.orElse(null);
		positionLocked = Json5Util.getBoolean(json, "position_locked").orElse(false);
		textOnImage = Json5Util.getBoolean(json, "text_on_image").orElse(false);
		textShadow = Json5Util.getBoolean(json, "text_shadow").orElse(false);
		textInset = Json5Util.getInt(json, "text_inset").orElse(0);
		textHorizAlign = Json5Util.fetch(json, "text_h_align", TextAlign.CODEC).orElse(TextAlign.NAME_MAP.defaultValue);
		textVertAlign = Json5Util.fetch(json, "text_v_align", TextAlign.CODEC).orElse(TextAlign.NAME_MAP.defaultValue);
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);

		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeDouble(width);
		buffer.writeDouble(height);
		buffer.writeDouble(rotation);
		Icon.STREAM_CODEC.encode(buffer, image);
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
		setImage(Icon.STREAM_CODEC.decode(buffer));
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

	@Override
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);

		config.addDouble("x", x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("y", y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("width", width, v -> width = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("height", height, v -> height = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("rotation", rotation, v -> rotation = v, 0, -180, 180);
		config.add("image", new EditableImageResource(), EditableImageResource.getIdentifier(image),
				v -> setImage(Icon.getIcon(v)), Identifier.withDefaultNamespace("textures/gui/presets/isles.png"));
		config.addColor("color", color, v -> color = v, Color4I.WHITE);
		config.addInt("order", order, v -> order = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		config.addInt("alpha", alpha, v -> alpha = v, 255, 0, 255);
		config.addEnum("click_action_type", clickAction.actionType(), v -> clickAction = clickAction.withType(v), ImageClickAction.ActionType.NAME_MAP);
		config.addString("click_action_data", clickAction.actionData(), v -> clickAction = clickAction.withData(v), "");
		config.addBool("dev", editorsOnly, v -> editorsOnly = v, false);
		config.addBool("corner", alignToCorner, v -> alignToCorner = v, false);
		config.addBool("position_locked", positionLocked, v -> positionLocked = v, false);

		EditableConfigGroup text = config.getOrCreateSubgroup("text");
		var editable = text.addBool("text_on_image", textOnImage, v -> textOnImage = v, false);
		text.addEnum("text_h_align", textHorizAlign, v -> textHorizAlign = v, TextAlign.NAME_MAP).setCanEdit(editable::getValue);
		text.addEnum("text_v_align", textVertAlign, v -> textVertAlign = v, TextAlign.NAME_MAP).setCanEdit(editable::getValue);
		text.addBool("text_shadow", textShadow, v -> textShadow = v, false).setCanEdit(editable::getValue);
		text.addInt("text_inset", textInset, v -> textInset = v, 0, 0, 50).setCanEdit(editable::getValue);

		Predicate<@Nullable QuestObjectBase> depTypes = object -> object == null || object instanceof Quest;
		config.add("dependency", new EditableQuestObject<>(depTypes), dependency, v -> dependency = v, null).setNameKey("ftbquests.dependency");
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
	public Icon<?> getAltIcon() {
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
	public void drawMoved(GuiGraphicsExtractor graphics) {
		var poseStack = graphics.pose();

		poseStack.pushMatrix();

		if (alignToCorner) {
			IconHelper.renderIcon(image.withColor(Color4I.WHITE.withAlpha(50)), graphics, 0, 0, 1, 1);
		} else {
			poseStack.translate(0.5f, 0.5f);
			poseStack.scale(0.5F, 0.5F);
			IconHelper.renderIcon(image.withColor(Color4I.WHITE.withAlpha(50)), graphics, -1, -1, 2, 2);
		}

		poseStack.popMatrix();
	}

	public boolean isAspectRatioOff() {
		return !Mth.equal(IconHelper.aspectRatio(image), width / height);
	}

	public void fixupAspectRatio(boolean adjustWidth) {
		if (isAspectRatioOff()) {
			var aspect = IconHelper.aspectRatio(image);
			if (adjustWidth) {
				width = height * aspect;
			} else {
				height = width / aspect;
			}
			Play2ServerNetworking.send(EditObjectMessage.forQuestObject(this));
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
