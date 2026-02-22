package top.ourisland.creepersiarena.game.lobby.inventory;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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

    @SuppressWarnings("unchecked")
    public static InventorySnapshot decode(byte[] data) {
        if (data == null || data.length == 0) return null;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             BukkitObjectInputStream in = new BukkitObjectInputStream(bis)) {

            ItemStack[] contents = (ItemStack[]) in.readObject();
            ItemStack[] armor = (ItemStack[]) in.readObject();
            ItemStack offHand = (ItemStack) in.readObject();
            int level = in.readInt();
            float exp = in.readFloat();
            String gm = in.readUTF();
            GameMode gameMode = (gm == null || gm.isBlank()) ? null : GameMode.valueOf(gm);

            if (contents == null) contents = new ItemStack[0];
            if (armor == null) armor = new ItemStack[0];

            return new InventorySnapshot(contents, armor, offHand, level, exp, gameMode);
        } catch (Throwable t) {
            return null;
        }
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

    public byte[] encode() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             BukkitObjectOutputStream out = new BukkitObjectOutputStream(bos)) {

            out.writeObject(contents);
            out.writeObject(armor);
            out.writeObject(offHand);
            out.writeInt(level);
            out.writeFloat(exp);
            out.writeUTF(gameMode == null ? "" : gameMode.name());
            out.flush();
            return bos.toByteArray();
        } catch (Throwable t) {
            return null;
        }
    }
}
