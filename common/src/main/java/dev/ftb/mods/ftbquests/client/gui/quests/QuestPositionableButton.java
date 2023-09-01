package dev.ftb.mods.ftbquests.client.gui.quests;

@FunctionalInterface
public interface QuestPositionableButton {
     Position getPosition();

     record Position(double x, double y, double w, double h) {
     }
}
