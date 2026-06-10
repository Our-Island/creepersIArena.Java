package top.ourisland.creepersiarena.game.ability;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.*;
import top.ourisland.creepersiarena.api.game.mode.IModeAbilityPolicy;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.core.component.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.game.GameManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public final class AbilityService implements IAbilityRegistry, IAbilityAdmin {

    private final Logger logger;
    private final ConfigManager configManager;
    private final Supplier<GameManager> gameManager;
    private final Map<AbilityId, RegisteredAbility> abilities = new ConcurrentHashMap<>();
    private final List<RegisteredAbilityPolicy> policies = new CopyOnWriteArrayList<>();
    private final Map<AbilityId, Boolean> adminOverrides = new ConcurrentHashMap<>();
    private volatile YamlConfiguration configSnapshot = new YamlConfiguration();

    public AbilityService(
            Logger logger,
            ConfigManager configManager,
            Supplier<GameManager> gameManager
    ) {
        this.logger = logger;
        this.configManager = configManager;
        this.gameManager = gameManager;
        reloadSnapshot();
    }

    private void reloadSnapshot() {
        var path = configManager.dataDir().resolve("config.yml");
        configSnapshot = YamlConfiguration.loadConfiguration(path.toFile());
    }

    @Override
    public void registerAbility(String ownerId, IAbility ability) {
        if (ability == null || ability.id() == null) return;
        String owner = RegisteredComponent.normalizeOwnerId(ownerId);
        abilities.put(ability.id(), new RegisteredAbility(owner, ability));
        try {
            ability.reload(config(ability.id()));
        } catch (Throwable t) {
            logger.warn("[Ability] reload failed while registering {}: {}", ability.id(), t.getMessage(), t);
        }
        logger.info("[Ability] Registered {} by {}.", ability.id(), owner);
    }

    @Override
    public void registerPolicy(String ownerId, IAbilityPolicy policy) {
        if (policy == null) return;
        String owner = RegisteredComponent.normalizeOwnerId(ownerId);
        policies.add(new RegisteredAbilityPolicy(owner, policy));
        logger.info("[Ability] Registered policy {} by {}.", policy.getClass().getSimpleName(), owner);
    }

    @Override
    public void setAdminEnabled(AbilityId abilityId, boolean enabled) {
        if (abilityId == null) return;
        if (enabled) adminOverrides.remove(abilityId);
        else adminOverrides.put(abilityId, false);
    }

    @Override
    public boolean adminEnabled(AbilityId abilityId) {
        return !Boolean.FALSE.equals(adminOverrides.get(abilityId));
    }

    @Override
    public List<AbilityId> abilityIds() {
        var out = new LinkedHashSet<>(abilities.keySet());
        var root = configSnapshot.getConfigurationSection("game.abilities");
        if (root != null) {
            for (var ns : root.getKeys(false)) {
                var nsSec = root.getConfigurationSection(ns);
                if (nsSec == null) continue;
                for (var value : nsSec.getKeys(false)) {
                    if (nsSec.isConfigurationSection(value)) out.add(AbilityId.of(ns, value));
                }
            }
        }
        return out.stream().sorted(Comparator.comparing(AbilityId::asString)).toList();
    }

    @Override
    public IAbilityConfigView config(AbilityId abilityId) {
        var id = abilityId == null
                ? AbilityId.of("core:unknown")
                : abilityId;
        var section = configSnapshot.getConfigurationSection(id.configPath());
        return new BukkitAbilityConfigView(id, section);
    }

    @Override
    public boolean isEnabled(AbilityId id, AbilityContext context) {
        if (id == null) return false;
        var view = config(id);
        boolean registered = abilities.containsKey(id);
        if (!registered && !view.exists()) return false;

        var section = view.section();
        if (section == null || !section.getBoolean("enabled", false)) return false;
        if (!adminEnabled(id)) return false;

        var state = DecisionState.active(view.defaultActive(false));
        state = applyConfigOverrides(state, section, context);
        if (state.hardDenied()) return false;

        state = applyModePolicies(id, context, state);
        if (state.hardDenied()) return false;

        state = applyRegisteredPolicies(id, context, state);
        return !state.hardDenied() && state.active();
    }

    private DecisionState applyConfigOverrides(
            DecisionState state,
            ConfigurationSection section,
            AbilityContext context
    ) {
        if (context == null) return state;
        state = applyNamedOverride(state, section.getConfigurationSection("modes"), context.modeId(), context.phase());
        if (state.hardDenied()) return state;
        return applyNamedOverride(state, section.getConfigurationSection("arenas"), context.arenaId(), context.phase());
    }

    private DecisionState applyModePolicies(
            AbilityId id,
            AbilityContext context,
            DecisionState state
    ) {
        var gm = gameManager == null ? null : gameManager.get();
        if (gm == null) return state;

        var out = state;
        Object[] candidates = {gm.rules(), gm.timeline(), gm.playerFlow()};
        for (var candidate : candidates) {
            if (!(candidate instanceof IModeAbilityPolicy policy)) continue;
            var decision = evaluatePolicy(policy, id, context, "mode");
            out = applyPolicyDecision(out, decision);
            if (out.hardDenied()) return out;
        }
        return out;
    }

    private DecisionState applyRegisteredPolicies(
            AbilityId id,
            AbilityContext context,
            DecisionState state
    ) {
        var out = state;
        for (var registered : policies) {
            var decision = evaluatePolicy(registered.value(), id, context, registered.ownerId());
            out = applyPolicyDecision(out, decision);
            if (out.hardDenied()) return out;
        }
        return out;
    }

    private DecisionState applyNamedOverride(
            DecisionState state,
            @Nullable ConfigurationSection parent,
            @Nullable String rawName,
            @Nullable String phase
    ) {
        if (parent == null || rawName == null || rawName.isBlank()) return state;
        ConfigurationSection override = null;
        for (var name : candidateKeys(rawName)) {
            override = parent.getConfigurationSection(name);
            if (override != null) break;
        }
        if (override == null) return state;

        if (override.contains("enabled") && !override.getBoolean("enabled", false)) {
            return DecisionState.denied();
        }

        var phases = normalizeList(override.getStringList("phases"));
        if (!phases.isEmpty()) {
            String normalizedPhase = normalizeToken(phase);
            if (normalizedPhase.isBlank() || !phases.contains(normalizedPhase)) return DecisionState.denied();
        }

        return override.contains("enabled") && override.getBoolean("enabled", false)
                ? state.withActive(true)
                : state;
    }

    private AbilityDecision evaluatePolicy(
            IModeAbilityPolicy policy,
            AbilityId id,
            AbilityContext context,
            String owner
    ) {
        try {
            var decision = policy.evaluateAbility(id, context);
            return decision == null ? AbilityDecision.PASS : decision;
        } catch (Throwable t) {
            logger.warn("[Ability] mode policy failed owner={} ability={} err={}", owner, id, t.getMessage(), t);
            return AbilityDecision.DENY;
        }
    }

    private DecisionState applyPolicyDecision(DecisionState state, AbilityDecision decision) {
        if (decision == AbilityDecision.DENY) return DecisionState.denied();
        if (decision == AbilityDecision.ALLOW) return state.withActive(true);
        return state;
    }

    private AbilityDecision evaluatePolicy(
            IAbilityPolicy policy,
            AbilityId id,
            AbilityContext context,
            String owner
    ) {
        try {
            var decision = policy.evaluate(id, context);
            return decision == null ? AbilityDecision.PASS : decision;
        } catch (Throwable t) {
            logger.warn("[Ability] policy failed owner={} ability={} err={}", owner, id, t.getMessage(), t);
            return AbilityDecision.DENY;
        }
    }

    private Set<String> candidateKeys(String raw) {
        String normalized = normalizeToken(raw);
        String plain = plainToken(normalized);
        var out = new LinkedHashSet<String>();
        out.add(normalized);
        out.add(plain);
        out.add(normalized.replace('_', '-'));
        out.add(plain.replace('_', '-'));
        return out;
    }

    private Set<String> normalizeList(List<String> values) {
        var out = new LinkedHashSet<String>();
        if (values == null) return out;
        for (String value : values) {
            String normalized = normalizeToken(value);
            if (!normalized.isBlank()) out.add(normalized);
        }
        return out;
    }

    private String normalizeToken(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private String plainToken(String normalized) {
        int index = normalized.indexOf(':');
        return index < 0 ? normalized : normalized.substring(index + 1);
    }

    public void reload() {
        reloadSnapshot();
        for (var registered : abilities.values()) {
            try {
                registered.value().reload(config(registered.value().id()));
            } catch (Throwable t) {
                logger.warn("[Ability] reload failed for {}: {}", registered.value().id(), t.getMessage(), t);
            }
        }
    }

    public List<RegisteredAbility> registeredAbilities() {
        return List.copyOf(abilities.values());
    }

    public List<RegisteredAbilityPolicy> registeredPolicies() {
        return List.copyOf(policies);
    }

    public @Nullable RegisteredAbility registeredAbility(AbilityId id) {
        return abilities.get(id);
    }

    private record DecisionState(
            boolean hardDenied,
            boolean active
    ) {

        static DecisionState active(boolean active) {
            return new DecisionState(false, active);
        }

        static DecisionState denied() {
            return new DecisionState(true, false);
        }

        DecisionState withActive(boolean nextActive) {
            return hardDenied ? this : new DecisionState(false, nextActive);
        }

    }

}
