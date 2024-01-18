package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public interface Movable {
	long getMovableID();

	Chapter getChapter();

	double getX();

	double getY();

	double getWidth();

	double getHeight();

	String getShape();

	/**
	 * Called client-side to initiate moving the object
	 *
	 * @param to new chapter
	 * @param x new X pos
	 * @param y new Y pos
	 */
	@Environment(EnvType.CLIENT)
	void move(Chapter to, double x, double y);

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

	@Environment(EnvType.CLIENT)
	default void drawMoved(GuiGraphics graphics) {
		QuestShape.get(getShape()).getShape().withColor(Color4I.WHITE.withAlpha(30)).draw(graphics, 0, 0, 1, 1);
	}
}