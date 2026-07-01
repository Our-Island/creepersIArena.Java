package top.ourisland.creepersiarena.core.command.handler.admin;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.core.ability.AbilityService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;
import top.ourisland.creepersiarena.core.command.message.CommandUsage;

import java.util.Comparator;

public final class AbilityAdminHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public AbilityAdminHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
    }

    public void abilityList(CommandSender sender) {
        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.");
            return;
        }

        var ids = admin.abilityIds();
        if (ids.isEmpty()) {
            messenger.warn(sender, "No abilities are registered or configured.");
            return;
        }

        var panel = CommandPanel.builder("Abilities");
        ids.stream()
                .sorted(Comparator.comparing(AbilityId::asString))
                .forEach(id -> panel.row("<click:suggest_command:'/ciaa ability info " + CommandMessenger.escapeForAttribute(id.asString()) + "'>"
                        + messenger.id(id.asString()) + "</click> "
                        + "<dark_gray>|</dark_gray> admin " + messenger.bool(admin.adminEnabled(id))
                        + " <dark_gray>|</dark_gray> config " + messenger.bool(admin.config(id).enabled(false))));
        messenger.panel(sender, panel.build());
    }

    public void abilityReload(CommandSender sender) {
        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.");
            return;
        }
        admin.reload();
        messenger.success(sender, "Ability runtime reloaded, including registered ability settings.");
    }

    public void enableAbility(CommandSender sender, AbilityId id) {
        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.");
            return;
        }

        admin.setAdminEnabled(id, true);
        messenger.successMini(sender, "Ability enabled by admin override: " + messenger.id(id.asString()));
        abilityInfo(sender, id);
    }

    public void abilityInfo(CommandSender sender, AbilityId id) {
        var admin = rt.requireService(IAbilityAdmin.class);
        var gate = rt.getService(IAbilityGate.class);
        var view = admin.config(id);
        var service = rt.getService(AbilityService.class);
        var registered = service == null ? null : service.registeredAbility(id);
        var effective = gate == null
                ? admin.isEnabled(id, null)
                : gate.isEnabledForGame(id, "command");

        messenger.panel(sender, CommandPanel.builder("Ability Info")
                .row("<gray>Ability:</gray> " + messenger.id(id.asString()))
                .row("<gray>Registered:</gray> " + messenger.yesNo(registered != null))
                .row("<gray>Owner:</gray> " + (registered == null
                        ? "<dark_gray>n/a</dark_gray>"
                        : messenger.id(registered.owner().extensionId().value())))
                .row("<gray>Config exists:</gray> " + messenger.yesNo(view.exists()))
                .row("<gray>Config enabled:</gray> " + messenger.bool(view.enabled(false)))
                .row("<gray>Default active:</gray> " + messenger.yesNo(view.defaultActive(false)))
                .row("<gray>Admin override:</gray> " + messenger.bool(admin.adminEnabled(id)))
                .row("<gray>Effective now:</gray> " + messenger.bool(effective))
                .build());
    }

    public void disableAbility(CommandSender sender, AbilityId id) {
        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.");
            return;
        }

        admin.setAdminEnabled(id, false);
        messenger.successMini(sender, "Ability disabled by admin override: " + messenger.id(id.asString()));
        abilityInfo(sender, id);
    }

    public void abilityUsage(CommandSender sender) {
        messenger.panel(sender, CommandPanel.builder("Ability Commands")
                .row(new CommandUsage("/ciaa ability list", "List all known abilities.").toMiniRow())
                .row(new CommandUsage("/ciaa ability info <ability>", "Show one ability's runtime state.").toMiniRow())
                .row(new CommandUsage("/ciaa ability enable <ability>", "Enable an ability through admin override.").toMiniRow())
                .row(new CommandUsage("/ciaa ability disable <ability>", "Disable an ability through admin override.").toMiniRow())
                .row(new CommandUsage("/ciaa ability reload", "Reload ability settings.").toMiniRow())
                .build());
    }

}
