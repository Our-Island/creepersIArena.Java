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
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
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

import java.util.Collections;
import java.util.List;

import static top.ourisland.creepersiarena.core.command.CommandParsers.parseValue;

public final class AdminCommandHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public AdminCommandHandlers(BootstrapRuntime rt) {
        this(rt, new CommandMessenger());
    }

    public AdminCommandHandlers(
            BootstrapRuntime rt,
            CommandMessenger messenger
    ) {
        this.rt = rt;
        this.messenger = messenger;
    }

    public void help(CommandSender sender) {
        messenger.info(sender, """
                /ciaa mode <namespace:mode>
                /ciaa arena <arena_id>
                /ciaa skip [arena_id]
                /ciaa cooldown <factor>
                /ciaa regen <factor>
                /ciaa mutation [<bool>|trigger]
                /ciaa ability <list|info|status|enable|disable|reload> [namespace:ability]
                /ciaa database status
                /ciaa economy <balance|give|take|set> ...
                /ciaa store <list|open> ...
                /ciaa entrance <bool>
                /ciaa language <id>
                /ciaa reload
                /ciaa extensions list
                /ciaa extensions info <id>
                /ciaa extensions dump
                /ciaa config <config|arena|skill> <node> <value>""");
    }

    public void mode(CommandSender sender, GameModeId modeId) {
        var games = rt.requireService(GameManager.class);
        if (!games.hasMode(modeId)) {
            messenger.error(sender, "Unknown mode: %s".formatted(modeId.asString()));
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
        messenger.success(sender, "Mode switched to: " + modeId.asString());
    }

    public void modeUsage(CommandSender sender) {
        messenger.usage(sender, "Usage: /ciaa mode <namespace:mode>");
    }

    public void arena(CommandSender sender, ArenaId arenaId) {
        var gm = rt.requireService(GameManager.class);
        var am = rt.requireService(ArenaManager.class);

        var inst = am.getArena(arenaId);
        if (inst == null) {
            messenger.error(sender, "Arena not found: %s".formatted(arenaId));
            return;
        }

        var curMode = gm.active() == null ? null : gm.active().mode();
        if (curMode != null && !inst.type().equals(curMode)) {
            messenger.error(sender, "Arena mode mismatch. active=%s arena=%s".formatted(curMode, inst.type()));
            return;
        }

        var st = rt.requireService(AdminRuntimeState.class);
        st.forcedNextArenaId(arenaId);

        messenger.success(sender, "Next arena set to: %s".formatted(arenaId));
    }

    public void arenaUsage(CommandSender sender) {
        messenger.usage(sender, "Usage: /ciaa arena <arena_id>");
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
                messenger.success(sender, "Skipped. Started: mode=%s arena=%s".formatted(targetMode, arenaId));
                return;
            } catch (Throwable t) {
                messenger.warn(sender, "Failed to start with arena=%s (%s), fallback to auto.".formatted(arenaId, t.getMessage()));
            }
        }

        gm.startAuto(targetMode);
        messenger.success(sender, "Skipped. Started: mode=" + targetMode + " arena=auto");
    }

    public void setCooldownFactor(CommandSender sender, double factor) {
        if (Double.isNaN(factor) || Double.isInfinite(factor) || factor < 0) {
            messenger.error(sender, "Invalid factor: " + factor);
            return;
        }

        var st = rt.requireService(AdminRuntimeState.class);
        st.cooldownFactor(factor);
        messenger.success(sender, "Cooldown factor set to: %s".formatted(factor));
    }

    public void cooldownUsage(CommandSender sender) {
        messenger.usage(sender, "Usage: /ciaa cooldown <factor>");
    }

    public void regenerationStatus(CommandSender sender) {
        messenger.info(sender, "TBI");
    }

    public void setRegenerationFactor(CommandSender sender, double factor) {
        messenger.info(sender, "TBI");
    }

    public void mutationStatus(CommandSender sender) {
        var mutation = rt.getService(MutationService.class);
        if (mutation == null) {
            messenger.error(sender, "Mutation service is not available.");
            return;
        }

        messenger.info(sender, mutation.statusLine(mutationAdminEnabled()));
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
        messenger.info(sender, mutation.statusLine(mutationAdminEnabled()));
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
        messenger.success(sender, "Mutation admin enabled: %s".formatted(enabled));
        messenger.info(sender, mutation.statusLine(admin.adminEnabled(CoreAbilities.MUTATION)));
    }

    public void abilityList(CommandSender sender) {
        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.");
            return;
        }

        var ids = admin.abilityIds();
        if (ids.isEmpty()) {
            messenger.info(sender, "No abilities are registered or configured.");
            return;
        }
        messenger.info(sender, "Abilities:");
        ids.forEach(id -> messenger.info(sender, "- %s admin=%s config=%s".formatted(
                id.asString(),
                admin.adminEnabled(id),
                admin.config(id).enabled(false)
        )));
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
        messenger.success(sender, "Ability admin override cleared/enabled: %s".formatted(id.asString()));
        abilityInfo(sender, id);
    }

    public void abilityInfo(CommandSender sender, AbilityId id) {
        var admin = rt.requireService(IAbilityAdmin.class);
        var gate = rt.getService(IAbilityGate.class);
        var view = admin.config(id);
        var service = rt.getService(AbilityService.class);
        var registered = service == null ? null : service.registeredAbility(id);

        messenger.info(sender, "Ability: " + id.asString());
        messenger.info(sender, "  registered: " + (registered != null));
        if (registered != null) messenger.info(sender, "  owner: " + registered.owner());
        messenger.info(sender, "  config-exists: " + view.exists());
        messenger.info(sender, "  config-enabled: " + view.enabled(false));
        messenger.info(sender, "  default-active: " + view.defaultActive(false));
        messenger.info(sender, "  admin-enabled: " + admin.adminEnabled(id));
        messenger.info(sender, "  effective-current-game: " + (gate == null
                ? admin.isEnabled(id, null)
                : gate.isEnabledForGame(id, "command")));
    }

    public void disableAbility(CommandSender sender, AbilityId id) {
        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.");
            return;
        }

        admin.setAdminEnabled(id, false);
        messenger.success(sender, "Ability admin disabled: %s".formatted(id.asString()));
        abilityInfo(sender, id);
    }

    public void abilityUsage(CommandSender sender) {
        messenger.usage(sender, "Usage: /ciaa ability <list|info|status|enable|disable|reload> [namespace:ability]");
    }

    public void storeList(CommandSender sender) {
        var registry = rt.getService(IStoreRegistry.class);
        if (registry == null) {
            messenger.error(sender, "Store service is not available.");
            return;
        }

        messenger.info(sender, "Stores:");
        registry.stores().forEach(store -> messenger.info(
                sender,
                "- %s items=%d".formatted(store.id().asString(), registry.items(store.id()).size())
        ));
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
            messenger.error(sender, "Player must be online: %s".formatted(playerName));
            return;
        }
        if (registry.store(storeId) == null) {
            messenger.error(sender, "Unknown store: %s".formatted(storeId.asString()));
            return;
        }
        stores.openStore(player, storeId);
        messenger.success(sender, "Opened store %s for %s".formatted(storeId.asString(), player.getName()));
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
            messenger.error(sender, "Player must be online for economy commands: %s".formatted(playerName));
            return;
        }
        if (!wallet.loaded(player.getUniqueId())) {
            messenger.error(sender, "Player data is still loading: %s".formatted(player.getName()));
            return;
        }

        messenger.info(sender, "Balance for %s:".formatted(player.getName()));
        var balances = wallet.balances(player.getUniqueId());
        currencies.currencies().forEach(currency -> messenger.info(
                sender,
                "- %s: %d".formatted(
                        currency.id().asString(),
                        balances.getOrDefault(currency.id(), 0L)
                )
        ));
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
            messenger.error(sender, "Player must be online for economy commands: %s".formatted(playerName));
            return;
        }
        if (!wallet.loaded(player.getUniqueId())) {
            messenger.error(sender, "Player data is still loading: %s".formatted(player.getName()));
            return;
        }
        if (currencies.currency(currencyId) == null) {
            messenger.error(sender, "Unknown currency: %s".formatted(currencyId.asString()));
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
            messenger.error(sender, "Transaction failed. Missing: %s".formatted(result.missingAmounts()));
            return;
        }

        messenger.success(sender, "Economy updated: %s %s = %d".formatted(
                player.getName(),
                currencyId.asString(),
                wallet.balance(player.getUniqueId(), currencyId)
        ));
    }

    public void economyHelp(CommandSender sender) {
        messenger.usage(sender, "Usage: /ciaa economy balance <player>");
        messenger.usage(sender, "Usage: /ciaa economy <give|take|set> <player> <namespace:currency> <amount>");
    }

    public void entrance(CommandSender sender, boolean enabled) {
        var st = rt.requireService(AdminRuntimeState.class);
        st.entranceAllowed(enabled);
        messenger.success(sender, "Entrance allowed: %s".formatted(enabled));
    }

    public void entranceUsage(CommandSender sender) {
        messenger.usage(sender, "Usage: /ciaa entrance <boolean>");
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

        messenger.success(sender, "Default language set to: %s".formatted(lang.trim()));
    }

    public void languageUsage(CommandSender sender) {
        messenger.usage(sender, "Usage: /ciaa language <language_id>");
    }

    public void reload(CommandSender sender) {
        var st = rt.requireService(AdminRuntimeState.class);
        st.reset();

        rt.reloadPlugin();
        messenger.success(sender, "Reloaded.");
    }

    public void extensionsList(CommandSender sender) {
        sendLines(sender, ExtensionDiagnostics.listLines(rt));
    }

    private void sendLines(CommandSender sender, List<String> lines) {
        lines.forEach(line -> messenger.info(sender, line));
    }

    public void extensionInfo(CommandSender sender, String id) {
        if (id == null || id.isBlank()) {
            messenger.usage(sender, "Usage: /ciaa extensions info <extension_id>");
            return;
        }
        sendLines(sender, ExtensionDiagnostics.infoLines(rt, id));
    }

    public void extensionsDump(CommandSender sender) {
        try {
            var target = ExtensionDiagnostics.writeDump(rt);
            messenger.success(sender, "Extension dump written to: %s".formatted(target));
        } catch (Throwable t) {
            messenger.error(sender, "Failed to write extension dump: %s".formatted(t.getMessage()));
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
        messenger.success(sender, "Updated %s: %s = %s".formatted(target.fileName(), node, value));
        messenger.info(sender, "Current value: %s".formatted(currentValue));
        messenger.info(sender, "Run /ciaa reload to apply.");
    }

    public void configUsage(CommandSender sender) {
        messenger.usage(sender, "Usage: /ciaa config <config|arena|skill> <node> <value>");
    }

    public void unknownConfigTarget(CommandSender sender, String target) {
        messenger.error(sender, "Unknown target: %s".formatted(target));
    }

    public void databaseUnavailable(CommandSender sender) {
        messenger.error(sender, "Database service is not available.");
    }

    public void databaseTables(CommandSender sender, IDatabaseService database) {
        database.read(connection -> {
                    var names = new java.util.ArrayList<String>();
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
                                messenger.error(sender, "Database tables lookup failed: %s".formatted(error.getMessage()));
                                return;
                            }
                            messenger.info(sender, "Database tables (%d):".formatted(tables.size()));
                            tables.forEach(table -> messenger.info(sender, "- %s".formatted(table)));
                        }));
    }

    public void databaseStatus(CommandSender sender, IDatabaseService database) {
        messenger.info(sender, "Database:");
        messenger.info(sender, "- type: %s".formatted(database.type()));
        messenger.info(sender, "- table-prefix: %s".formatted(database.tablePrefix()));
        messenger.info(sender, "- ready: %s".formatted(database.ready()));
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
                        messenger.info(sender, "- connection: ok");
                    } else {
                        messenger.error(sender, "- connection: failed (" + (error == null
                                ? "unknown"
                                : error.getMessage()) + ")");
                    }
                }));
    }

}
