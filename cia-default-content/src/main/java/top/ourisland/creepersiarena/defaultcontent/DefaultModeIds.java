package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.game.mode.GameModeId;

public final class DefaultModeIds {

    public static final String BATTLE_VALUE = "cia:battle";
    public static final String STEAL_VALUE = "cia:steal";

    public static final GameModeId BATTLE = GameModeId.of(DefaultContentIds.key("battle"));
    public static final GameModeId STEAL = GameModeId.of(DefaultContentIds.key("steal"));

    private DefaultModeIds() {
    }

}
