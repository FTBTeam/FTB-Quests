package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftbquests.quest.Movable;

public interface QuestPositionableButton {
     Position getPosition();

     Movable moveAndDeleteFocus();

     record Position(double x, double y, double w, double h) {
     }
}
