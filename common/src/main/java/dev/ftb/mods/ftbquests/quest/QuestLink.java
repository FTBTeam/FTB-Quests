package dev.ftb.mods.ftbquests.quest;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.Tristate;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.util.FTBQCodecs;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class QuestLink extends QuestObject implements Movable, Excludable, DependencyLineSettings {
    private Chapter chapter;
    private long linkId;

    private double x;
    private double y;
    private String shape;
    private double size;
    private Tristate hideDependencyLines;  // lines pointing to this link; default => follow the linked quest
    private final Map<Long, Boolean> depLineVisibility;  // per-dependency override; absent => use hideDependencyLines

    public QuestLink(long id, Chapter chapter, long linkId) {
        super(id);

        this.chapter = chapter;
        this.linkId = linkId;

        shape = "";
        size = 1D;
        hideDependencyLines = Tristate.DEFAULT;
        depLineVisibility = new Long2BooleanOpenHashMap();
    }

    @Override
    public long getEffectiveId() {
        return getQuest().map(QuestObjectBase::getId).orElse(super.getEffectiveId());
    }

    @Override
    @Nullable
    public Quest getRelatedQuest() {
        return getQuest().orElse(null);
    }

    @Override
    public QuestObjectType getObjectType() {
        return QuestObjectType.QUEST_LINK;
    }

    @Override
    public BaseQuestFile getQuestFile() {
        return chapter.file;
    }

    @Override
    public Component getAltTitle() {
        return getQuest().map(Quest::getAltTitle).orElse(Component.empty());
    }

    @Override
    public Icon<?> getAltIcon() {
        // using .map() here doesn't work...
        return getQuest().isPresent() ? getQuest().get().getAltIcon() : Icons.BARRIER;
    }

    @Override
    public boolean isVisible(TeamData data) {
        return getQuest().map(q -> q.isVisible(data)).orElse(false);
    }

    @Override
    public @Nullable Chapter getQuestChapter() {
        return chapter;
    }

    @Override
    public int getRelativeProgressFromChildren(TeamData data) {
        return 0;
    }

    @Override
    public boolean isOptionalForProgression(TeamData teamData) {
        return getQuest().map(q -> q.isOptionalForProgression(teamData)).orElse(false);
    }

    @Override
    public boolean isCompletedRaw(TeamData data) {
        return getQuest().map(q -> q.isCompletedRaw(data)).orElse(false);
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
        super.onCreated();

        chapter.addQuestLink(this);
    }

    @Override
    public void deleteSelf() {
        super.deleteSelf();

        getQuest().ifPresent(Quest::clearCachedData);
        chapter.removeQuestLink(this);
    }

    @Override
    public void fillConfigGroup(EditableConfigGroup config) {
        super.fillConfigGroup(config);

        config.addEnum("shape", shape.isEmpty() ? "default" : shape, v -> shape = v.equals("default") ? "" : v, QuestShape.idMapWithDefault);
        config.addDouble("size", size, v -> size = v, 1, 0.0625D, 8D);
        config.addDouble("x", x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        config.addDouble("y", y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        config.addTristate("hide_dependency_lines", hideDependencyLines, v -> hideDependencyLines = v);
    }

    @Override
    public void editedFromGUI() {
        QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
        if (gui != null) {
            gui.refreshQuestPanel();
            gui.refreshViewQuestPanel();
        }
    }

    @Override
    public void writeData(Json5Object json, HolderLookup.Provider provider) {
        super.writeData(json, provider);

        json.addProperty("linked_quest", getCodeString(linkId));
        json.addProperty("x", x);
        json.addProperty("y", y);
        if (!shape.isEmpty()) json.addProperty("shape", shape);
        if (size != 1D) json.addProperty("size", size);
        hideDependencyLines.write(json, "hide_dependency_lines");
        if (!depLineVisibility.isEmpty()) {
            Json5Util.store(json, "dep_line_visibility", Quest.DEP_LINE_VIS_CODEC, depLineVisibility);
        }
    }

    @Override
    public void readData(Json5Object json, HolderLookup.Provider provider) {
        super.readData(json, provider);

        linkId = Long.parseLong(Json5Util.getString(json, "linked_quest").orElseThrow(), 16);
        x = Json5Util.getDouble(json, "x").orElseThrow();
        y = Json5Util.getDouble(json, "y").orElseThrow();
        shape = Json5Util.getString(json, "shape").orElse("");
        if (shape.equals("default")) {
            shape = "";
        }
        size = Json5Util.getDouble(json, "size").orElse(1D);
        hideDependencyLines = Tristate.read(json, "hide_dependency_lines");
        depLineVisibility.clear();
        Json5Util.fetch(json, "dep_line_visibility", Quest.DEP_LINE_VIS_CODEC).ifPresent(depLineVisibility::putAll);
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);

        buffer.writeLong(linkId);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(size);
        buffer.writeUtf(shape);
        hideDependencyLines.write(buffer);
        FTBQCodecs.LONG_BOOL_MAP_STREAM_CODEC.encode(buffer, depLineVisibility);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);

        linkId = buffer.readLong();
        x = buffer.readDouble();
        y = buffer.readDouble();
        size = buffer.readDouble();
        shape = buffer.readUtf(64);
        hideDependencyLines = Tristate.read(buffer);
        depLineVisibility.clear();
        depLineVisibility.putAll(FTBQCodecs.LONG_BOOL_MAP_STREAM_CODEC.decode(buffer));
    }

    @Override
    public QuestLink setPosition(double qx, double qy) {
        x = qx;
        y = qy;
        getQuestFile().markDirty();
        return this;
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
        if (!Objects.equals(newChapter, getChapter())) {
            getChapter().removeQuestLink(this);
            newChapter.addQuestLink(this);
            this.chapter = newChapter;
        }
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
    public boolean isResizable() {
        return true;
    }

    @Override
    public void setDimensions(double width, double height) {
        size = Math.clamp(width, getMinSize(), getMaxSize());
    }

    @Override
    public double getWidth() {
        return size;
    }

    @Override
    public double getHeight() {
        return size;
    }

    @Override
    public String getShape() {
        return shape.isEmpty() ? chapter.getDefaultQuestShape() : shape;
    }

    public boolean linksTo(Quest quest) {
        return linkId == quest.id;
    }

    @Override
    public boolean isQuestObjectExcluded(TeamData teamData) {
        return getQuest().map(q -> q.isQuestObjectExcluded(teamData)).orElse(false);
    }

    @Override
    public boolean shouldHideDependencyLines() {
        return hideDependencyLines.get(getQuest().map(Quest::shouldHideDependencyLines).orElse(chapter.defaultHideDependencyLines));
    }

    @Override
    public boolean shouldHideDependencyLines(Quest dep) {
        if (depLineVisibility.containsKey(dep.getId())) {
            return !depLineVisibility.get(dep.getId());
        } else if (hideDependencyLines != Tristate.DEFAULT) {
            return hideDependencyLines.isTrue();
        } else {
            return getQuest().map(q -> q.shouldHideDependencyLines(dep)).orElse(chapter.defaultHideDependencyLines);
        }
    }

    @Override
    public Map<Long, Tristate> depLineVisibilityAsTristateMap() {
        return DependencyLineSettings.depLineVisibilityAsTristateMap(getQuest().stream().flatMap(Quest::streamDependencies), depLineVisibility);
    }

    @Override
    public void updateDepLineVisibilityFromGui(Map<Long, Tristate> value) {
        if (!getQuestFile().isServerSide()) {
            DependencyLineSettings.updateDepLineVisibility(depLineVisibility, value, getQuestFile());
            Play2ServerNetworking.send(EditObjectMessage.forQuestObject(this));
        }
    }

    @Override
    public boolean shouldHideDependentLines() {
        return getQuest().map(Quest::shouldHideDependentLines).orElse(false);
    }
}
