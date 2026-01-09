package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface Movable {
	long getMovableID();

	Chapter getChapter();

	double getX();

	double getY();

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
	 * Called client-side to initiate moving the object
	 *
	 * @param to new chapter
	 * @param x new X pos
	 * @param y new Y pos
	 */
	void initiateMoveClientSide(Chapter to, double x, double y);

	/**
	 * Called on both server and client to actually update the object's position; must also update any related objects,
	 * e.g. chapter links if the chapter ID is changing.
	 *
	 * @param x new X pos
	 * @param y new Y pos
	 * @param chapterId new chapter ID
	 */
	void onMoved(double x, double y, long chapterId);

	/**
	 * Called on the client when the object is copied via context menu or pressing Ctrl-C
	 */
	void copyToClipboard();

	Component getTitle();

	default void drawMoved(GuiGraphics graphics) {
		IconHelper.renderIcon(QuestShape.get(getShape()).getShape().withColor(Color4I.WHITE.withAlpha(30)), graphics, 0, 0, 1, 1);
	}
}
