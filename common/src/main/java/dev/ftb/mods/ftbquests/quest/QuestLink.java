package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.util.FTBQCodecs;
import dev.architectury.networking.NetworkManager;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class QuestLink extends QuestObject implements Movable, Excludable, DepLineVisibilityProvider {
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
    public Icon getAltIcon() {
        return getQuest().map(Quest::getAltIcon).orElse(null);
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
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);

        config.addEnum("shape", shape.isEmpty() ? "default" : shape, v -> shape = v.equals("default") ? "" : v, QuestShape.idMapWithDefault);
        config.addDouble("size", size, v -> size = v, 1, 0.0625D, 8D);
        config.addDouble("x", x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        config.addDouble("y", y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        config.addTristate("hide_dependency_lines", hideDependencyLines, v -> hideDependencyLines = v);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void editedFromGUI() {
        QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
        if (gui != null) {
            gui.refreshQuestPanel();
            gui.refreshViewQuestPanel();
        }
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);

        nbt.putString("linked_quest", getCodeString(linkId));
        nbt.putDouble("x", x);
        nbt.putDouble("y", y);
        if (!shape.isEmpty()) {
            nbt.putString("shape", shape);
        }
        if (size != 1D) {
            nbt.putDouble("size", size);
        }
        hideDependencyLines.write(nbt, "hide_dependency_lines");
        if (!depLineVisibility.isEmpty()) {
            nbt.put("dep_line_visibility", DepLineVisibilityProvider.writeDepLineVisibility(depLineVisibility));
        }
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);

        linkId = Long.parseLong(nbt.getString("linked_quest"), 16);
        x = nbt.getDouble("x");
        y = nbt.getDouble("y");
        shape = nbt.getString("shape");
        if (shape.equals("default")) {
            shape = "";
        }
        size = nbt.contains("size") ? nbt.getDouble("size") : 1D;
        hideDependencyLines = Tristate.read(nbt, "hide_dependency_lines");
        depLineVisibility.clear();
        if (nbt.contains("dep_line_visibility")) {
            depLineVisibility.putAll(DepLineVisibilityProvider.readDepLineVisibility(nbt.getCompound("dep_line_visibility")));
        }
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
    public Movable setPosition(double qx, double qy) {
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

    /**
     * {@return true if lines pointing to this link should be hidden}. Unless overridden on the link itself, this
     * follows the linked quest's setting.
     */
    public boolean shouldHideDependencyLines() {
        return hideDependencyLines.get(getQuest().map(Quest::shouldHideDependencyLines).orElse(chapter.defaultHideDependencyLines));
    }

    /**
     * {@return true if the line from the given dependency to this link should be hidden}. A per-dependency setting on
     * this link takes priority, then the link's own setting, and finally the linked quest's setting.
     */
    public boolean shouldHideDependencyLines(Quest dep) {
        if (depLineVisibility.containsKey(dep.getId())) {
            return !depLineVisibility.get(dep.getId());
        }
        if (hideDependencyLines != Tristate.DEFAULT) {
            return hideDependencyLines.isTrue();
        }
        return getQuest().map(q -> q.shouldHideDependencyLines(dep)).orElse(chapter.defaultHideDependencyLines);
    }

    @Override
    public Map<Long, Tristate> depLineVisibilityAsTristateMap() {
        return DepLineVisibilityProvider.depLineVisibilityAsTristateMap(getQuest().stream().flatMap(Quest::streamDependencies), depLineVisibility);
    }

    @Override
    public void updateDepLineVisibilityFromGui(Map<Long, Tristate> value) {
        if (!getQuestFile().isServerSide()) {
            DepLineVisibilityProvider.updateDepLineVisibility(depLineVisibility, value, getQuestFile());
            NetworkManager.sendToServer(EditObjectMessage.forQuestObject(this));
        }
    }
}
