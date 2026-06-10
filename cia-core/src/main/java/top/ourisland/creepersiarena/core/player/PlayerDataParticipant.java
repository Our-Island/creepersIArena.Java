package top.ourisland.creepersiarena.core.player;

import java.util.UUID;

public interface PlayerDataParticipant {

    default void load(UUID playerId) throws Exception {
    }

    default void unload(UUID playerId) throws Exception {
    }

    default void flushAll() throws Exception {
    }

}
