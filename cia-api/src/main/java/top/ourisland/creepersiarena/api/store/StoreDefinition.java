package top.ourisland.creepersiarena.api.store;

import net.kyori.adventure.text.Component;

public record StoreDefinition(
        StoreId id,
        Component title,
        int rows
) {

    public StoreDefinition {
        if (id == null) throw new IllegalArgumentException("store id is null");
        title = title == null ? Component.text(id.asString()) : title;
        rows = Math.clamp(rows, 1, 6);
    }

}
