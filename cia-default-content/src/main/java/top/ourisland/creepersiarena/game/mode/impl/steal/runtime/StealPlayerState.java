package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.mode.impl.steal.model.StealTeam;

final class StealPlayerState {

    private static final String PREFIX = "cia.steal.";
    private static final String READY = PREFIX + "ready";
    private static final String PARTICIPANT = PREFIX + "participant";
    private static final String ALIVE = PREFIX + "alive";
    private static final String TEAM = PREFIX + "team";

    private StealPlayerState() {
    }

    static boolean ready(PlayerSession session) {
        return session != null && session.modeBoolean(READY, false);
    }

    static void ready(PlayerSession session, boolean value) {
        if (session != null) session.setModeBoolean(READY, value);
    }

    static boolean participant(PlayerSession session) {
        return session != null && session.modeBoolean(PARTICIPANT, false);
    }

    static void participant(PlayerSession session, boolean value) {
        if (session != null) session.setModeBoolean(PARTICIPANT, value);
    }

    static boolean alive(PlayerSession session) {
        return session != null && session.modeBoolean(ALIVE, true);
    }

    static void alive(PlayerSession session, boolean value) {
        if (session != null) session.setModeBoolean(ALIVE, value);
    }

    static StealTeam team(PlayerSession session) {
        if (session == null) return null;

        Object stored = session.modeData(TEAM);
        if (stored instanceof String value) {
            StealTeam team = StealTeam.fromKey(value);
            if (team != null) return team;
        }

        var keyed = StealTeam.fromKey(session.selectedTeamKey());
        return keyed == null ? StealTeam.fromNumericId(session.selectedTeam()) : keyed;
    }

    static void team(PlayerSession session, StealTeam team) {
        if (session == null) return;
        if (team == null) {
            session.modeData(TEAM, null);
            session.selectedTeam(null);
            session.selectedTeamKey(null);
            return;
        }

        session.modeData(TEAM, team.key());
        session.selectedTeam(team.numericId());
        session.selectedTeamKey(team.key());
    }

    static void clear(PlayerSession session) {
        if (session == null) return;
        session.clearModeData(PREFIX);
        session.selectedTeam(null);
        session.selectedTeamKey(null);
    }

}
