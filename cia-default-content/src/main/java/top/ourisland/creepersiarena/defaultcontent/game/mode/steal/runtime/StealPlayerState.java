package top.ourisland.creepersiarena.defaultcontent.game.mode.steal.runtime;

import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.SessionDataKey;
import top.ourisland.creepersiarena.defaultcontent.game.mode.steal.model.StealTeam;

final class StealPlayerState {

    private static final CiaNamespace NAMESPACE = new CiaNamespace("cia");
    private static final SessionDataKey<Boolean> READY = SessionDataKey.of(CiaKey.of(NAMESPACE, "steal/ready"), Boolean.class);
    private static final SessionDataKey<Boolean> PARTICIPANT = SessionDataKey.of(CiaKey.of(NAMESPACE, "steal/participant"), Boolean.class);
    private static final SessionDataKey<Boolean> ALIVE = SessionDataKey.of(CiaKey.of(NAMESPACE, "steal/alive"), Boolean.class);
    private static final SessionDataKey<String> TEAM = SessionDataKey.of(CiaKey.of(NAMESPACE, "steal/team"), String.class);

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
        if (session == null) return null;
        var stored = session.get(TEAM);
        if (stored != null) {
            StealTeam team = StealTeam.fromKey(stored);
            if (team != null) return team;
        }
        var keyed = StealTeam.fromKey(session.selectedTeamKey());
        return keyed == null ? StealTeam.fromNumericId(session.selectedTeam()) : keyed;
    }

    static void team(PlayerSession session, StealTeam team) {
        if (session == null) return;
        if (team == null) {
            session.remove(TEAM);
            session.selectedTeam(null);
            session.selectedTeamKey(null);
            return;
        }
        session.set(TEAM, team.key());
        session.selectedTeam(team.numericId());
        session.selectedTeamKey(team.key());
    }

    static void clear(PlayerSession session) {
        if (session == null) return;
        session.clearNamespace(NAMESPACE);
        session.selectedTeam(null);
        session.selectedTeamKey(null);
    }

}
