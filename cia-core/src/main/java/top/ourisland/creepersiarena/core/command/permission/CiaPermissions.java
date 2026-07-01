package top.ourisland.creepersiarena.core.command.permission;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

public final class CiaPermissions {

    // ====== base ======
    public static final String
            CHOOSEJOB = "creepersiarena.choosejob";

    // ====== player command root ======
    public static final String
            COMMAND = "creepersiarena.command",
            COMMAND_JOIN = "creepersiarena.command.join",
            COMMAND_LEAVE = "creepersiarena.command.leave",
            COMMAND_JOB = "creepersiarena.command.job",
            COMMAND_TEAM = "creepersiarena.command.team",
            COMMAND_LANGUAGE = "creepersiarena.command.language",
            COMMAND_PREFERENCE = "creepersiarena.command.preference",
            COMMAND_BALANCE = "creepersiarena.command.balance",
            COMMAND_STORE = "creepersiarena.command.store",
            COMMAND_PARTICLES = "creepersiarena.command.particles";

    // ====== admin command root ======
    public static final String
            ADMIN = "creepersiarena.command.admin",
            ADMIN_GAME = "creepersiarena.command.admin.game",
            ADMIN_MODE = "creepersiarena.command.admin.mode",
            ADMIN_ARENA = "creepersiarena.command.admin.arena",
            ADMIN_SKIP = "creepersiarena.command.admin.skip",
            ADMIN_COOLDOWN = "creepersiarena.command.admin.cooldown",
            ADMIN_REGENERATION = "creepersiarena.command.admin.regeneration",
            ADMIN_MUTATION = "creepersiarena.command.admin.mutation",
            ADMIN_ABILITY = "creepersiarena.command.admin.ability",
            ADMIN_DATABASE = "creepersiarena.command.admin.database",
            ADMIN_ECONOMY = "creepersiarena.command.admin.economy",
            ADMIN_STORE = "creepersiarena.command.admin.store",
            ADMIN_ENTRANCE = "creepersiarena.command.admin.entrance",
            ADMIN_LANGUAGE = "creepersiarena.command.admin.language",
            ADMIN_RELOAD = "creepersiarena.command.admin.reload",
            ADMIN_EXTENSIONS = "creepersiarena.command.admin.extensions",
            ADMIN_CONFIG = "creepersiarena.command.admin.config";

    private CiaPermissions() {
    }

    public static void registerAll(Plugin plugin) {
        ensure(CHOOSEJOB, PermissionDefault.TRUE);

        var pCommandRoot = ensure(COMMAND, PermissionDefault.TRUE);
        addChild(pCommandRoot, ensure(COMMAND_JOIN, PermissionDefault.TRUE));
        addChild(pCommandRoot, ensure(COMMAND_LEAVE, PermissionDefault.TRUE));
        addChild(pCommandRoot, ensure(COMMAND_JOB, PermissionDefault.TRUE));
        addChild(pCommandRoot, ensure(COMMAND_TEAM, PermissionDefault.TRUE));
        addChild(pCommandRoot, ensure(COMMAND_LANGUAGE, PermissionDefault.TRUE));
        addChild(pCommandRoot, ensure(COMMAND_PREFERENCE, PermissionDefault.TRUE));
        addChild(pCommandRoot, ensure(COMMAND_BALANCE, PermissionDefault.TRUE));
        addChild(pCommandRoot, ensure(COMMAND_STORE, PermissionDefault.TRUE));
        addChild(pCommandRoot, ensure(COMMAND_PARTICLES, PermissionDefault.TRUE));

        var pAdminRoot = ensure(ADMIN, PermissionDefault.OP);
        addChild(pAdminRoot, ensure(ADMIN_GAME, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_MODE, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_ARENA, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_SKIP, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_COOLDOWN, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_REGENERATION, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_MUTATION, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_ABILITY, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_DATABASE, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_ECONOMY, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_STORE, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_ENTRANCE, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_LANGUAGE, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_RELOAD, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_EXTENSIONS, PermissionDefault.OP));
        addChild(pAdminRoot, ensure(ADMIN_CONFIG, PermissionDefault.OP));

        plugin.getLogger().info("Registered permissions (programmatic): choosejob, command tree, admin tree.");
    }

    private static Permission ensure(String node, PermissionDefault def) {
        var existing = Bukkit.getPluginManager().getPermission(node);
        if (existing != null) {
            existing.setDefault(def);
            return existing;
        }
        var created = new Permission(node, def);
        Bukkit.getPluginManager().addPermission(created);
        return created;
    }

    private static void addChild(Permission parent, Permission child) {
        var children = parent.getChildren();
        children.put(child.getName(), true);
        parent.recalculatePermissibles();
    }

}
