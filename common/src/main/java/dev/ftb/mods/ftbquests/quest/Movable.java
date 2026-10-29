package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.net.MoveMovableMessage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Represents a quest object that can be moved around on screen, and between chapters.
 */
public interface Movable {
	/**
	 * Get the unique quest object ID for this movable.
	 * @return the ID
	 */
	long getMovableID();

	Chapter getChapter();

	QuestObjectType getObjectType();

	void setChapter(Chapter newChapter);

	double getX();

	double getY();

	Movable setPosition(double x, double y);

	double getWidth();

	double getHeight();

	String getShape();

	/**
	 * {@return true if this object can be interactively resized in the quest screen}
	 */
	default boolean isResizable() {
		return false;
	}

	/**
	 * {@return true if width and height can be changed independently; if false, objects are always square}
	 */
	default boolean isFreeformResizable() {
		return false;
	}

	default double getMinSize() {
		return 0.0625D;
	}

	default double getMaxSize() {
		return 8D;
	}

	/**
	 * Set the size of this object, client-side only. Callers are responsible for sending the change to the server.
	 * Square-only objects use the width and ignore the height.
	 */
	default void setDimensions(double width, double height) {
	}

	default double getRotation() {
		return 0.0;
	}

	/**
	 * {@return true if this object's rotation can be interactively changed in the quest screen}
	 */
	default boolean isRotatable() {
		return false;
	}

	/**
	 * Set the rotation of this object in degrees, client-side only. Callers are responsible for sending the change to the server.
	 */
	default void setRotation(double degrees) {
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
			Play2ServerNetworking.send(new MoveMovableMessage(getMovableID(), to.getId(), x, y));
		}
	}

	/**
	 * Called on the client when the object is copied via context menu or pressing Ctrl-C
	 */
	default void copyToClipboard() {
		FTBQuestsClient.copyToClipboard(QuestObjectBase.getCodeString(getMovableID()));
	}

	Component getTitle();

	/**
	 * Draw a "ghost" image of the object currently being moved.
	 *
	 * @param graphics the graphics context
	 */
	default void drawMoved(GuiGraphicsExtractor graphics) {
		IconHelper.renderIcon(QuestShape.get(getShape()).getShape().withColor(Color4I.WHITE.withAlpha(30)), graphics, 0, 0, 1, 1);
	}
}
