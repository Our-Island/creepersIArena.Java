package top.ourisland.creepersiarena.defaultcontent.game.mode.steal.model;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import top.ourisland.creepersiarena.api.game.team.TeamId;

public enum StealTeam {

    RED(TeamId.parse("red"), NamedTextColor.RED),
    BLUE(TeamId.parse("blue"), NamedTextColor.BLUE);

    @Getter private final TeamId id;
    @Getter private final NamedTextColor color;

    StealTeam(TeamId id, NamedTextColor color) {
        this.id = id;
        this.color = color;
    }

    public static StealTeam fromId(TeamId id) {
        if (id == null) return null;
        if (RED.id.equals(id)) return RED;
        if (BLUE.id.equals(id)) return BLUE;
        return null;
    }

    public String key() {
        return id.value();
    }

    public String displayNameZh() {
        return this == RED ? "红队" : "蓝队";
    }

    public StealTeam opposite() {
        return this == RED ? BLUE : RED;
    }

}
