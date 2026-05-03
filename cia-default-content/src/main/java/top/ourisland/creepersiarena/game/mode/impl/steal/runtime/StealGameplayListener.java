package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import top.ourisland.creepersiarena.utils.Msg;

public final class StealGameplayListener implements Listener {

    private final GameManager gameManager;
    private final PlayerSessionStore sessions;
    private final StealReadyItemCodec readyItems = new StealReadyItemCodec();

    public StealGameplayListener(GameManager gameManager, PlayerSessionStore sessions) {
        this.gameManager = gameManager;
        this.sessions = sessions;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReadyInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() == Action.PHYSICAL) return;

        if (readyItems.isReadyButton(event.getItem())) {
            var active = activeSteal();
            if (active == null) return;

            event.setCancelled(true);
            toggleReady(event.getPlayer(), active);
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

    private void toggleReady(Player player, ActiveSteal active) {
        if (player == null || active == null) return;
        if (!isWaitingHub(player, active)) {
            Msg.actionBar(player, Component.text("当前阶段不能切换准备", NamedTextColor.RED));
            return;
        }

        PlayerSession session = sessions.getOrCreate(player);
        boolean next = !StealPlayerState.ready(session);
        StealPlayerState.ready(session, next);
        StealPlayerState.participant(session, false);
        StealPlayerState.alive(session, false);
        active.session().addPlayer(player.getUniqueId());

        int ready = active.timeline().countReadyOnline();
        int required = active.state().modeConfig().requiredReadyPlayers(org.bukkit.Bukkit.getOnlinePlayers().size());
        player.getInventory().setItem(0, readyItems.readyButton(next, ready, required));

        player.playSound(player, next
                ? Sound.BLOCK_NOTE_BLOCK_PLING
                : Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.PLAYERS, 1.0f, next ? 2.0f : 1.0f);
        Msg.actionBar(player, next
                ? Component.text("✪ 你已准备加入偷窃模式", NamedTextColor.GREEN)
                : Component.text("✪ 你取消了准备", NamedTextColor.GRAY));
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

    private boolean isWaitingHub(Player player, ActiveSteal active) {
        if (player == null || active == null) return false;
        if (active.state().phase != StealPhase.LOBBY && active.state().phase != StealPhase.START_COUNTDOWN)
            return false;
        PlayerSession session = sessions.get(player);
        return session != null && session.state() == PlayerState.HUB;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReadyClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!readyItems.isReadyButton(event.getCurrentItem())) return;

        var active = activeSteal();
        if (active == null) return;

        event.setCancelled(true);
        toggleReady(player, active);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReadyDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        var active = activeSteal();
        if (active == null || !isWaitingHub(player, active)) return;
        if (event.getRawSlots().contains(0)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReadySwap(PlayerSwapHandItemsEvent event) {
        if (!readyItems.isReadyButton(event.getMainHandItem()) && !readyItems.isReadyButton(event.getOffHandItem()))
            return;

        var active = activeSteal();
        if (active == null) return;

        event.setCancelled(true);
        toggleReady(event.getPlayer(), active);
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
