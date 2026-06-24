package dev.ftb.mods.ftbquests.quest;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class ChapterImage extends QuestObjectBase implements Movable {
	private Chapter chapter;
	private double x, y;
	private double width, height;
	private double rotation;
	private Icon image;
	private Color4I color;
	private int alpha;
	private String click;
	private boolean editorsOnly;
	private boolean alignToCorner;
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

		click = nbt.getString("click");
		editorsOnly = nbt.getBoolean("dev");
		alignToCorner = nbt.getBoolean("corner");

		dependency = nbt.contains("dependency") ? chapter.file.getQuest(chapter.file.getID(nbt.get("dependency"))) : null;
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
		setImage(NetUtils.readIcon(buffer));
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
		config.addString("click", click, v -> click = v, "");
		config.addBool("dev", editorsOnly, v -> editorsOnly = v, false);
		config.addBool("corner", alignToCorner, v -> alignToCorner = v, false);

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
			NetworkManager.sendToServer(EditObjectMessage.forQuestObject(chapter));
		}
	}

	public boolean shouldShowImage(TeamData teamData) {
		return !editorsOnly && (dependency == null || teamData.isCompleted(dependency));
	}
}
