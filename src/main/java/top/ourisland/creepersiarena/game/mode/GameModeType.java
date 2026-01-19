package top.ourisland.creepersiarena.game.mode;

public enum GameModeType {
    BATTLE,
    STEAL;

    public boolean isBattle() {
        return this == BATTLE;
    }

    public boolean isSteal() {
        return this == STEAL;
    }
}

