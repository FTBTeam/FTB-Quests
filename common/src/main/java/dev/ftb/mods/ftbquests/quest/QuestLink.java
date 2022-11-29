package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.net.MoveMovableMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class QuestLink extends QuestObject implements Movable {
    private Chapter chapter;
    private long linkId;

    private double x;
    private double y;

    public QuestLink(Chapter chapter, long linkId) {
        this.chapter = chapter;
        this.linkId = linkId;
    }

    @Override
    public QuestObjectType getObjectType() {
        return QuestObjectType.QUEST_LINK;
    }

    @Override
    public QuestFile getQuestFile() {
        return chapter.file;
    }

    @Override
    public Component getAltTitle() {
        return getQuest().map(Quest::getAltTitle).orElse(Component.empty());
    }

    @Override
    public Icon getAltIcon() {
        return getQuest().map(Quest::getAltIcon).orElse(null);
    }

    @Override
    public int getRelativeProgressFromChildren(TeamData data) {
        return 0;
    }

    public Optional<Quest> getQuest() {
        return chapter.file.get(linkId) instanceof Quest q ? Optional.of(q) : Optional.empty();
    }

    @Override
    public long getParentID() {
        return chapter.id;
    }

    @Override
    public void onCreated() {
        chapter.questLinks.add(this);
    }

    @Override
    public void deleteSelf() {
        super.deleteSelf();
        chapter.questLinks.remove(this);
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);

        nbt.putString("linked_quest", getCodeString(linkId));
        nbt.putDouble("x", x);
        nbt.putDouble("y", y);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);

        linkId = Long.parseLong(nbt.getString("linked_quest"), 16);
        x = nbt.getDouble("x");
        y = nbt.getDouble("y");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);

        buffer.writeLong(linkId);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);

        linkId = buffer.readLong();
        x = buffer.readDouble();
        y = buffer.readDouble();
    }

    public void setPosition(double qx, double qy) {
        x = qx;
        y = qy;
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
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getWidth() {
        return getQuest().map(Quest::getWidth).orElse(20.0);
    }

    @Override
    public double getHeight() {
        return getQuest().map(Quest::getHeight).orElse(20.0);
    }

    @Override
    public String getShape() {
        return getQuest().map(Quest::getShape).orElse("");
    }

    @Override
    public void move(Chapter to, double x, double y) {
        new MoveMovableMessage(this, to.id, x, y).sendToServer();
    }

    @Override
    public void onMoved(double x, double y, long chapterId) {
        this.x = x;
        this.y = y;

        if (chapterId != chapter.id) {
            QuestFile f = getQuestFile();
            Chapter newChapter = f.getChapter(chapterId);

            if (newChapter != null) {
                chapter.questLinks.remove(this);
                newChapter.questLinks.add(this);
                chapter = newChapter;
            }
        }
    }

    public boolean linksTo(Quest quest) {
        return linkId == quest.id;
    }
}
