package top.ourisland.creepersiarena.job.listener;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;
import top.ourisland.creepersiarena.job.skill.event.SkillContext;
import top.ourisland.creepersiarena.job.skill.event.impl.InteractEvent;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRuntime;
import top.ourisland.creepersiarena.job.skill.ui.SkillItemCodec;

import java.util.function.LongSupplier;

public final class SkillUiListener implements Listener {

    private final PlayerSessionStore sessions;
    private final SkillItemCodec codec;
    private final SkillRuntime runtime;
    private final LongSupplier nowTick;

    public SkillUiListener(
            @NonNull PlayerSessionStore sessions,
            @NonNull SkillItemCodec codec,
            @NonNull SkillRuntime runtime,
            @NonNull LongSupplier nowTick
    ) {
        this.sessions = sessions;
        this.codec = codec;
        this.runtime = runtime;
        this.nowTick = nowTick;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() == Action.PHYSICAL) return;

        Player p = e.getPlayer();
        var s = sessions.get(p);
        if (s == null || s.state() != PlayerState.IN_GAME) return;

        ItemStack item = e.getItem();
        if (item == null) return;

        String skillId = codec.readSkillId(item);
        if (skillId == null) return;

        e.setCancelled(true);

        long tick = nowTick.getAsLong();
        runtime.handle(new SkillContext(
                p,
                new InteractEvent(e.getAction(), true),
                item,
                skillId,
                tick
        ));
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        var s = sessions.get(p);
        if (s == null || s.state() != PlayerState.IN_GAME) return;

        ItemStack cur = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        if ((cur != null && codec.isSkillItem(cur)) || codec.isSkillItem(cursor)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        var s = sessions.get(p);
        if (s == null || s.state() != PlayerState.IN_GAME) return;

        if (codec.isSkillItem(e.getOldCursor())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        var s = sessions.get(e.getPlayer());
        if (s == null || !s.state().isInGame()) return;

        e.setCancelled(true);
    }
}
