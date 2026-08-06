package dev.ftb.mods.ftbquests.integration;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.integration.permissions.PermissionHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PermissionsHelper {
    public static final String EDITOR_PERM = "ftbquests.editor";

    /**
     * Check if this command source stack has editor permissions for the questbook. Used for most FTB Quests commands.
     *
     * @param source the command source
     * @return true if the player has editor permissions
     */
    public static boolean hasEditorPermission(CommandSourceStack source) {
        // has GM perm level or better, or has "ftbquests.editor" FTB Ranks node

        //noinspection DataFlowIssue
        return source.isPlayer() && hasEditorPermission(source.getPlayer());
    }

    /**
     * Check if this player has editor permissions. See {@link #hasEditorPermission(CommandSourceStack)} above. This
     * method will work for client players but of course should not be considered authoritative there.
     *
     * @param player the player to check
     * @return true if the player has editor permissions
     */
    public static boolean hasEditorPermission(Player player) {
        return player.hasPermissions(Commands.LEVEL_GAMEMASTERS)
                || player instanceof ServerPlayer sp && PermissionHelper.INSTANCE.getProvider().getBooleanPermission(sp, EDITOR_PERM, false);
    }

    /**
     * Check if the player has editor permissions <em>and</em> is currently in editing mode.
     *
     * @param context the packet context for a network packet.
     * @return true if the player is a valid editor right now
     */
    public static boolean canPlayerEdit(NetworkManager.PacketContext context) {
        return canPlayerEdit(context.getPlayer());
    }

    /**
     * Check if the player has editor permissions <em>and</em> is currently in editing mode.
     *
     * @param player the player to check; if null, this method will always return false
     * @return true if the player is a valid editor right now
     */
    public static boolean canPlayerEdit(Player player) {
        return player != null
                && hasEditorPermission(player)
                && FTBQuestsAPI.api().getQuestFile(player.level().isClientSide).getTeamData(player)
                .map(d -> d.isPlayerInEditMode(player))
                .orElse(false);
    }
}
