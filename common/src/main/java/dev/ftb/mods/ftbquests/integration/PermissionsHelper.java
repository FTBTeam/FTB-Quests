package dev.ftb.mods.ftbquests.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import dev.ftb.mods.ftblibrary.integration.permissions.PermissionHelper;

public class PermissionsHelper {
    public static final String EDITOR_PERM = "ftbquests.editor";

    public static boolean hasEditorPermission(ServerPlayer player, boolean def) {
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
                || PermissionHelper.getProvider().getBooleanPermission(player, EDITOR_PERM, def);
    }
}
