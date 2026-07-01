package top.ourisland.creepersiarena.core.command.handler.admin;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.model.ConfigTarget;
import top.ourisland.creepersiarena.core.command.service.config.ConfigCommandService;

public final class ConfigAdminHandlers {

    private final ConfigCommandService service;

    public ConfigAdminHandlers(CommandHandlerContext context) {
        this.service = new ConfigCommandService(context);
    }

    public void config(
            CommandSender sender,
            ConfigTarget target,
            String node,
            String valueRaw
    ) {
        service.config(sender, target, node, valueRaw);
    }

    public void configSet(
            CommandSender sender,
            ConfigTarget target,
            String node,
            String valueRaw,
            boolean create
    ) {
        service.configSet(sender, target, node, valueRaw, create);
    }

    public void configGet(
            CommandSender sender,
            ConfigTarget target,
            String node
    ) {
        service.configGet(sender, target, node);
    }

    public void configList(
            CommandSender sender,
            ConfigTarget target
    ) {
        service.configList(sender, target);
    }

    public void configReload(CommandSender sender) {
        service.configReload(sender);
    }

    public void configUsage(CommandSender sender) {
        service.configUsage(sender);
    }

    public void unknownConfigTarget(CommandSender sender, String target) {
        service.unknownConfigTarget(sender, target);
    }

}
