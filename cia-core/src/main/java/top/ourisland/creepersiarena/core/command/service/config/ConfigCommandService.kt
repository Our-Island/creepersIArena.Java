package top.ourisland.creepersiarena.core.command.service.config;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.config.ConfigWriteGuard;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;
import top.ourisland.creepersiarena.core.command.message.CommandUsage;
import top.ourisland.creepersiarena.core.command.model.ConfigTarget;
import top.ourisland.creepersiarena.core.config.ConfigManager;
import top.ourisland.creepersiarena.core.game.regeneration.RegenerationService;
import top.ourisland.creepersiarena.core.utils.I18n;

import java.util.List;

public final class ConfigCommandService {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public ConfigCommandService(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
    }

    public void config(
            CommandSender sender,
            ConfigTarget target,
            String node,
            String valueRaw
    ) {
        configSet(sender, target, node, valueRaw, false);
    }

    public void configSet(
            CommandSender sender,
            ConfigTarget target,
            String node,
            String valueRaw,
            boolean create
    ) {
        var cfg = rt.requireService(ConfigManager.class);
        var normalizedNode = ConfigWriteGuard.normalizeNode(node);
        if (normalizedNode == null) {
            messenger.error(sender, "Config node is required.");
            return;
        }

        boolean exists = configNodeExists(cfg, target, normalizedNode);
        boolean section = exists && configSection(cfg, target, normalizedNode);
        try {
            ConfigWriteGuard.validateWrite(normalizedNode, exists, section, create);
        } catch (IllegalArgumentException exception) {
            if (!exists) {
                messenger.warnMini(sender, "Config node does not exist: " + messenger.id(normalizedNode));
                messenger.hint(sender, "Use /ciaa config set " + target.id() + " " + normalizedNode + " --create <value> to create it intentionally.");
            } else if (section) {
                messenger.errorMini(sender, "Refusing to overwrite object config node: " + messenger.id(normalizedNode));
                messenger.hint(sender, "Use /ciaa config list " + target.id() + " to choose a concrete child node.");
            } else {
                messenger.error(sender, exception.getMessage());
            }
            return;
        }

        Object oldValue = exists ? configNode(cfg, target, normalizedNode) : null;
        Object newValue;
        try {
            newValue = ConfigWriteGuard.coerceValue(oldValue, valueRaw);
        } catch (IllegalArgumentException exception) {
            messenger.error(sender, exception.getMessage());
            return;
        }

        boolean ok = switch (target) {
            case CONFIG -> cfg.setGlobalNode(normalizedNode, newValue);
            case ARENA -> cfg.setArenaNode(normalizedNode, newValue);
            case SKILL -> cfg.setSkillNode(normalizedNode, newValue);
        };

        if (!ok) {
            messenger.error(sender, "Write failed.");
            return;
        }

        Object currentValue = configNode(cfg, target, normalizedNode);
        messenger.panel(sender, CommandPanel.builder("Config Updated")
                .row("<gray>File:</gray> " + messenger.id(target.fileName()))
                .row("<gray>Node:</gray> " + messenger.id(normalizedNode))
                .row("<gray>Previous:</gray> " + (exists ? configValue(oldValue) : "<dark_gray>missing</dark_gray>"))
                .row("<gray>Written:</gray> " + configValue(newValue))
                .row("<gray>Current:</gray> " + configValue(currentValue))
                .row("<gold>Run</gold> <click:suggest_command:'/ciaa config reload'><yellow>/ciaa config reload</yellow></click> <gold>to reload config objects.</gold>")
                .build());
    }

    private boolean configNodeExists(
            ConfigManager cfg,
            ConfigTarget target,
            String node
    ) {
        return switch (target) {
            case CONFIG -> cfg.globalNodeExists(node);
            case ARENA -> cfg.arenaNodeExists(node);
            case SKILL -> cfg.skillNodeExists(node);
        };
    }

    private boolean configSection(
            ConfigManager cfg,
            ConfigTarget target,
            String node
    ) {
        return switch (target) {
            case CONFIG -> cfg.globalSection(node);
            case ARENA -> cfg.arenaSection(node);
            case SKILL -> cfg.skillSection(node);
        };
    }

    private Object configNode(
            ConfigManager cfg,
            ConfigTarget target,
            String node
    ) {
        return switch (target) {
            case CONFIG -> cfg.getGlobalNode(node);
            case ARENA -> cfg.getArenaNode(node);
            case SKILL -> cfg.getSkillNode(node);
        };
    }

    private String configValue(Object value) {
        if (value == null) return "<dark_gray>null</dark_gray>";
        if (value instanceof ConfigurationSection section) {
            return "<dark_gray>object(" + section.getKeys(false).size() + " keys)</dark_gray>";
        }
        if (value instanceof List<?> list) {
            return messenger.value(list);
        }
        return messenger.value(value);
    }

