package dev.ftb.mods.ftbquests.quest;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableImageResource;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.client.config.EditableQuestObject;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;

import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

public final class ChapterImage extends QuestObjectBase implements Movable {
	private Chapter chapter;
	private double x, y;
	private double width, height;
	private double rotation;
	private Icon<?> image;
	private Color4I color;
	private int alpha;
	private String click;
	private boolean editorsOnly;
	private boolean alignToCorner;
	@Nullable
	private Quest dependency;
	private int order;

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
		click = "";
		editorsOnly = false;
		alignToCorner = false;
		dependency = null;
		order = 0;
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

	public String getClick() {
		return click;
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
		if (!click.isEmpty()) nbt.putString("click", click);
		if (editorsOnly) nbt.putBoolean("dev", true);
		if (alignToCorner) nbt.putBoolean("corner", true);
		if (dependency != null) nbt.putString("dependency", dependency.getCodeString());
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);

		x = nbt.getDouble("x").orElseThrow();
		y = nbt.getDouble("y").orElseThrow();
		width = nbt.getDouble("width").orElseThrow();
		height = nbt.getDouble("height").orElseThrow();
		rotation = nbt.getDouble("rotation").orElseThrow();
		setImage(Icon.getIcon(nbt.getString("image").orElseThrow()));
		color = nbt.getInt("color").map(Color4I::rgb).orElse(Color4I.WHITE);
		alpha = nbt.getInt("alpha").orElse(255);
		order = nbt.getInt("order").orElse(0);
		click = nbt.getStringOr("click", "");
		editorsOnly = nbt.getBooleanOr("dev", false);
		alignToCorner = nbt.getBooleanOr("corner", false);
		dependency = nbt.getString("dependency")
				.map(dependency -> chapter.file.getQuest(chapter.file.getID(dependency)))
				.orElse(null);
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
		buffer.writeUtf(click, Short.MAX_VALUE);
		buffer.writeBoolean(editorsOnly);
		buffer.writeBoolean(alignToCorner);
		buffer.writeLong(dependency == null ? 0L : dependency.id);
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
		click = buffer.readUtf(Short.MAX_VALUE);
		editorsOnly = buffer.readBoolean();
		alignToCorner = buffer.readBoolean();
		dependency = chapter.file.getQuest(buffer.readLong());
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
		config.addString("click", click, v -> click = v, "");
		config.addBool("dev", editorsOnly, v -> editorsOnly = v, false);
		config.addBool("corner", alignToCorner, v -> alignToCorner = v, false);

		Predicate<@Nullable QuestObjectBase> depTypes = object -> object == null || object instanceof Quest;
		config.add("dependency", new EditableQuestObject<>(depTypes), dependency, v -> dependency = v, null).setNameKey("ftbquests.dependency");
	}

	@Override
	public void onCreated() {
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
		this.chapter = newChapter;
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
	public void drawMoved(GuiGraphics graphics) {
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
			NetworkManager.sendToServer(EditObjectMessage.forQuestObject(chapter));
		}
	}

	public boolean shouldShowImage(TeamData teamData) {
		return !editorsOnly && (dependency == null || teamData.isCompleted(dependency));
	}
}
