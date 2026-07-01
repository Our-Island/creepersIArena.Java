package top.ourisland.creepersiarena.core.command;

import com.mojang.brigadier.tree.CommandNode;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CommandTreeRegistrationTest {

    @Test
    void buildsOnlyTheCurrentPlayerAndAdminRoots() {
        var roots = new CiaCommandRoots(null);

        var player = roots.playerRoot().build();
        var admin = roots.adminRoot(CiaCommandConstants.ADMIN_ROOT_LITERAL).build();

        assertEquals("cia", player.getName());
        assertEquals("ciaa", admin.getName());
        assertEquals(Set.of(
                "help", "join", "leave", "job", "team", "language", "pref", "balance", "store", "particles"
        ), childNames(player));
        assertEquals(Set.of(
                "help", "game", "ability", "database", "economy", "store", "language", "reload", "extension", "config"
        ), childNames(admin));
    }

    private static Set<String> childNames(CommandNode<?> node) {
        assertNotNull(node);
        return node.getChildren().stream()
                .map(CommandNode::getName)
                .collect(Collectors.toSet());
    }

    @Test
    void retainsEveryGroupedCommandPath() {
        var roots = new CiaCommandRoots(null);
        var player = roots.playerRoot().build();
        var admin = roots.adminRoot(CiaCommandConstants.ADMIN_ROOT_LITERAL).build();

        assertChildren(player.getChild("pref"), Set.of("language", "particles", "scoreboard", "reset"));
        assertChildren(player.getChild("particles"), Set.of("off", "select"));
        assertChildren(admin.getChild("game"), Set.of("mode", "arena", "skip", "cooldown", "regen", "mutation", "entrance"));
        assertChildren(admin.getChild("ability"), Set.of("list", "reload", "info", "enable", "disable"));
        assertChildren(admin.getChild("database"), Set.of("status", "ping", "tables"));
        assertChildren(admin.getChild("economy"), Set.of("balance", "give", "take", "set"));
        assertChildren(admin.getChild("extension"), Set.of("list", "info", "dump"));
        assertChildren(admin.getChild("config"), Set.of("get", "list", "set", "reload"));
    }

    private static void assertChildren(CommandNode<?> node, Set<String> expected) {
        assertEquals(expected, childNames(node));
    }

    @Test
    void doesNotReintroduceRemovedCompatibilityPaths() {
        assertTrue(CiaCommandConstants.PLAYER_ROOT_ALIASES.isEmpty());
        assertTrue(CiaCommandConstants.ADMIN_ROOT_ALIASES.isEmpty());

        var roots = new CiaCommandRoots(null);
        var player = roots.playerRoot().build();
        var admin = roots.adminRoot(CiaCommandConstants.ADMIN_ROOT_LITERAL).build();

        assertNull(player.getChild("admin"));
        assertNull(player.getChild("preference"));
        assertNull(admin.getChild("mode"));
        assertNull(admin.getChild("arena"));
        assertNull(admin.getChild("skip"));
        assertNull(admin.getChild("cooldown"));
        assertNull(admin.getChild("regen"));
        assertNull(admin.getChild("mutation"));
        assertNull(admin.getChild("entrance"));
        assertNull(admin.getChild("extensions"));
    }

}
