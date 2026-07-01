package top.ourisland.creepersiarena.core.command.permission;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandPermissionTest {

    @Test
    void declaresEveryPlayerCommandPermissionUnderThePublicRoot() {
        assertEquals(List.of(
                CiaPermissions.COMMAND_JOIN,
                CiaPermissions.COMMAND_LEAVE,
                CiaPermissions.COMMAND_JOB,
                CiaPermissions.COMMAND_TEAM,
                CiaPermissions.COMMAND_LANGUAGE,
                CiaPermissions.COMMAND_PREFERENCE,
                CiaPermissions.COMMAND_BALANCE,
                CiaPermissions.COMMAND_STORE,
                CiaPermissions.COMMAND_PARTICLES
        ), CiaPermissions.playerChildNodes());
    }

    @Test
    void declaresEveryAdminCommandPermissionUnderTheAdminRoot() {
        assertEquals(List.of(
                CiaPermissions.ADMIN_GAME,
                CiaPermissions.ADMIN_MODE,
                CiaPermissions.ADMIN_ARENA,
                CiaPermissions.ADMIN_SKIP,
                CiaPermissions.ADMIN_COOLDOWN,
                CiaPermissions.ADMIN_REGENERATION,
                CiaPermissions.ADMIN_MUTATION,
                CiaPermissions.ADMIN_ABILITY,
                CiaPermissions.ADMIN_DATABASE,
                CiaPermissions.ADMIN_ECONOMY,
                CiaPermissions.ADMIN_STORE,
                CiaPermissions.ADMIN_ENTRANCE,
                CiaPermissions.ADMIN_LANGUAGE,
                CiaPermissions.ADMIN_RELOAD,
                CiaPermissions.ADMIN_EXTENSION,
                CiaPermissions.ADMIN_CONFIG
        ), CiaPermissions.adminChildNodes());
    }

    @Test
    void hasOneUniqueCanonicalDeclarationForEveryPermissionNode() {
        var nodes = CiaPermissions.allNodes();

        assertEquals(nodes.size(), new HashSet<>(nodes).size());
        assertTrue(nodes.contains(CiaPermissions.COMMAND));
        assertTrue(nodes.contains(CiaPermissions.ADMIN));
        assertTrue(nodes.stream().allMatch(node -> node.startsWith("creepersiarena.command")));
        assertFalse(nodes.contains("creepersiarena.choosejob"));
        assertFalse(nodes.contains("creepersiarena.command.admin.extensions"));
    }

}
