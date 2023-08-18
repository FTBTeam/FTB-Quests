package dev.ftb.mods.ftbquests.api;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public interface QuestFile {
    boolean isServerSide();

    boolean canEdit();

    @Nullable TeamData getNullableTeamData(UUID id);

    TeamData getOrCreateTeamData(UUID teamId);

    TeamData getOrCreateTeamData(Team team);

    TeamData getOrCreateTeamData(Entity player);

    Collection<TeamData> getAllTeamData();

    void forAllChapters(Consumer<Chapter> consumer);

    void forAllQuests(Consumer<Quest> consumer);

    void forAllQuestLinks(Consumer<QuestLink> consumer);
}
