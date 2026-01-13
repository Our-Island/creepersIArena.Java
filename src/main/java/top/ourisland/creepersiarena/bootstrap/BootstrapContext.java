package top.ourisland.creepersiarena.bootstrap;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.flow.PlayerTransitions;
import top.ourisland.creepersiarena.game.lobby.LobbyManager;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.inventory.LobbyItemService;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemCodec;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemFactory;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.RespawnService;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.SkillContextFactory;
import top.ourisland.creepersiarena.job.skill.SkillEngine;
import top.ourisland.creepersiarena.job.skill.SkillItemCodec;
import top.ourisland.creepersiarena.job.skill.SkillItemFactory;

public record BootstrapContext(
        JavaPlugin plugin,
        World world,

        // Core
        ConfigManager configManager,

        // Arena
        ArenaManager arenaManager,

        // Lobby
        LobbyManager lobbyManager,
        LobbyService lobbyService,
        LobbyItemCodec lobbyItemCodec,
        LobbyItemFactory lobbyItemFactory,
        LobbyItemService lobbyItemService,

        // Player runtime
        PlayerSessionStore sessionStore,
        PlayerTransitions transitions,
        RespawnService respawns,

        // Game
        GameFlow flow,
        GameRuntime runtime,
        GameManager gameManager,

        // Jobs/skills
        JobManager jobManager,
        SkillItemCodec skillItemCodec,
        SkillItemFactory skillItemFactory,
        SkillContextFactory skillContextFactory,
        SkillEngine skillEngine,

        // Tasks
        BukkitTask skillTickTask,
        BukkitTask gameTickTask
) {
}
