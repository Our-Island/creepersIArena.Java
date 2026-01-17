package top.ourisland.creepersiarena;

import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.bootstrap.PluginBootstrap;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.flow.PlayerTransitions;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRuntime;

public final class CreepersIArena extends JavaPlugin {

    public static Logger logger;
    private PluginBootstrap bootstrap;

    public static @NonNull Logger getLog() {
        return logger;
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.disable();
            bootstrap = null;
        }
    }

    @Override
    public void onEnable() {
        logger = getSLF4JLogger();
        this.bootstrap = new PluginBootstrap();
        this.bootstrap.enable(this);
    }

    public ConfigManager config() {
        return bootstrap.ctx().configManager();
    }

    public ArenaManager arenas() {
        return bootstrap.ctx().arenaManager();
    }

    public LobbyService lobbies() {
        return bootstrap.ctx().lobbyService();
    }

    public GameManager games() {
        return bootstrap.ctx().gameManager();
    }

    public PlayerSessionStore sessions() {
        return bootstrap.ctx().sessionStore();
    }

    public PlayerTransitions transitions() {
        return bootstrap.ctx().transitions();
    }

    public GameFlow flow() {
        return bootstrap.ctx().flow();
    }

    public JobManager jobs() {
        return bootstrap.ctx().jobManager();
    }

    public SkillRuntime skills() {
        return bootstrap.ctx().skillRuntime();
    }

    public PluginBootstrap bootstrap() {
        return bootstrap;
    }
}
