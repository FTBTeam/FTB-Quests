package dev.ftb.mods.ftbquests.api;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface QuestFile {
    /**
     * {@return true if this quest file is on the server, false if on the client}
     */
    boolean isServerSide();

    /**
     * Check if this quest file is editable. This will always return false on the server;
     * use {@link TeamData#getCanEdit(Player)} there.
     * @return true if the file can be edited (clientside), false otherwise
     */
    boolean canEdit();

    /**
     * {@return FTB Quests progress data for the given FTB Teams team UUID, or null if there is no data for this team ID}
     * @param id the team ID to check
     */
    @Nullable TeamData getNullableTeamData(UUID id);

    /**
     * {@return FTB Quests progress data for the given FTB Teams team UUID, creating new progress data if necessary}
     * @param teamId the team ID to check
     */
    TeamData getOrCreateTeamData(UUID teamId);

    /**
     * {@return FTB Quests progress data for the given FTB Teams team, creating new progress data if necessary}
     * @param team the team to check
     */
    TeamData getOrCreateTeamData(Team team);

    /**
     * Get the FTB Quests team progress data for the given player, creating new progress data if necessary.
     * @param player the player to check
     * @return the team data, or {@code Optional.empty()} if the player isn't yet known by FTB Teams (rare, but possible, particularly for players joining the server for the first time)
     */
    Optional<TeamData> getTeamData(Player player);

    /**
     * {@return collection of progress data for all known teams}
     */
    Collection<TeamData> getAllTeamData();

    void forAllChapters(Consumer<Chapter> consumer);

    void forAllQuests(Consumer<Quest> consumer);

    void forAllQuestLinks(Consumer<QuestLink> consumer);
}
