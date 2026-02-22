package top.ourisland.creepersiarena.permission;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public final class CiaPermissions {

    // ====== base ======
    public static final String CHOOSEJOB = "creepersiarena.choosejob";
    // ====== command root ======
    public static final String COMMAND = "creepersiarena.command";
    public static final String COMMAND_JOIN = "creepersiarena.command.join";
    public static final String COMMAND_LEAVE = "creepersiarena.command.leave";
    public static final String COMMAND_JOB = "creepersiarena.command.job";
    public static final String COMMAND_TEAM = "creepersiarena.command.team";
    public static final String COMMAND_LANGUAGE = "creepersiarena.command.language";
    public static final String COMMAND_PREFERENCE = "creepersiarena.command.preference";
    // ====== admin root ======
    public static final String ADMIN = "creepersiarena.command.admin";
    public static final String ADMIN_MODE = "creepersiarena.command.admin.mode";
    public static final String ADMIN_ARENA = "creepersiarena.command.admin.arena";
    public static final String ADMIN_SKIP = "creepersiarena.command.admin.skip";
    public static final String ADMIN_COOLDOWN = "creepersiarena.command.admin.cooldown";
    public static final String ADMIN_REGENERATION = "creepersiarena.command.admin.regeneration";
    public static final String ADMIN_MUTATION = "creepersiarena.command.admin.mutation";
    public static final String ADMIN_ENTRANCE = "creepersiarena.command.admin.entrance";
    public static final String ADMIN_LANGUAGE = "creepersiarena.command.admin.language";
    public static final String ADMIN_RELOAD = "creepersiarena.command.admin.reload";
    public static final String ADMIN_CONFIG = "creepersiarena.command.admin.config";

    private CiaPermissions() {
    }

    public static void registerAll(Plugin plugin) {
        Permission pChooseJob = ensure(CHOOSEJOB, PermissionDefault.TRUE);

        Permission pCommandRoot = ensure(COMMAND, PermissionDefault.TRUE);
        Permission pJoin = ensure(COMMAND_JOIN, PermissionDefault.TRUE);
        Permission pLeave = ensure(COMMAND_LEAVE, PermissionDefault.TRUE);
        Permission pJob = ensure(COMMAND_JOB, PermissionDefault.TRUE);
        Permission pTeam = ensure(COMMAND_TEAM, PermissionDefault.TRUE);
        Permission pLanguage = ensure(COMMAND_LANGUAGE, PermissionDefault.TRUE);
        Permission pPreference = ensure(COMMAND_PREFERENCE, PermissionDefault.TRUE);

        Permission pAdminRoot = ensure(ADMIN, PermissionDefault.OP);
        Permission pAdminMode = ensure(ADMIN_MODE, PermissionDefault.OP);
        Permission pAdminArena = ensure(ADMIN_ARENA, PermissionDefault.OP);
        Permission pAdminSkip = ensure(ADMIN_SKIP, PermissionDefault.OP);
        Permission pAdminCooldown = ensure(ADMIN_COOLDOWN, PermissionDefault.OP);
        Permission pAdminRegeneration = ensure(ADMIN_REGENERATION, PermissionDefault.OP);
        Permission pAdminMutation = ensure(ADMIN_MUTATION, PermissionDefault.OP);
        Permission pAdminEntrance = ensure(ADMIN_ENTRANCE, PermissionDefault.OP);
        Permission pAdminLanguage = ensure(ADMIN_LANGUAGE, PermissionDefault.OP);
        Permission pAdminReload = ensure(ADMIN_RELOAD, PermissionDefault.OP);
        Permission pAdminConfig = ensure(ADMIN_CONFIG, PermissionDefault.OP);

        // command root -> sub commands
        addChild(pCommandRoot, pJoin);
        addChild(pCommandRoot, pLeave);
        addChild(pCommandRoot, pJob);
        addChild(pCommandRoot, pTeam);
        addChild(pCommandRoot, pLanguage);
        addChild(pCommandRoot, pPreference);

        // admin root -> admin sub commands
        addChild(pAdminRoot, pAdminMode);
        addChild(pAdminRoot, pAdminArena);
        addChild(pAdminRoot, pAdminSkip);
        addChild(pAdminRoot, pAdminCooldown);
        addChild(pAdminRoot, pAdminRegeneration);
        addChild(pAdminRoot, pAdminMutation);
        addChild(pAdminRoot, pAdminEntrance);
        addChild(pAdminRoot, pAdminLanguage);
        addChild(pAdminRoot, pAdminReload);
        addChild(pAdminRoot, pAdminConfig);

        plugin.getLogger().info("Registered permissions (programmatic): "
                + "choosejob, command tree, admin tree.");
    }

    private static Permission ensure(String node, PermissionDefault def) {
        Permission existing = Bukkit.getPluginManager().getPermission(node);
        if (existing != null) {
            existing.setDefault(def);
            return existing;
        }
        Permission created = new Permission(node, def);
        Bukkit.getPluginManager().addPermission(created);
        return created;
    }

    private static void addChild(Permission parent, Permission child) {
        Map<String, Boolean> children = parent.getChildren();
        children.put(child.getName(), true);
        parent.getChildren().put(child.getName(), true);
        parent.recalculatePermissibles();
    }
}
