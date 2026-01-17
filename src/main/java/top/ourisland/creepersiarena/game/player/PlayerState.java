package top.ourisland.creepersiarena.game.player;

public enum PlayerState {
    HUB,
    IN_GAME,
    RESPAWN,
    SPECTATE;

    public boolean isInGame() {
        return this == IN_GAME;
    }

    public boolean isSpectate() {
        return this == SPECTATE;
    }

    public boolean isLobbyState() {
        return isHub() || isRespawn();
    }

    public boolean isHub() {
        return this == HUB;
    }

    public boolean isRespawn() {
        return this == RESPAWN;
    }
}

