package top.ourisland.creepersiarena.core.command.handler.admin

import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.api.ability.AbilityId
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin
import top.ourisland.creepersiarena.api.ability.IAbilityGate
import top.ourisland.creepersiarena.core.ability.AbilityService
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.command.message.CommandPanel
import top.ourisland.creepersiarena.core.command.message.CommandUsage

class AbilityAdminHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun abilityList(sender: CommandSender) {
        val admin = rt.getService(IAbilityAdmin::class.java)
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.")
            return
        }

        val ids = admin.abilityIds()
        if (ids.isEmpty()) {
            messenger.warn(sender, "No abilities are registered or configured.")
            return
        }

        val panel = CommandPanel.builder("Abilities")
        ids.sortedBy { it.asString() }.forEach { id ->
            panel.row(
                "<click:suggest_command:'/ciaa ability info ${CommandMessenger.escapeForAttribute(id.asString())}'>${
                    messenger.id(
                        id.asString()
                    )
                }</click> <dark_gray>|</dark_gray> admin ${messenger.bool(admin.adminEnabled(id))} <dark_gray>|</dark_gray> config ${
                    messenger.bool(
                        admin.config(id).enabled(false)
                    )
                }"
            )
        }
        messenger.panel(sender, panel.build())
    }

    fun abilityReload(sender: CommandSender) {
        val admin = rt.getService(IAbilityAdmin::class.java)
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.")
            return
        }
        admin.reload()
        messenger.success(sender, "Ability runtime reloaded, including registered ability settings.")
    }

    fun enableAbility(sender: CommandSender, id: AbilityId) {
        val admin = rt.getService(IAbilityAdmin::class.java)
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.")
            return
        }

        admin.setAdminEnabled(id, true)
        messenger.successMini(sender, "Ability enabled by admin override: ${messenger.id(id.asString())}")
        abilityInfo(sender, id)
    }

    fun abilityInfo(sender: CommandSender, id: AbilityId) {
        val admin = rt.requireService(IAbilityAdmin::class.java)
        val gate = rt.getService(IAbilityGate::class.java)
        val view = admin.config(id)
        val service = rt.getService(AbilityService::class.java)
        val registered = service?.registeredAbility(id)
        val effective = gate?.isEnabledForGame(id, "command") ?: admin.isEnabled(id, null)

        messenger.panel(
            sender,
            CommandPanel.builder("Ability Info")
                .row("<gray>Ability:</gray> ${messenger.id(id.asString())}")
                .row("<gray>Registered:</gray> ${messenger.yesNo(registered != null)}")
                .row(
                    "<gray>Owner:</gray> " +
                            if (registered == null) {
                                "<dark_gray>n/a</dark_gray>"
                            } else {
                                messenger.id(registered.owner().extensionId().value())
                            }
                )
                .row("<gray>Config exists:</gray> ${messenger.yesNo(view.exists())}")
                .row("<gray>Config enabled:</gray> ${messenger.bool(view.enabled(false))}")
                .row("<gray>Default active:</gray> ${messenger.yesNo(view.defaultActive(false))}")
                .row("<gray>Admin override:</gray> ${messenger.bool(admin.adminEnabled(id))}")
                .row("<gray>Effective now:</gray> ${messenger.bool(effective)}")
                .build()
        )
    }

    fun disableAbility(sender: CommandSender, id: AbilityId) {
        val admin = rt.getService(IAbilityAdmin::class.java)
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.")
            return
        }

        admin.setAdminEnabled(id, false)
        messenger.successMini(sender, "Ability disabled by admin override: ${messenger.id(id.asString())}")
        abilityInfo(sender, id)
    }

    fun abilityUsage(sender: CommandSender) {
        messenger.panel(
            sender,
            CommandPanel.builder("Ability Commands")
                .row(CommandUsage("/ciaa ability list", "List all known abilities.").toMiniRow())
                .row(CommandUsage("/ciaa ability info <ability>", "Show one ability's runtime state.").toMiniRow())
                .row(
                    CommandUsage(
                        "/ciaa ability enable <ability>",
                        "Enable an ability through admin override."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/ciaa ability disable <ability>",
                        "Disable an ability through admin override."
                    ).toMiniRow()
                )
                .row(CommandUsage("/ciaa ability reload", "Reload ability settings.").toMiniRow())
                .build()
        )
    }

}
