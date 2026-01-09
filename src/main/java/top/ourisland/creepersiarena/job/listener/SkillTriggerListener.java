package top.ourisland.creepersiarena.job.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.job.skill.SkillContextFactory;
import top.ourisland.creepersiarena.job.skill.SkillEngine;
import top.ourisland.creepersiarena.job.skill.SkillItemCodec;
import top.ourisland.creepersiarena.job.skill.Trigger;

public final class SkillTriggerListener implements Listener {
    private final SkillItemCodec codec;
    private final SkillEngine engine;
    private final SkillContextFactory ctxFactory;

    public SkillTriggerListener(SkillItemCodec codec, SkillEngine engine, SkillContextFactory ctxFactory) {
        this.codec = codec;
        this.engine = engine;
        this.ctxFactory = ctxFactory;
    }

    // ---------- 右键/左键空气或方块 ----------
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();

        int slot = p.getInventory().getHeldItemSlot();
        if (slot < 0 || slot > 2) return;

        ItemStack item = p.getInventory().getItem(slot);
        if (!codec.isSkillItem(item)) return;

        Trigger trigger = switch (e.getAction()) {
            case RIGHT_CLICK_AIR -> Trigger.RIGHT_CLICK_AIR;
            case RIGHT_CLICK_BLOCK -> Trigger.RIGHT_CLICK_BLOCK;
            case LEFT_CLICK_AIR -> Trigger.LEFT_CLICK_AIR;
            case LEFT_CLICK_BLOCK -> Trigger.LEFT_CLICK_BLOCK;
            default -> null;
        };
        if (trigger == null) return;

        var ctx = ctxFactory.create(
                p, trigger, slot, item, EquipmentSlot.HAND,
                null,
                e.getClickedBlock(),
                e
        );

        boolean ok = engine.dispatch(ctx);

        // 技能物品通常需要取消原交互（避免开门/放置/使用原物品）
        if (ok && (trigger == Trigger.RIGHT_CLICK_AIR || trigger == Trigger.RIGHT_CLICK_BLOCK || trigger == Trigger.LEFT_CLICK_BLOCK)) {
            e.setCancelled(true);
        }
    }

    // ---------- 右键实体 ----------
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();

        int slot = p.getInventory().getHeldItemSlot();
        if (slot < 0 || slot > 2) return;

        ItemStack item = p.getInventory().getItem(slot);
        if (!codec.isSkillItem(item)) return;

        var ctx = ctxFactory.create(
                p, Trigger.RIGHT_CLICK_ENTITY, slot, item, EquipmentSlot.HAND,
                e.getRightClicked(),
                null,
                e
        );

        boolean ok = engine.dispatch(ctx);
        if (ok) e.setCancelled(true);
    }

    // ---------- 消耗品（如果某技能设计成“吃/喝触发”） ----------
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();

        int slot = p.getInventory().getHeldItemSlot();
        if (slot < 0 || slot > 2) return;

        ItemStack item = e.getItem();
        if (!codec.isSkillItem(item)) return;

        var ctx = ctxFactory.create(
                p, Trigger.CONSUME_ITEM, slot, item, EquipmentSlot.HAND,
                null, null,
                e
        );

        boolean ok = engine.dispatch(ctx);
        // 通常这种技能物品不应该被真的吃掉
        if (ok) e.setCancelled(true);
    }

    // ---------- 近战命中 / 被击 ----------
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageByEntityEvent e) {
        // 玩家作为攻击者：HIT_ENTITY
        if (e.getDamager() instanceof Player p) {
            int slot = p.getInventory().getHeldItemSlot();
            if (slot >= 0 && slot <= 2) {
                ItemStack item = p.getInventory().getItem(slot);
                if (codec.isSkillItem(item)) {
                    var ctx = ctxFactory.create(
                            p, Trigger.HIT_ENTITY, slot, item, EquipmentSlot.HAND,
                            e.getEntity(), null,
                            e
                    );
                    engine.dispatch(ctx);
                }
            }
        }

        // 玩家作为受害者：HURT_BY_ENTITY
        if (e.getEntity() instanceof Player victim) {
            int slot = victim.getInventory().getHeldItemSlot();
            if (slot >= 0 && slot <= 2) {
                ItemStack item = victim.getInventory().getItem(slot);
                if (codec.isSkillItem(item)) {
                    Entity attacker = e.getDamager();
                    var ctx = ctxFactory.create(
                            victim, Trigger.HURT_BY_ENTITY, slot, item, EquipmentSlot.HAND,
                            attacker, null,
                            e
                    );
                    engine.dispatch(ctx);
                }
            }
        }
    }

    // ---------- 弓/弩射击 ----------
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onShoot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        int slot = p.getInventory().getHeldItemSlot();
        if (slot < 0 || slot > 2) return;

        ItemStack item = p.getInventory().getItem(slot);
        if (!codec.isSkillItem(item)) return;

        var ctx = ctxFactory.create(
                p, Trigger.SHOOT, slot, item, EquipmentSlot.HAND,
                e.getProjectile(), null,
                e
        );

        boolean ok = engine.dispatch(ctx);
        // 如果你的技能“完全接管射击”，可以取消原射击
        // if (ok) e.setCancelled(true);
    }
}
