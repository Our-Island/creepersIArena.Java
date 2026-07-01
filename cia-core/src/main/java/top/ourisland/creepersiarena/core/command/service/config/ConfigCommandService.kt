package top.ourisland.creepersiarena.core.command.service.config

import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.config.ConfigWriteGuard
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.command.message.CommandPanel
import top.ourisland.creepersiarena.core.command.message.CommandUsage
import top.ourisland.creepersiarena.core.command.model.ConfigTarget
import top.ourisland.creepersiarena.core.command.model.ConfigTarget.*
import top.ourisland.creepersiarena.core.config.ConfigManager
import top.ourisland.creepersiarena.core.game.regeneration.RegenerationService
import top.ourisland.creepersiarena.core.utils.I18n

class ConfigCommandService(
    context: CommandHandlerContext
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun config(
        sender: CommandSender,
        target: ConfigTarget,
        node: String,
        valueRaw: String
    ) {
        configSet(sender, target, node, valueRaw, false)
    }

    fun configSet(
        sender: CommandSender,
        target: ConfigTarget,
        node: String,
        valueRaw: String,
        create: Boolean
    ) {
        val cfg = rt.requireService(ConfigManager::class.java)
        val normalizedNode = ConfigWriteGuard.normalizeNode(node)

        if (normalizedNode == null) {
            messenger.error(sender, "Config node is required.")
            return
        }

        val exists = configNodeExists(cfg, target, normalizedNode)
        val section = exists && configSection(cfg, target, normalizedNode)

        try {
            ConfigWriteGuard.validateWrite(normalizedNode, exists, section, create)
        } catch (exception: IllegalArgumentException) {
            if (!exists) {
                messenger.warnMini(
                    sender,
                    "Config node does not exist: ${messenger.id(normalizedNode)}"
                )
                messenger.hint(
                    sender,
                    "Use /ciaa config set ${target.id()} $normalizedNode --create <value> to create it intentionally."
                )
            } else if (section) {
                messenger.errorMini(
                    sender,
                    "Refusing to overwrite object config node: ${messenger.id(normalizedNode)}"
                )
                messenger.hint(
                    sender,
                    "Use /ciaa config list ${target.id()} to choose a concrete child node."
                )
            } else {
                messenger.error(sender, exception.message)
            }
            return
        }

        val oldValue: Any? = if (exists) {
            configNode(cfg, target, normalizedNode)
        } else {
            null
        }

        val newValue: Any? = try {
            ConfigWriteGuard.coerceValue(oldValue, valueRaw)
        } catch (exception: IllegalArgumentException) {
            messenger.error(sender, exception.message)
            return
        }

        val ok = when (target) {
            CONFIG -> cfg.setGlobalNode(normalizedNode, newValue)
            ARENA -> cfg.setArenaNode(normalizedNode, newValue)
            SKILL -> cfg.setSkillNode(normalizedNode, newValue)
        }

        if (!ok) {
            messenger.error(sender, "Write failed.")
            return
        }

        val currentValue = configNode(cfg, target, normalizedNode)

        messenger.panel(
            sender,
            CommandPanel.builder("Config Updated")
                .row("<gray>File:</gray> ${messenger.id(target.fileName())}")
                .row("<gray>Node:</gray> ${messenger.id(normalizedNode)}")
                .row(
                    "<gray>Previous:</gray> ${
                        if (exists) configValue(oldValue) else "<dark_gray>missing</dark_gray>"
                    }"
                )
                .row("<gray>Written:</gray> ${configValue(newValue)}")
                .row("<gray>Current:</gray> ${configValue(currentValue)}")
                .row("<gold>Run</gold> <click:suggest_command:'/ciaa config reload'><yellow>/ciaa config reload</yellow></click> <gold>to reload config objects.</gold>")
                .build()
        )
    }

    private fun configNodeExists(
        cfg: ConfigManager,
        target: ConfigTarget,
        node: String
    ): Boolean {
        return when (target) {
            CONFIG -> cfg.globalNodeExists(node)
            ARENA -> cfg.arenaNodeExists(node)
            SKILL -> cfg.skillNodeExists(node)
        }
    }

    private fun configSection(
        cfg: ConfigManager,
        target: ConfigTarget,
        node: String
    ): Boolean {
        return when (target) {
            CONFIG -> cfg.globalSection(node)
            ARENA -> cfg.arenaSection(node)
            SKILL -> cfg.skillSection(node)
        }
    }

    private fun configNode(
        cfg: ConfigManager,
        target: ConfigTarget,
        node: String
    ): Any? {
        return when (target) {
            CONFIG -> cfg.getGlobalNode(node)
            ARENA -> cfg.getArenaNode(node)
            SKILL -> cfg.getSkillNode(node)
        }
    }

    private fun configValue(value: Any?): String {
        return when (value) {
            null -> "<dark_gray>null</dark_gray>"

            is ConfigurationSection -> {
                "<dark_gray>object(${value.getKeys(false).size} keys)</dark_gray>"
            }

            is List<*> -> messenger.value(value)

            else -> messenger.value(value)
        }
    }

    fun configGet(
        sender: CommandSender,
        target: ConfigTarget,
        node: String
    ) {
        val cfg = rt.requireService(ConfigManager::class.java)
        val normalizedNode = ConfigWriteGuard.normalizeNode(node)

        if (normalizedNode == null) {
            messenger.error(sender, "Config node is required.")
            return
        }

        if (!configNodeExists(cfg, target, normalizedNode)) {
            messenger.warnMini(
                sender,
                "Config node does not exist: ${messenger.id(normalizedNode)}"
            )
            messenger.hint(
                sender,
                "Use /ciaa config list ${target.id()} to browse available nodes."
            )
            return
        }

        val value = configNode(cfg, target, normalizedNode)

        messenger.panel(
            sender,
            CommandPanel.builder("Config Value")
                .row("<gray>File:</gray> ${messenger.id(target.fileName())}")
                .row("<gray>Node:</gray> ${messenger.id(normalizedNode)}")
                .row("<gray>Type:</gray> ${messenger.id(configType(value))}")
                .row("<gray>Value:</gray> ${configValue(value)}")
                .build()
        )
    }

    private fun configType(value: Any?): String {
        return when (value) {
            null -> "null"
            is ConfigurationSection -> "object"
            is List<*> -> "list"
            else -> value.javaClass.simpleName.lowercase()
        }
    }

    fun configList(
        sender: CommandSender,
        target: ConfigTarget
    ) {
        val cfg = rt.requireService(ConfigManager::class.java)
        val keys = configKeys(cfg, target)

        if (keys.isEmpty()) {
            messenger.warnMini(
                sender,
                "No config nodes found in ${messenger.id(target.fileName())}"
            )
            return
        }

        val panel = CommandPanel.builder("Config Nodes: ${target.fileName()}")

        panel.row("<gray>Total:</gray> <gold>${keys.size}</gold>")

        keys.take(60).forEach { key ->
            val value = configNode(cfg, target, key)

            panel.row(
                "<click:suggest_command:'/ciaa config get ${target.id()} " +
                        "${CommandMessenger.escapeForAttribute(key)}'>" +
                        "${messenger.id(key)}</click> <dark_gray>|</dark_gray> " +
                        "${messenger.id(configType(value))} <dark_gray>=</dark_gray> " +
                        shortConfigValue(value)
            )
        }

        if (keys.size > 60) {
            panel.row(
                "<dark_gray>…</dark_gray> <gray>${keys.size - 60} more nodes hidden.</gray>"
            )
        }

        messenger.panel(sender, panel.build())
    }

    private fun configKeys(
        cfg: ConfigManager,
        target: ConfigTarget
    ): List<String> {
        return when (target) {
            CONFIG -> cfg.listGlobalKeys()
            ARENA -> cfg.listArenaKeys()
            SKILL -> cfg.listSkillKeys()
        }
    }

    private fun shortConfigValue(value: Any?): String {
        var rendered = when (value) {
            null -> "null"

            is ConfigurationSection -> {
                "object(${value.getKeys(false).size} keys)"
            }

            is List<*> -> value.toString()

            else -> value.toString()
        }

        if (rendered.length > 48) {
            rendered = rendered.substring(0, 45) + "..."
        }

        return messenger.value(rendered)
    }

    fun configReload(sender: CommandSender) {
        val cfg = rt.requireService(ConfigManager::class.java)

        cfg.reloadAll()
        I18n.reload()

        val abilityAdmin = rt.getService(IAbilityAdmin::class.java)
        abilityAdmin?.reload()

        val regeneration = rt.getService(RegenerationService::class.java)
        regeneration?.reloadConfig()

        messenger.success(sender, "Configuration files reloaded.")
        messenger.hint(
            sender,
            "Runtime-only admin overrides are unchanged. Use /ciaa reload for a full plugin runtime reload."
        )
    }

    fun configUsage(sender: CommandSender) {
        messenger.panel(
            sender,
            CommandPanel.builder("Config Commands")
                .row(
                    CommandUsage(
                        "/ciaa config get <config|arena|skill> <node>",
                        "Read one config value."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/ciaa config list <config|arena|skill>",
                        "List known config nodes."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/ciaa config set <config|arena|skill> <node> <value>",
                        "Update an existing scalar/list node."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/ciaa config set <target> <node> --create <value>",
                        "Create a missing node intentionally."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/ciaa config reload",
                        "Reload config files and config-backed systems."
                    ).toMiniRow()
                )
                .row("<yellow>Protection:</yellow> <gray>Object sections cannot be overwritten by this command.</gray>")
                .build()
        )
    }

    fun unknownConfigTarget(sender: CommandSender, target: String) {
        messenger.errorMini(sender, "Unknown config target: ${messenger.id(target)}")
        messenger.hint(sender, "Valid targets: config, arena, skill.")
    }

}
