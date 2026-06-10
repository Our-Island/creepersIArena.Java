package top.ourisland.creepersiarena.core.game.lobby;

import org.bukkit.Location;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.region.Region2D;

public record LobbyInstance(
        String id,
        Location anchor,
        Region2D region,
        @Nullable EntryZone entryZone
) {

}
