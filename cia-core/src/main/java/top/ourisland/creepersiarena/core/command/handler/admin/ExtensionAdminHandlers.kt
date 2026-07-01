package top.ourisland.creepersiarena.core.command.handler.admin;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.extension.debug.ExtensionDiagnostics;

import java.util.ArrayList;
import java.util.List;

public final class ExtensionAdminHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public ExtensionAdminHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
    }

    public void extensionsList(CommandSender sender) {
        messenger.panel(sender, "Extensions", diagnosticRows(ExtensionDiagnostics.listLines(rt)));
    }

    private List<String> diagnosticRows(List<String> lines) {
        if (lines == null || lines.isEmpty()) return List.of("<dark_gray>No diagnostic lines.</dark_gray>");

        var rows = new ArrayList<String>();
        lines.forEach(line -> {
            if (line == null || line.isBlank()) {
                rows.add("");
                return;
            }
            var trimmed = line.trim();
            if (trimmed.startsWith("- ")) {
                rows.add("<gray>•</gray> <white>" + CommandMessenger.escape(trimmed.substring(2)) + "</white>");
                return;
            }
            var idx = trimmed.indexOf('=');
            if (idx > 0) {
                var key = trimmed.substring(0, idx);
                var value = trimmed.substring(idx + 1);
                rows.add("<gray>•</gray> <aqua>" + CommandMessenger.escape(key) + "</aqua><dark_gray>:</dark_gray> <white>" + CommandMessenger.escape(value) + "</white>");
                return;
            }
            rows.add("<gray>" + CommandMessenger.escape(trimmed) + "</gray>");
        });
        return rows;
    }

    private void sendLines(CommandSender sender, List<String> lines) {
        messenger.panel(sender, "Diagnostics", diagnosticRows(lines));
    }

    public void extensionInfo(CommandSender sender, String id) {
        if (id == null || id.isBlank()) {
            messenger.usage(sender, "/ciaa extension info <extension_id>");
            return;
        }
        messenger.panel(sender, "Extension Info", diagnosticRows(ExtensionDiagnostics.infoLines(rt, id)));
    }

    public void extensionsDump(CommandSender sender) {
        try {
            var target = ExtensionDiagnostics.writeDump(rt);
            messenger.successMini(sender, "Extension dump written to: " + messenger.value(target));
        } catch (Throwable t) {
            messenger.errorMini(sender, "Failed to write extension dump: " + messenger.value(t.getMessage()));
        }
    }

}
