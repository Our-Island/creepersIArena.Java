package top.ourisland.creepersiarena.defaultcontent.game.mode.steal.runtime;

import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.identity.ExtensionSessionData;
import top.ourisland.creepersiarena.api.identity.SessionDataKey;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentRuntimeIdentity;
import top.ourisland.creepersiarena.defaultcontent.game.mode.steal.model.StealTeam;

final class StealPlayerState {

    private static final ExtensionSessionData SESSION_DATA = DefaultContentRuntimeIdentity.sessionData();
    private static final SessionDataKey<Boolean>
            READY = SESSION_DATA.key("steal/ready", Boolean.class),
            PARTICIPANT = SESSION_DATA.key("steal/participant", Boolean.class),
            ALIVE = SESSION_DATA.key("steal/alive", Boolean.class);

    private StealPlayerState() {
    }

    static boolean ready(PlayerSession session) {
        return session != null && session.getOrDefault(READY, false);
    }

    static void ready(PlayerSession session, boolean value) {
        if (session != null) session.set(READY, value);
    }

    static boolean participant(PlayerSession session) {
        return session != null && session.getOrDefault(PARTICIPANT, false);
    }

    static void participant(PlayerSession session, boolean value) {
        if (session != null) session.set(PARTICIPANT, value);
    }

    static boolean alive(PlayerSession session) {
        return session != null && session.getOrDefault(ALIVE, true);
    }

    static void alive(PlayerSession session, boolean value) {
        if (session != null) session.set(ALIVE, value);
    }

    static StealTeam team(PlayerSession session) {
        return session == null ? null : StealTeam.fromId(session.selectedTeam());
    }

    static void team(PlayerSession session, StealTeam team) {
        if (session != null) session.selectedTeam(team == null ? null : team.id());
    }

    static void clear(PlayerSession session) {
        if (session == null) return;
        session.remove(READY);
        session.remove(PARTICIPANT);
        session.remove(ALIVE);
        session.selectedTeam(null);
    }

}
