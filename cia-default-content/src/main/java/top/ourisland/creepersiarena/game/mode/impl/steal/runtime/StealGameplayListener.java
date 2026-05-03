package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.projectiles.ProjectileSource;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.mode.impl.steal.model.StealTeam;

public final class StealGameplayListener implements Listener {

    private final GameManager gameManager;
    private final PlayerSessionStore sessions;

    public StealGameplayListener(GameManager gameManager, PlayerSessionStore sessions) {
        this.gameManager = gameManager;
        this.sessions = sessions;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReadyInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() == Action.PHYSICAL) return;

        var readyActive = activeSteal();
        if (readyActive != null && readyActive.timeline().isReadyButton(event.getItem())) {
            event.setCancelled(true);
            readyActive.timeline().toggleReady(event.getPlayer());
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            var active = activeSteal();
            if (active == null || active.state().phase != StealPhase.ROUND_PLAYING) return;
            Block block = event.getClickedBlock();
            if (block == null || !isRedstoneOre(block.getType()) || !active.state()
                    .arenaConfig()
                    .isRedstoneTarget(block)) {
                return;
            }

            event.setCancelled(true);
            tryMineRedstone(event.getPlayer(), block, active);
        }
    }

    private ActiveSteal activeSteal() {
        GameSession session = gameManager.active();
        if (session == null || !session.mode().equals(GameModeType.of("steal"))) return null;
        if (!(gameManager.timeline() instanceof StealTimeline timeline)) return null;
        return new ActiveSteal(session, timeline, timeline.state());
    }

    private boolean isRedstoneOre(Material material) {
        return material == Material.DEEPSLATE_REDSTONE_ORE || material == Material.REDSTONE_ORE;
    }

    private boolean tryMineRedstone(Player player, Block block, ActiveSteal active) {
        if (player == null || block == null || active == null) return false;
        boolean accepted = active.timeline().onMinedRedstone(player, active.state().modeConfig());
        if (!accepted) return false;

        block.setType(Material.AIR, false);
        player.playSound(player, Sound.BLOCK_DEEPSLATE_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReadyClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        var active = activeSteal();
        if (active == null || !active.timeline().isReadyButton(event.getCurrentItem())) return;

        event.setCancelled(true);
        active.timeline().toggleReady(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReadyDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        var active = activeSteal();
        if (active == null) return;
        PlayerSession session = sessions.get(player);
        if (session == null || session.state() != PlayerState.HUB) return;
        if (event.getRawSlots().contains(0)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReadySwap(PlayerSwapHandItemsEvent event) {
        var active = activeSteal();
        if (active == null) return;
        if (!active.timeline().isReadyButton(event.getMainHandItem())
                && !active.timeline().isReadyButton(event.getOffHandItem())) {
            return;
        }

        event.setCancelled(true);
        active.timeline().toggleReady(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        var active = activeSteal();
        if (active == null) return;

        PlayerSession session = sessions.get(player);
        if (session == null || session.state() != PlayerState.IN_GAME) return;

        if (active.state().phase != StealPhase.ROUND_PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;

        var attacker = attackingPlayer(event);
        if (attacker == null) return;

        var active = activeSteal();
        if (active == null) return;

        PlayerSession targetSession = sessions.get(target);
        PlayerSession attackerSession = sessions.get(attacker);
        if ((targetSession == null || targetSession.state() != PlayerState.IN_GAME)
                && (attackerSession == null || attackerSession.state() != PlayerState.IN_GAME)) {
            return;
        }

        if (active.state().phase != StealPhase.ROUND_PLAYING) {
            event.setCancelled(true);
            return;
        }

        StealTeam sourceTeam = active.state().team(attacker.getUniqueId());
        StealTeam targetTeam = active.state().team(target.getUniqueId());
        if (sourceTeam == null || targetTeam == null || sourceTeam == targetTeam) {
            event.setCancelled(true);
        }
    }

    private Player attackingPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) return player;
        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) return player;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        var active = activeSteal();
        if (active == null) return;

        Player player = event.getPlayer();
        PlayerSession playerSession = sessions.get(player);
        if (playerSession == null || playerSession.state() != PlayerState.IN_GAME) return;

        if (active.state().phase != StealPhase.ROUND_PLAYING) {
            event.setCancelled(true);
            return;
        }

        boolean target = active.state()
                .arenaConfig()
                .isRedstoneTarget(event.getBlock()) && isRedstoneOre(event.getBlock().getType());
        if (!target) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        event.setDropItems(false);
        event.setExpToDrop(0);
        tryMineRedstone(player, event.getBlock(), active);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        var active = activeSteal();
        if (active == null) return;

        PlayerSession playerSession = sessions.get(event.getPlayer());
        if (playerSession == null || playerSession.state() != PlayerState.IN_GAME) return;

        if (active.state().phase != StealPhase.LOBBY && active.state().phase != StealPhase.START_COUNTDOWN) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        var active = activeSteal();
        if (active == null) return;

        PlayerSession playerSession = sessions.get(event.getEntity());
        if (playerSession == null || playerSession.state() != PlayerState.IN_GAME) return;

        active.timeline().onPlayerDeath(event.getEntity());
    }

    private record ActiveSteal(
            GameSession session,
            StealTimeline timeline,
            StealState state
    ) {

    }

}
