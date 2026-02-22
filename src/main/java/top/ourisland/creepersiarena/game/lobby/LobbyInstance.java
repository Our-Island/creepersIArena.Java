package top.ourisland.creepersiarena.game.lobby;

import org.bukkit.Location;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.core.region.Region2D;

public record LobbyInstance(
        String id,
        Location anchor,
        Region2D region,
        @Nullable EntryZone entryZone
        ) {
}