    public void configGet(
            CommandSender sender,
            ConfigTarget target,
            String node
    ) {
        var cfg = rt.requireService(ConfigManager.class);
        var normalizedNode = ConfigWriteGuard.normalizeNode(node);
        if (normalizedNode == null) {
            messenger.error(sender, "Config node is required.");
            return;
        }

        if (!configNodeExists(cfg, target, normalizedNode)) {
            messenger.warnMini(sender, "Config node does not exist: " + messenger.id(normalizedNode));
            messenger.hint(sender, "Use /ciaa config list " + target.id() + " to browse available nodes.");
            return;
        }

        Object value = configNode(cfg, target, normalizedNode);
        messenger.panel(sender, CommandPanel.builder("Config Value")
                .row("<gray>File:</gray> " + messenger.id(target.fileName()))
                .row("<gray>Node:</gray> " + messenger.id(normalizedNode))
                .row("<gray>Type:</gray> " + messenger.id(configType(value)))
                .row("<gray>Value:</gray> " + configValue(value))
                .build());
    }

    private String configType(Object value) {
        return switch (value) {
            case null -> "null";
            case ConfigurationSection _ -> "object";
            case List<?> _ -> "list";
            default -> value.getClass().getSimpleName().toLowerCase();
        };
    }

    public void configList(
            CommandSender sender,
            ConfigTarget target
    ) {
        var cfg = rt.requireService(ConfigManager.class);
        var keys = configKeys(cfg, target);
        if (keys.isEmpty()) {
            messenger.warnMini(sender, "No config nodes found in " + messenger.id(target.fileName()));
            return;
        }

        var panel = CommandPanel.builder("Config Nodes: " + target.fileName());
        panel.row("<gray>Total:</gray> <gold>" + keys.size() + "</gold>");
        keys.stream().limit(60).forEach(key -> {
            Object value = configNode(cfg, target, key);
            panel.row("<click:suggest_command:'/ciaa config get " + target.id() + " "
                    + CommandMessenger.escapeForAttribute(key) + "'>"
                    + messenger.id(key) + "</click> <dark_gray>|</dark_gray> "
                    + messenger.id(configType(value)) + " <dark_gray>=</dark_gray> " + shortConfigValue(value));
        });
        if (keys.size() > 60) {
            panel.row("<dark_gray>…</dark_gray> <gray>" + (keys.size() - 60) + " more nodes hidden.</gray>");
        }
        messenger.panel(sender, panel.build());
    }

    private List<String> configKeys(
            ConfigManager cfg,
            ConfigTarget target
    ) {
        return switch (target) {
            case CONFIG -> cfg.listGlobalKeys();
            case ARENA -> cfg.listArenaKeys();
            case SKILL -> cfg.listSkillKeys();
        };
    }

    private String shortConfigValue(Object value) {
        var rendered = switch (value) {
            case null -> "null";
            case ConfigurationSection section -> "object(" + section.getKeys(false).size() + " keys)";
            case List<?> list -> list.toString();
            default -> String.valueOf(value);
        };
        if (rendered.length() > 48) rendered = rendered.substring(0, 45) + "...";
        return messenger.value(rendered);
    }

    public void configReload(CommandSender sender) {
        var cfg = rt.requireService(ConfigManager.class);
        cfg.reloadAll();
        I18n.reload();

        var abilityAdmin = rt.getService(IAbilityAdmin.class);
        if (abilityAdmin != null) abilityAdmin.reload();

        var regeneration = rt.getService(RegenerationService.class);
        if (regeneration != null) regeneration.reloadConfig();

        messenger.success(sender, "Configuration files reloaded.");
        messenger.hint(sender, "Runtime-only admin overrides are unchanged. Use /ciaa reload for a full plugin runtime reload.");
    }

    public void configUsage(CommandSender sender) {
        messenger.panel(sender, CommandPanel.builder("Config Commands")
                .row(new CommandUsage("/ciaa config get <config|arena|skill> <node>", "Read one config value.").toMiniRow())
                .row(new CommandUsage("/ciaa config list <config|arena|skill>", "List known config nodes.").toMiniRow())
                .row(new CommandUsage("/ciaa config set <config|arena|skill> <node> <value>", "Update an existing scalar/list node.").toMiniRow())
                .row(new CommandUsage("/ciaa config set <target> <node> --create <value>", "Create a missing node intentionally.").toMiniRow())
                .row(new CommandUsage("/ciaa config reload", "Reload config files and config-backed systems.").toMiniRow())
                .row("<yellow>Protection:</yellow> <gray>Object sections cannot be overwritten by this command.</gray>")
                .build());
    }

    public void unknownConfigTarget(CommandSender sender, String target) {
        messenger.errorMini(sender, "Unknown config target: " + messenger.id(target));
        messenger.hint(sender, "Valid targets: config, arena, skill.");
    }

}
