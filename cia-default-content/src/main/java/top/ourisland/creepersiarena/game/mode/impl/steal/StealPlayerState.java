package top.ourisland.creepersiarena.game.mode.impl.steal;

import top.ourisland.creepersiarena.api.game.player.PlayerSession;

final class StealPlayerState {

    private static final String READY = "cia.steal.ready";
    private static final String PARTICIPANT = "cia.steal.participant";
    private static final String ALIVE = "cia.steal.alive";

    private StealPlayerState() {
    }

    static boolean ready(PlayerSession session) {
        return session != null && session.modeBoolean(READY, false);
    }

    static void ready(PlayerSession session, boolean value) {
        if (session != null) session.modeBoolean(READY, value);
    }

    static boolean participant(PlayerSession session) {
        return session != null && session.modeBoolean(PARTICIPANT, false);
    }

    static void participant(PlayerSession session, boolean value) {
        if (session != null) session.modeBoolean(PARTICIPANT, value);
    }

    static boolean alive(PlayerSession session) {
        return session == null || session.modeBoolean(ALIVE, true);
    }

    static void alive(PlayerSession session, boolean value) {
        if (session != null) session.modeBoolean(ALIVE, value);
    }

}
