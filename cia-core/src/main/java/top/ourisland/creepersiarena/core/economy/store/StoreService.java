package top.ourisland.creepersiarena.core.economy.store;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.economy.store.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StoreService implements IStoreService {

    private final StoreRegistry registry;
    private final StoreItemCodec codec;
    private final IAbilityGate abilities;
    private final Map<UUID, StoreId> openStores = new ConcurrentHashMap<>();

    public StoreService(
            StoreRegistry registry,
            StoreItemCodec codec,
            IAbilityGate abilities
    ) {
        this.registry = registry;
        this.codec = codec;
        this.abilities = abilities;
    }

    public void clearOpen(Player player) {
        if (player != null) openStores.remove(player.getUniqueId());
    }

    public StoreItemCodec codec() {
        return codec;
    }

    @Override
    public boolean openStore(
            Player player,
            StoreId storeId
    ) {
        return openStore(player, storeId, null);
    }

    private boolean openStore(
            Player player,
            StoreId storeId,
            StoreItemId focusedItemId
    ) {
        if (player == null || storeId == null) return false;
        if (!abilities.isEnabled(CoreAbilities.STORE_UI, player, "store_open")) {
            player.sendMessage(Component.text("Store UI is disabled."));
            return false;
        }

        var store = registry.store(storeId);
        if (store == null) {
            player.sendMessage(Component.text("Unknown store: " + storeId.asString()));
            return false;
        }

        var holder = new StoreInventoryHolder(storeId, 0);
        var inventory = Bukkit.createInventory(holder, store.rows() * 9, store.title());
        holder.inventory(inventory);

        var context = new StoreRenderContext(player, store, 0);
        var visibleItems = new java.util.ArrayList<>(registry.items(storeId).stream()
                .filter(item -> item.state(context) != StoreItemState.HIDDEN)
                .toList());

        if (focusedItemId != null) {
            visibleItems.sort((left, right) -> {
                boolean leftFocused = focusedItemId.equals(left.id());
                boolean rightFocused = focusedItemId.equals(right.id());
                if (leftFocused == rightFocused) return 0;
                return leftFocused ? -1 : 1;
            });
        }

        int max = Math.min(inventory.getSize(), visibleItems.size());
        for (int i = 0; i < max; i++) {
            var item = visibleItems.get(i);
            ItemStack icon = item.icon(context);
            if (icon == null || icon.getType().isAir()) icon = new ItemStack(Material.BARRIER);
            codec.mark(icon, storeId, item.id());
            inventory.setItem(i, icon);
        }

        openStores.put(player.getUniqueId(), storeId);
        player.openInventory(inventory);
        return true;
    }


    @Override
    public boolean openItem(
            Player player,
            StoreId storeId,
            StoreItemId itemId
    ) {
        if (itemId == null) return openStore(player, storeId);
        if (registry.item(storeId, itemId) == null && player != null) {
            player.sendMessage(Component.text("Unknown store item: " + itemId.asString()));
            return false;
        }
        return openStore(player, storeId, itemId);
    }

    @Override
    public void refresh(Player player) {
        if (player == null) return;
        var storeId = openStores.get(player.getUniqueId());
        if (storeId == null) return;
        openStore(player, storeId);
    }


}
