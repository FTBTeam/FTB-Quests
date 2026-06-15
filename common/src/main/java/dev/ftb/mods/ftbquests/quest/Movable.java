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
	long getMovableID();

	Chapter getChapter();

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
	 * Called client-side to initiate the actual move
	 *
	 * @param to new chapter
	 * @param x new X pos
	 * @param y new Y pos
	 */
	default void initiateMoveClientSide(Chapter to, double x, double y) {
		Play2ServerNetworking.send(new MoveMovableMessage(getMovableID(), to.getId(), x, y));
	}

	/**
	 * Called on both server and client to actually update the object's position; must also update any related objects,
	 * e.g. chapter links if the chapter ID is changing.
	 *
	 * @param newX new X pos
	 * @param newY new Y pos
	 * @param newChapterId new chapter ID
	 */
	default void onMoved(double newX, double newY, long newChapterId) {
		setPosition(newX, newY);

		Chapter oldChapter = getChapter();
		if (newChapterId != oldChapter.getId()) {
			Chapter newChapter = oldChapter.getQuestFile().getChapter(newChapterId);
			if (newChapter != null) {
				oldChapter.removeChildObject(this);
				newChapter.addChildObject(this);
				setChapter(newChapter);
			}
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
