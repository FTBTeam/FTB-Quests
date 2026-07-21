package dev.ftb.mods.ftbquests.quest;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.net.MoveMovableMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Represents a quest object which
 * <ul>
 *     <li>Has a (X,Y) position on the screen, which can be adjusted</li>
 *     <li>Belongs to a chapter, and can be moved to a different chapter</li>
 * </ul>
 */
public interface Movable {
	/**
	 * Get the unique quest object ID for this movable.
	 * @return the ID
	 */
	long getMovableID();

	Chapter getChapter();

	QuestObjectType getObjectType();

	/**
	 * Change the chapter for this movable object. This method is also responsible for updating the chapters,
	 * to remove the object from the old one and add it to the new one. This is a no-op if the new chapter is the
	 * same as the current chapter.
	 *
	 * @param newChapter the chapter to move to
	 */
	void setChapter(Chapter newChapter);

	double getX();

	double getY();

	Movable setPosition(double x, double y);

	double getWidth();

	double getHeight();

	String getShape();

	default double getRotation() {
		return 0.0;
	}

	default boolean isAlignToCorner() {
		return false;
	}

	/**
	 * {@return true if this object's position has been locked to prevent accidental moving}
	 */
	default boolean isPositionLocked() {
		return false;
	}

	/**
	 * Called client-side to initiate the actual move
	 *
	 * @param to new chapter
	 * @param x new X pos
	 * @param y new Y pos
	 */
	default void requestMove(Chapter to, double x, double y) {
		if (!isPositionLocked()) {
			NetworkManager.sendToServer(new MoveMovableMessage(getMovableID(), to.getId(), x, y));
		}
	}

	/**
	 * Called on the client when the object ID is copied via context menu or pressing Ctrl-C
	 */
	default void copyToClipboard() {
		FTBQuestsClient.copyToClipboard(QuestObjectBase.getCodeString(getMovableID()));
	}

	Component getTitle();

	@Environment(EnvType.CLIENT)
	default void drawMoved(GuiGraphics graphics) {
		QuestShape.get(getShape()).getShape().withColor(Color4I.WHITE.withAlpha(30)).draw(graphics, 0, 0, 1, 1);
	}
}