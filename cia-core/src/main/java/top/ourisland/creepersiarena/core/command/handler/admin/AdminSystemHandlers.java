package top.ourisland.creepersiarena.core.command.handler.admin;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.AdminRuntimeState;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandHelpRenderer;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.config.ConfigManager;
import top.ourisland.creepersiarena.core.utils.I18n;

public final class AdminSystemHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;
    private final CommandHelpRenderer helpRenderer;

    public AdminSystemHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
        this.helpRenderer = context.helpRenderer();
    }

    public void help(CommandSender sender) {
        helpRenderer.adminHelp(sender);
    }

    public void language(CommandSender sender, String lang) {
        var cfg = rt.requireService(ConfigManager.class);

        boolean ok = cfg.setGlobalNode("lang", lang.trim());
        if (!ok) {
            messenger.error(sender, "Failed to write config.yml");
            return;
        }

        cfg.reloadAll();
        I18n.reload();

        messenger.successMini(sender, "Default language set to: " + messenger.id(lang.trim()));
    }

    public void languageUsage(CommandSender sender) {
        messenger.usage(sender, "/ciaa language <language_id>");
    }

    public void reload(CommandSender sender) {
        var st = rt.requireService(AdminRuntimeState.class);
        st.reset();

        rt.reloadPlugin();
        messenger.success(sender, "Reloaded plugin runtime state.");
    }

}
