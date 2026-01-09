package top.ourisland.creepersiarena.game.lobby.inventory;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class InventorySnapshot {
    private final ItemStack[] contents;
    private final ItemStack[] armor;
    private final ItemStack offHand;
    private final int level;
    private final float exp;
    private final GameMode gameMode;

    private InventorySnapshot(ItemStack[] contents, ItemStack[] armor, ItemStack offHand, int level, float exp, GameMode gameMode) {
        this.contents = contents;
        this.armor = armor;
        this.offHand = offHand;
        this.level = level;
        this.exp = exp;
        this.gameMode = gameMode;
    }

    public static InventorySnapshot capture(Player p) {
        var inv = p.getInventory();
        return new InventorySnapshot(
                inv.getContents().clone(),
                inv.getArmorContents().clone(),
                inv.getItemInOffHand(),
                p.getLevel(),
                p.getExp(),
                p.getGameMode()
        );
    }

    public void restore(Player p) {
        var inv = p.getInventory();
        inv.clear();
        inv.setContents(contents.clone());
        inv.setArmorContents(armor.clone());
        inv.setItemInOffHand(offHand);
        p.setLevel(level);
        p.setExp(exp);
        p.setGameMode(gameMode);
    }
}
