package top.ourisland.creepersiarena.core.command.handler.admin

import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.AdminRuntimeState
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandHelpRenderer
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.config.ConfigManager
import top.ourisland.creepersiarena.core.utils.I18n

class AdminSystemHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger
    private val helpRenderer: CommandHelpRenderer = context.helpRenderer

    fun help(sender: CommandSender) {
        helpRenderer.adminHelp(sender)
    }

    fun language(sender: CommandSender, lang: String) {
        val cfg = rt.requireService(ConfigManager::class.java)
        val trimmed = lang.trim()

        val ok = cfg.setGlobalNode("lang", trimmed)
        if (!ok) {
            messenger.error(sender, "Failed to write config.yml")
            return
        }

        cfg.reloadAll()
        I18n.reload()

        messenger.successMini(sender, "Default language set to: ${messenger.id(trimmed)}")
    }

    fun languageUsage(sender: CommandSender) {
        messenger.usage(sender, "/ciaa language <language_id>")
    }

    fun reload(sender: CommandSender) {
        val st = rt.requireService(AdminRuntimeState::class.java)
        st.reset()

        rt.reloadPlugin()
        messenger.success(sender, "Reloaded plugin runtime state.")
    }

}
