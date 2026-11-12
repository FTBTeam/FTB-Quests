package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.config.Tristate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/// Some common functionality shared by Quest and QuestLink
public interface DepLineVisibilityProvider {
    Map<Long, Tristate> depLineVisibilityAsTristateMap();

    void updateDepLineVisibilityFromGui(Map<Long, Tristate> value);

    static Map<Long, Tristate> depLineVisibilityAsTristateMap(Stream<QuestObject> deps, Map<Long, Boolean> visibility) {
        Map<Long, Tristate> res = new HashMap<>();
        deps.forEach(dep -> {
            if (visibility.containsKey(dep.getId())) {
                res.put(dep.getId(), visibility.get(dep.getId()) ? Tristate.TRUE : Tristate.FALSE);
            } else {
                res.put(dep.getId(), Tristate.DEFAULT);
            }
        });
        return res;
    }

    static void updateDepLineVisibility(Map<Long, Boolean> visibility, Map<Long, Tristate> value, BaseQuestFile file) {
        visibility.clear();
        value.forEach((id, val) -> {
            if (val != Tristate.DEFAULT && file.getQuest(id) != null) {
                visibility.put(id, val.isTrue());
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
