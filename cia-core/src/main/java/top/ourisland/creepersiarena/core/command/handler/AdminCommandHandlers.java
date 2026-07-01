package top.ourisland.creepersiarena.core.command.handler;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.database.IDatabaseService;
import top.ourisland.creepersiarena.api.economy.*;
import top.ourisland.creepersiarena.api.economy.store.IStoreRegistry;
import top.ourisland.creepersiarena.api.economy.store.IStoreService;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.api.game.arena.ArenaId;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.core.ability.AbilityService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.AdminRuntimeState;
import top.ourisland.creepersiarena.core.command.message.CommandHelpRenderer;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;
import top.ourisland.creepersiarena.core.command.message.CommandUsage;
import top.ourisland.creepersiarena.core.command.model.ConfigTarget;
import top.ourisland.creepersiarena.core.command.model.EconomyOperation;
import top.ourisland.creepersiarena.core.config.ConfigManager;
import top.ourisland.creepersiarena.core.extension.debug.ExtensionDiagnostics;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.game.arena.ArenaManager;
import top.ourisland.creepersiarena.core.game.flow.GameFlow;
import top.ourisland.creepersiarena.core.game.mutation.MutationResetReason;
import top.ourisland.creepersiarena.core.game.mutation.MutationService;
import top.ourisland.creepersiarena.core.utils.I18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static top.ourisland.creepersiarena.core.command.CommandParsers.parseValue;

