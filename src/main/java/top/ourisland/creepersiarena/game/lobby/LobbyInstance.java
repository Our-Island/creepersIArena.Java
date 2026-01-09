package top.ourisland.creepersiarena.game.lobby;

import org.bukkit.Location;
import top.ourisland.creepersiarena.game.region.Region2D;

public record LobbyInstance(String id, Location anchor, Region2D region) {
}
