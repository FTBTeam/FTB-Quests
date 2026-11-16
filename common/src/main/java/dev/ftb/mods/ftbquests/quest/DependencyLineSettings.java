package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.config.Tristate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/// Some common functionality shared by Quest and QuestLink
public interface DependencyLineSettings {
    /// Extract current dependency visibility settings from the quest object. This should return a map entry
    /// for every single dependency; `Tristate.DEFAULT` can be used to indicate a fallback to default visibility.
    Map<Long, Tristate> depLineVisibilityAsTristateMap();

    /// Apply new dependency visibility settings after a GUI change.
    void updateDepLineVisibilityFromGui(Map<Long, Tristate> value);

    /// @return whether this quest object should default to hiding its dependency lines
    boolean shouldHideDependencyLines();

    /// @return whether dependency lines for the specific dependency should be hidden
    boolean shouldHideDependencyLines(Quest dep);

    /// @return whether lines pointing from this quest object to its dependents should be hidden
    boolean shouldHideDependentLines();

    static Map<Long, Tristate> depLineVisibilityAsTristateMap(Stream<QuestObject> deps, Map<Long, Boolean> currentVisibility) {
        Map<Long, Tristate> res = new HashMap<>();
        deps.forEach(dep -> res.put(dep.getId(), currentVisibility.containsKey(dep.getId()) ?
                (currentVisibility.get(dep.getId()) ? Tristate.TRUE : Tristate.FALSE) :
                Tristate.DEFAULT));
        return res;
    }

    static void updateDepLineVisibility(Map<Long, Boolean> into, Map<Long, Tristate> newValues, BaseQuestFile file) {
        into.clear();
        newValues.forEach((id, val) -> {
            if (val != Tristate.DEFAULT && file.getQuest(id) != null) {
                into.put(id, val.isTrue());
            }
        });
    }

    static Tag writeDepLineVisibility(Map<Long, Boolean> depLineVisibility) {
        CompoundTag tag = new CompoundTag();
        depLineVisibility.forEach((id, val) -> {
            tag.putBoolean(QuestObject.getCodeString(id), val);
        });
        return tag;
    }

    static Map<Long, Boolean> readDepLineVisibility(CompoundTag tag) {
        Map<Long, Boolean> res = new HashMap<>();
        tag.getAllKeys().forEach(key -> {
            long id = QuestObjectBase.parseCodeString(key);
            if (id != 0L) {
                res.put(id, tag.getBoolean(key));
            }
        });
        return res;
    }
}
