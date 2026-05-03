package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModePlayerFlow;
import top.ourisland.creepersiarena.api.game.mode.context.ModeLobbyContext;
import top.ourisland.creepersiarena.api.game.mode.context.ModePlayerContext;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.defaultcontent.DefaultLoadoutService;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemService;
import top.ourisland.creepersiarena.game.mode.impl.steal.model.StealTeam;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.SkillTickTask;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry;
import top.ourisland.creepersiarena.job.skill.ui.SkillHotbarRenderer;
import top.ourisland.creepersiarena.utils.Msg;

import java.util.List;

/**
 * Player flow for the bundled steal mode.
 */
final class StealPlayerFlow implements IModePlayerFlow {

    private final DefaultLoadoutService kit;
    private final StealLobbyUi lobbyUi;
    private final ConfigManager configManager;
    private final StealState state;

    StealPlayerFlow(GameRuntime runtime, StealState state, StealLobbyUi lobbyUi) {
        this.kit = new DefaultLoadoutService(
                runtime.requireService(JobManager.class),
                runtime.requireService(SkillRegistry.class),
                runtime.requireService(SkillHotbarRenderer.class),
                runtime.requireService(SkillTickTask.class)::nowTick
        );
        this.lobbyUi = lobbyUi;
        this.configManager = runtime.requireService(ConfigManager.class);
        this.state = state;
    }

    @Override
    public Location spawnLocation(ModePlayerContext ctx) {
        ArenaInstance arena = ctx.game().arena();
        StealTeam team = state.team(ctx.player().getUniqueId());
        String teamKey = team == null ? ctx.session().selectedTeamKey() : team.key();
        if (teamKey != null) {
            List<Location> group = arena.spawnGroup(teamKey);
            if (!group.isEmpty()) {
                int index = Math.floorMod(ctx.player().getUniqueId().hashCode(), group.size());
                return group.get(index).clone();
            }
        }
        return arena.firstSpawnOrAnchor("default");
    }

    @Override
    public boolean allowHubEntrance(ModeLobbyContext ctx) {
        return false;
    }

    @Override
    public boolean acceptsLobbyUiInput(ModeLobbyContext ctx) {
        return ctx != null && lobbyUi.acceptsInput(ctx.session());
    }

    @Override
    public boolean showJobSelector(ModeLobbyContext ctx) {
        if (ctx == null || ctx.session() == null) return false;
        return switch (state.phase) {
            case CHOOSE_JOB -> StealPlayerState.participant(ctx.session())
                    && ctx.session().state() == PlayerState.IN_GAME;
            case LOBBY, START_COUNTDOWN -> false;
            case SPECTATOR_TOUR, ROUND_PLAYING, ROUND_CELEBRATION, GAME_END_CELEBRATION ->
                    state.modeConfig().allowRespawnJobSelection()
                            && ctx.session().state() == PlayerState.RESPAWN;
        };
    }

    @Override
    public int selectableTeamCount(ModeLobbyContext ctx) {
        if (state.phase == StealPhase.LOBBY || state.phase == StealPhase.START_COUNTDOWN) return 2;
        return 0;
    }

    @Override
    public void decorateLobbyInventory(ModeLobbyContext ctx, org.bukkit.inventory.PlayerInventory inventory) {
        lobbyUi.decorate(ctx, inventory);
    }

    @Override
    public boolean allowJobSelection(ModeLobbyContext ctx) {
        return showJobSelector(ctx);
    }

    @Override
    public void onEnterGame(ModePlayerContext ctx) {
        var player = ctx.player();

        if (state.phase == StealPhase.CHOOSE_JOB) {
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            runtimeApplyJobSelectionKit(player, ctx);
            Msg.actionBar(player, Component.text("请选择本轮职业", NamedTextColor.AQUA));
            return;
        }

        if (state.phase != StealPhase.ROUND_PLAYING) {
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            Msg.actionBar(player, Component.text("等待偷窃模式阶段切换", NamedTextColor.GRAY));
            return;
        }

        player.setGameMode(GameMode.ADVENTURE);
        kit.apply(player, ctx.session());

        StealTeam team = state.team(player.getUniqueId());
        if (team == StealTeam.BLUE) {
            player.getInventory().setItem(6, bluePickaxe(state.modeConfig().mineCooldownSeconds()));
        }

        if (team == null) {
            Msg.actionBar(player, Component.text("进入偷窃回合", NamedTextColor.GREEN));
            return;
        }

        Msg.actionBar(player, Component.text("你是", NamedTextColor.WHITE)
                .append(Component.text(team.displayNameZh(), team.color())));
    }

    private void runtimeApplyJobSelectionKit(Player player, ModePlayerContext ctx) {
        var lobbyItems = ctx.runtime().requireService(LobbyItemService.class);
        lobbyItems.applyJobSelectionKit(player, ctx.session(), configManager.globalConfig());
    }

    private ItemStack bluePickaxe(int cooldownSeconds) {
        ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta meta = pickaxe.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("铁镐", NamedTextColor.WHITE));
            meta.lore(List.of(
                    Component.text("✎ 用于拆除红石矿", NamedTextColor.GRAY),
                    Component.text("❃ 左键使用", NamedTextColor.GRAY),
                    Component.text("❃ " + cooldownSeconds + " 秒冷却", NamedTextColor.GRAY)
            ));
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            pickaxe.setItemMeta(meta);
        }
        return pickaxe;
    }

}