public final class AdminCommandHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;
    private final CommandHelpRenderer helpRenderer;

    public AdminCommandHandlers(BootstrapRuntime rt) {
        this(rt, new CommandMessenger());
    }

    public AdminCommandHandlers(
            BootstrapRuntime rt,
            CommandMessenger messenger
    ) {
        this.rt = rt;
        this.messenger = messenger;
        this.helpRenderer = new CommandHelpRenderer(messenger);
    }

    public void help(CommandSender sender) {
        helpRenderer.adminHelp(sender);
    }

    public void mode(CommandSender sender, GameModeId modeId) {
        var games = rt.requireService(GameManager.class);
        if (!games.hasMode(modeId)) {
            messenger.errorMini(sender, "Unknown mode: " + messenger.id(modeId.asString()));
            messenger.hint(sender, "Use /ciaa mode and press Tab to see available modes.");
            return;
        }

        var state = rt.requireService(AdminRuntimeState.class);
        state.forcedNextMode(modeId);
        state.forcedNextArenaId(null);

        var flow = rt.requireService(GameFlow.class);
        if (games.active() != null) {
            flow.endGameAndBackToHub("ADMIN_MODE_SWITCH");
        }

        games.startAuto(modeId);
        messenger.successMini(sender, "Mode switched to: " + messenger.id(modeId.asString()));
        messenger.hint(sender, "The active game was ended and a new auto-start flow was requested.");
    }

    public void modeUsage(CommandSender sender) {
        messenger.usage(sender, "/ciaa mode <namespace:mode>");
    }

    public void arena(CommandSender sender, ArenaId arenaId) {
        var gm = rt.requireService(GameManager.class);
        var am = rt.requireService(ArenaManager.class);

        var inst = am.getArena(arenaId);
        if (inst == null) {
            messenger.errorMini(sender, "Arena not found: " + messenger.id(arenaId.toString()));
            messenger.hint(sender, "Use /ciaa arena and press Tab to see loaded arenas.");
            return;
        }

        var curMode = gm.active() == null ? null : gm.active().mode();
        if (curMode != null && !inst.type().equals(curMode)) {
            messenger.errorMini(sender, "Arena mode mismatch. Active: " + messenger.id(curMode.asString())
                    + " <gray>arena:</gray> " + messenger.id(inst.type().asString()));
            return;
        }

        var st = rt.requireService(AdminRuntimeState.class);
        st.forcedNextArenaId(arenaId);

        messenger.successMini(sender, "Next arena set to: " + messenger.id(arenaId.toString()));
    }

    public void arenaUsage(CommandSender sender) {
        messenger.usage(sender, "/ciaa arena <arena_id>");
    }

    public void invalidArena(CommandSender sender, String message) {
        messenger.error(sender, message);
    }

    public void skip(CommandSender sender, ArenaId overrideArena) {
        var st = rt.requireService(AdminRuntimeState.class);
        var gm = rt.requireService(GameManager.class);
        var flow = rt.requireService(GameFlow.class);

        var targetMode = st.forcedNextMode();
        if (targetMode == null) {
            var g = gm.active();
            targetMode = (g == null)
                    ? rt.requireService(ConfigManager.class).globalConfig().game().defaultMode()
                    : g.mode();
            if (targetMode == null) {
                messenger.error(sender, "No active or configured default game mode.");
                return;
            }
        }

        flow.endGameAndBackToHub("ADMIN_SKIP");

        var arenaId = overrideArena != null ? overrideArena : st.forcedNextArenaId();
        if (arenaId != null) {
            try {
                gm.start(targetMode, arenaId);
                messenger.successMini(sender, "Skipped current game. Started " + messenger.id(targetMode.asString())
                        + " on " + messenger.id(arenaId.toString()));
                return;
            } catch (Throwable t) {
                messenger.warnMini(sender, "Failed to start arena " + messenger.id(arenaId.toString())
                        + " <dark_gray>(" + CommandMessenger.escape(t.getMessage()) + ")</dark_gray>; falling back to auto arena.");
            }
        }

        gm.startAuto(targetMode);
        messenger.successMini(sender, "Skipped current game. Started " + messenger.id(targetMode.asString()) + " with <gold>auto arena</gold>.");
    }

    public void setCooldownFactor(CommandSender sender, double factor) {
        if (Double.isNaN(factor) || Double.isInfinite(factor) || factor < 0) {
            messenger.errorMini(sender, "Invalid factor: " + messenger.value(factor));
            return;
        }

        var st = rt.requireService(AdminRuntimeState.class);
        st.cooldownFactor(factor);
        messenger.successMini(sender, "Cooldown factor set to: <gold>" + factor + "x</gold>");
    }

    public void cooldownUsage(CommandSender sender) {
        messenger.usage(sender, "/ciaa cooldown <factor>");
    }

    public void regenerationStatus(CommandSender sender) {
        messenger.warn(sender, "Regeneration controls are not available yet.");
        messenger.hint(sender, "This command is reserved for a later implementation stage.");
    }

    public void setRegenerationFactor(CommandSender sender, double factor) {
        messenger.warn(sender, "Regeneration controls are not available yet.");
        messenger.hint(sender, "Requested factor was " + factor + "x, but the regeneration command is not wired to a runtime service yet.");
    }

    public void mutationStatus(CommandSender sender) {
        var mutation = rt.getService(MutationService.class);
        if (mutation == null) {
            messenger.error(sender, "Mutation service is not available.");
            return;
        }

        messenger.panel(sender, CommandPanel.builder("Mutation")
                .row(statusLine(mutation.statusLine(mutationAdminEnabled())))
                .row("<gray>Admin override:</gray> " + messenger.bool(mutationAdminEnabled()))
                .build());
    }

    private boolean mutationAdminEnabled() {
        var admin = rt.getService(IAbilityAdmin.class);
        return admin == null || admin.adminEnabled(CoreAbilities.MUTATION);
    }

    public void triggerMutation(CommandSender sender) {
        var mutation = rt.getService(MutationService.class);
        if (mutation == null) {
            messenger.error(sender, "Mutation service is not available.");
            return;
        }

        var result = mutation.trigger();
        messenger.info(sender, result.message());
        mutationStatus(sender);
    }

    public void setMutationEnabled(CommandSender sender, boolean enabled) {
        var mutation = rt.getService(MutationService.class);
        if (mutation == null) {
            messenger.error(sender, "Mutation service is not available.");
            return;
        }

        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.");
            return;
        }
        admin.setAdminEnabled(CoreAbilities.MUTATION, enabled);
        if (!enabled) mutation.reset(MutationResetReason.ADMIN_DISABLED);
        messenger.successMini(sender, "Mutation admin override: " + messenger.bool(enabled));
        mutationStatus(sender);
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
                .row("<gray>Owner:</gray> " + (registered == null ? "<dark_gray>n/a</dark_gray>" : messenger.id(registered.owner().extensionId().value())))
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

    public void storeList(CommandSender sender) {
        var registry = rt.getService(IStoreRegistry.class);
        if (registry == null) {
            messenger.error(sender, "Store service is not available.");
            return;
        }

        var stores = registry.stores().stream()
                .sorted(Comparator.comparing(store -> store.id().asString()))
                .toList();
        if (stores.isEmpty()) {
            messenger.warn(sender, "No stores are registered.");
            return;
        }

        var panel = CommandPanel.builder("Stores");
        stores.forEach(store -> panel.row("<click:suggest_command:'/ciaa store open '>"
                + messenger.id(store.id().asString()) + "</click> "
                + "<dark_gray>|</dark_gray> rows <gold>" + store.rows() + "</gold> "
                + "<dark_gray>|</dark_gray> items <gold>" + registry.items(store.id()).size() + "</gold>"));
        messenger.panel(sender, panel.build());
    }

    public void openStore(
            CommandSender sender,
            String playerName,
            StoreId storeId
    ) {
        var stores = rt.getService(IStoreService.class);
        var registry = rt.getService(IStoreRegistry.class);
        if (stores == null || registry == null) {
            messenger.error(sender, "Store service is not available.");
            return;
        }

        var player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            messenger.errorMini(sender, "Player must be online: " + messenger.id(playerName));
            return;
        }
        if (registry.store(storeId) == null) {
            messenger.errorMini(sender, "Unknown store: " + messenger.id(storeId.asString()));
            messenger.hint(sender, "Use /ciaa store list or press Tab to see available stores.");
            return;
        }
        stores.openStore(player, storeId);
        messenger.successMini(sender, "Opened store " + messenger.id(storeId.asString()) + " for " + messenger.id(player.getName()));
    }

    public void economyBalance(CommandSender sender, String playerName) {
        var wallet = rt.getService(IWalletService.class);
        var currencies = rt.getService(ICurrencyRegistry.class);
        if (wallet == null || currencies == null) {
            messenger.error(sender, "Economy service is not available.");
            return;
        }

        var player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            messenger.errorMini(sender, "Player must be online for economy commands: " + messenger.id(playerName));
            return;
        }
        if (!wallet.loaded(player.getUniqueId())) {
            messenger.warnMini(sender, "Player data is still loading: " + messenger.id(player.getName()));
            return;
        }

        var panel = CommandPanel.builder("Balance: " + player.getName());
        var balances = wallet.balances(player.getUniqueId());
        currencies.currencies().stream()
                .sorted(Comparator.comparing(currency -> currency.id().asString()))
                .forEach(currency -> panel.row("<gray>•</gray> " + messenger.id(currency.id().asString())
                        + " <dark_gray>=</dark_gray> <gold>" + balances.getOrDefault(currency.id(), 0L) + "</gold>"));
        messenger.panel(sender, panel.build());
    }

    public void economyAmount(
            CommandSender sender,
            EconomyOperation operation,
            String playerName,
            CurrencyId currencyId,
            long amount
    ) {
        var wallet = rt.getService(IWalletService.class);
        var currencies = rt.getService(ICurrencyRegistry.class);
        if (wallet == null || currencies == null) {
            messenger.error(sender, "Economy service is not available.");
            return;
        }

        var player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            messenger.errorMini(sender, "Player must be online for economy commands: " + messenger.id(playerName));
            return;
        }
        if (!wallet.loaded(player.getUniqueId())) {
            messenger.warnMini(sender, "Player data is still loading: " + messenger.id(player.getName()));
            return;
        }
        if (currencies.currency(currencyId) == null) {
            messenger.errorMini(sender, "Unknown currency: " + messenger.id(currencyId.asString()));
            messenger.hint(sender, "Use /ciaa economy and press Tab at the currency argument.");
            return;
        }

        var currencyAmount = new CurrencyAmount(currencyId, amount);
        var reason = WalletChangeReason.command("admin:%s".formatted(operation.id()));
        var result = switch (operation) {
            case GIVE -> wallet.deposit(player.getUniqueId(), currencyAmount, reason);
            case TAKE -> wallet.withdraw(player.getUniqueId(), CurrencyCost.of(currencyAmount), reason);
            case SET -> wallet.set(player.getUniqueId(), currencyAmount, reason);
        };

        if (result.disabled()) {
            messenger.error(sender, "Currency ability is disabled.");
            return;
        }
        if (!result.success()) {
            messenger.errorMini(sender, "Transaction failed. Missing: " + messenger.value(result.missingAmounts()));
            return;
        }

        messenger.panel(sender, CommandPanel.builder("Economy Updated")
                .row("<gray>Operation:</gray> " + messenger.id(operation.id()))
                .row("<gray>Player:</gray> " + messenger.id(player.getName()))
                .row("<gray>Currency:</gray> " + messenger.id(currencyId.asString()))
                .row("<gray>New balance:</gray> <gold>" + wallet.balance(player.getUniqueId(), currencyId) + "</gold>")
                .build());
    }

    public void economyHelp(CommandSender sender) {
        messenger.panel(sender, CommandPanel.builder("Economy Commands")
                .row(new CommandUsage("/ciaa economy balance <player>", "Show all balances for an online player.").toMiniRow())
                .row(new CommandUsage("/ciaa economy give <player> <currency> <amount>", "Deposit currency into a wallet.").toMiniRow())
                .row(new CommandUsage("/ciaa economy take <player> <currency> <amount>", "Withdraw currency from a wallet.").toMiniRow())
                .row(new CommandUsage("/ciaa economy set <player> <currency> <amount>", "Set one currency balance exactly.").toMiniRow())
                .build());
    }

    public void entrance(CommandSender sender, boolean enabled) {
        var st = rt.requireService(AdminRuntimeState.class);
        st.entranceAllowed(enabled);
        messenger.successMini(sender, "Entrance is now " + messenger.bool(enabled));
    }

    public void entranceUsage(CommandSender sender) {
        messenger.usage(sender, "/ciaa entrance <true|false>");
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

    public void extensionsList(CommandSender sender) {
        messenger.panel(sender, "Extensions", diagnosticRows(ExtensionDiagnostics.listLines(rt)));
    }

    private void sendLines(CommandSender sender, List<String> lines) {
        messenger.panel(sender, "Diagnostics", diagnosticRows(lines));
    }

    public void extensionInfo(CommandSender sender, String id) {
        if (id == null || id.isBlank()) {
            messenger.usage(sender, "/ciaa extensions info <extension_id>");
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

    // TODO: modify a field with object will break the config
    public void config(
            CommandSender sender,
            ConfigTarget target,
            String node,
            String valueRaw
    ) {
        Object value = parseValue(valueRaw);

        var cfg = rt.requireService(ConfigManager.class);

        boolean ok = switch (target) {
            case CONFIG -> cfg.setGlobalNode(node, value);
            case ARENA -> cfg.setArenaNode(node, value);
            case SKILL -> cfg.setSkillNode(node, value);
        };

        if (!ok) {
            messenger.error(sender, "Write failed.");
            return;
        }

        Object cur = switch (target) {
            case CONFIG -> cfg.getGlobalNode(node);
            case ARENA -> cfg.getArenaNode(node);
            case SKILL -> cfg.getSkillNode(node);
        };
        var currentValue = cur == null ? "null" : String.valueOf(cur);
        messenger.panel(sender, CommandPanel.builder("Config Updated")
                .row("<gray>File:</gray> " + messenger.id(target.fileName()))
                .row("<gray>Node:</gray> " + messenger.id(node))
                .row("<gray>Written value:</gray> " + messenger.value(value))
                .row("<gray>Current value:</gray> " + messenger.value(currentValue))
                .row("<gold>Run</gold> <click:suggest_command:'/ciaa reload'><yellow>/ciaa reload</yellow></click> <gold>to apply the change.</gold>")
                .build());
    }

    public void configUsage(CommandSender sender) {
        messenger.panel(sender, CommandPanel.builder("Config Command")
                .row(new CommandUsage("/ciaa config config <node> <value>", "Edit config.yml.").toMiniRow())
                .row(new CommandUsage("/ciaa config arena <node> <value>", "Edit arena.yml.").toMiniRow())
                .row(new CommandUsage("/ciaa config skill <node> <value>", "Edit skill.yml.").toMiniRow())
                .row("<yellow>Warning:</yellow> <gray>Object-node protection is planned for the next config command stage.</gray>")
                .build());
    }

    public void unknownConfigTarget(CommandSender sender, String target) {
        messenger.errorMini(sender, "Unknown config target: " + messenger.id(target));
        messenger.hint(sender, "Valid targets: config, arena, skill.");
    }

    public void databaseUnavailable(CommandSender sender) {
        messenger.error(sender, "Database service is not available.");
    }

    public void databaseTables(CommandSender sender, IDatabaseService database) {
        database.read(connection -> {
                    var names = new ArrayList<String>();
                    var metadata = connection.getMetaData();
                    try (var rs = metadata.getTables(null, null, database.tablePrefix() + "%", new String[]{"TABLE"})) {
                        while (rs.next()) {
                            names.add(rs.getString("TABLE_NAME"));
                        }
                    }
                    Collections.sort(names);
                    return names;
                })
                .whenComplete((tables, error) -> Bukkit.getServer()
                        .getGlobalRegionScheduler()
                        .execute(rt.plugin(), () -> {
                            if (error != null) {
                                messenger.errorMini(sender, "Database tables lookup failed: " + messenger.value(error.getMessage()));
                                return;
                            }
                            var panel = CommandPanel.builder("Database Tables");
                            panel.row("<gray>Total:</gray> <gold>" + tables.size() + "</gold>");
                            tables.forEach(table -> panel.row("<gray>•</gray> " + messenger.id(table)));
                            messenger.panel(sender, panel.build());
                        }));
    }

    public void databaseStatus(CommandSender sender, IDatabaseService database) {
        messenger.panel(sender, CommandPanel.builder("Database")
                .row("<gray>Type:</gray> " + messenger.id(database.type()))
                .row("<gray>Table prefix:</gray> " + messenger.id(database.tablePrefix()))
                .row("<gray>Ready:</gray> " + messenger.yesNo(database.ready()))
                .row("<gray>Connection:</gray> <yellow>checking...</yellow>")
                .build());
        databasePing(sender, database);
    }

    public void databasePing(CommandSender sender, IDatabaseService database) {
        database.read(connection -> {
                    try (var st = connection.createStatement()) {
                        st.execute("SELECT 1");
                    }
                    return true;
                })
                .whenComplete((ok, error) -> Bukkit.getServer().getGlobalRegionScheduler().execute(rt.plugin(), () -> {
                    if (error == null && Boolean.TRUE.equals(ok)) {
                        messenger.success(sender, "Database connection OK.");
                    } else {
                        messenger.errorMini(sender, "Database connection failed: " + messenger.value(error == null
                                ? "unknown"
                                : error.getMessage()));
                    }
                }));
    }

    private String statusLine(String plain) {
        return "<gray>" + CommandMessenger.escape(plain) + "</gray>";
    }

    private List<String> diagnosticRows(List<String> lines) {
        if (lines == null || lines.isEmpty()) return List.of("<dark_gray>No diagnostic lines.</dark_gray>");

        var rows = new ArrayList<String>();
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                rows.add("");
                continue;
            }
            var trimmed = line.trim();
            if (trimmed.startsWith("- ")) {
                rows.add("<gray>•</gray> <white>" + CommandMessenger.escape(trimmed.substring(2)) + "</white>");
                continue;
            }
            var idx = trimmed.indexOf('=');
            if (idx > 0) {
                var key = trimmed.substring(0, idx);
                var value = trimmed.substring(idx + 1);
                rows.add("<gray>•</gray> <aqua>" + CommandMessenger.escape(key) + "</aqua><dark_gray>:</dark_gray> <white>" + CommandMessenger.escape(value) + "</white>");
                continue;
            }
            rows.add("<gray>" + CommandMessenger.escape(trimmed) + "</gray>");
        }
        return rows;
    }

}
