package top.ourisland.creepersiarena.core.ability;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.*;
import top.ourisland.creepersiarena.api.config.StrictConfig;
import top.ourisland.creepersiarena.api.game.mode.IModeAbilityPolicy;
import top.ourisland.creepersiarena.api.identity.CiaConfigPaths;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.config.ConfigManager;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.identity.OwnedRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class AbilityService implements IAbilityRegistry, IAbilityAdmin {

    private final Logger logger;
    private final ConfigManager configManager;
    private final Supplier<GameManager> gameManager;
    private final OwnedRegistry<AbilityId, IAbility> abilities;
    private final List<RegisteredAbilityPolicy> policies = new CopyOnWriteArrayList<>();
    private final Map<AbilityId, Boolean> adminOverrides = new ConcurrentHashMap<>();
    private volatile YamlConfiguration configSnapshot = new YamlConfiguration();

    public AbilityService(
            @lombok.NonNull Logger logger,
            @lombok.NonNull ConfigManager configManager,
            Supplier<GameManager> gameManager,
            NamespaceRegistry namespaces
    ) {
        this.logger = logger;
        this.configManager = configManager;
        this.gameManager = gameManager;
        this.abilities = new OwnedRegistry<>(namespaces);
        reloadSnapshot();
    }

    private void reloadSnapshot() {
        var path = configManager.dataDir().resolve("config.yml");
        configSnapshot = YamlConfiguration.loadConfiguration(path.toFile());
    }

    @Override
    public void registerAbility(
            RegistrationOwner owner,
            IAbility @NonNull ... abilityProviders
    ) {
        registerAbilities(owner, List.of(abilityProviders));
    }

    @Override
    public void registerAbility(
            RegistrationOwner owner,
            @lombok.NonNull IAbility ability
    ) {
        registerAbilities(owner, List.of(ability));
    }

    @Override
    public void registerPolicy(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull IAbilityPolicy policy
    ) {
        policies.add(new RegisteredAbilityPolicy(owner, policy));
        logger.info("[Ability] Registered policy {} by {}.", policy.getClass().getSimpleName(), owner.extensionId());
    }

    private void registerAbilities(
            RegistrationOwner owner,
            Collection<IAbility> abilityProviders
    ) {
        var requested = List.copyOf(abilityProviders);
        var registrations = requested.stream()
                .map(ability -> new OwnedRegistry.Registration<>(ability.id(), ability))
                .toList();

        abilities.registerAllInitialized(
                owner,
                registrations,
                ability -> ability.reload(config(ability.id()))
        );
        requested.forEach(ability -> logger.info(
                "[Ability] Registered {} by {}.",
                ability.id(),
                owner.extensionId()
        ));
    }

    public void clearOwner(RegistrationOwner owner) {
        var removed = abilities.entries().stream()
                .filter(entry -> entry.owner() == owner)
                .map(RegisteredComponent::id)
                .toList();
        abilities.clearOwner(owner);
        policies.removeIf(policy -> policy.owner() == owner);
        removed.forEach(adminOverrides::remove);
    }

    @Override
    public void setAdminEnabled(
            @lombok.NonNull AbilityId abilityId,
            boolean enabled
    ) {
        if (enabled) adminOverrides.remove(abilityId);
        else adminOverrides.put(abilityId, false);
    }

    @Override
    public boolean adminEnabled(AbilityId abilityId) {
        return !Boolean.FALSE.equals(adminOverrides.get(abilityId));
    }

    @Override
    public @NonNull List<AbilityId> abilityIds() {
        var out = abilities.entries().stream()
                .map(RegisteredComponent::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var root = StrictConfig.section(configSnapshot, "game.abilities", "game.abilities");

        if (root != null) {
            for (var namespaceValue : root.getKeys(false)) {
                var namespaceSection = StrictConfig.section(
                        root,
                        namespaceValue,
                        "game.abilities." + namespaceValue
                );
                if (namespaceSection == null) {
                    throw new IllegalArgumentException("Missing ability namespace section game.abilities." + namespaceValue);
                }

                CiaNamespace namespace;
                try {
                    namespace = CiaNamespace.parse(namespaceValue);
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException(
                            "Invalid ability namespace at game.abilities." + namespaceValue,
                            exception
                    );
                }
                collectConfiguredIds(namespace, namespaceSection, "", out);
            }
        }
        return out.stream()
                .sorted(Comparator.comparing(AbilityId::asString))
                .toList();
    }

    @Override
    public @NonNull IAbilityConfigView config(@lombok.NonNull AbilityId abilityId) {
        String path = "game.abilities." + CiaConfigPaths.section(abilityId);
        return new BukkitAbilityConfigView(abilityId, StrictConfig.section(configSnapshot, path, path));
    }

    @Override
    public boolean isEnabled(AbilityId id, AbilityContext context) {
        if (id == null) return false;

        var view = config(id);
        var registered = abilities.get(id) != null;
        if (!registered && !view.exists()) return false;

        var section = view.section();
        if (section == null || !view.enabled(false) || !adminEnabled(id)) return false;

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
            @Nullable AbilityContext context
    ) {
        if (context == null) return state;

        if (context.modeId() != null) {
            state = applyOverride(
                    state,
                    StrictConfig.section(
                            section,
                            "modes." + CiaConfigPaths.section(context.modeId()),
                            section.getCurrentPath() + ".modes." + CiaConfigPaths.section(context.modeId())
                    ),
                    context.phase()
            );
        }
        if (state.hardDenied()) return state;

        if (context.arenaId() != null) {
            state = applyOverride(
                    state,
                    StrictConfig.section(
                            section,
                            "arenas." + context.arenaId().value(),
                            section.getCurrentPath() + ".arenas." + context.arenaId().value()
                    ),
                    context.phase()
            );
        }
        return state;
    }

    private DecisionState applyModePolicies(
            AbilityId id,
            AbilityContext context,
            DecisionState state
    ) {
        var manager = gameManager == null ? null : gameManager.get();
        if (manager == null) return state;

        var out = state;
        Object[] candidates = {manager.rules(), manager.timeline(), manager.playerFlow()};
        for (var candidate : candidates) {
            if (!(candidate instanceof IModeAbilityPolicy policy)) continue;
            out = applyPolicyDecision(out, evaluatePolicy(policy, id, context, "mode"));
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
            out = applyPolicyDecision(
                    out,
                    evaluatePolicy(registered.value(), id, context, registered.owner().extensionId().value())
            );
            if (out.hardDenied()) return out;
        }
        return out;
    }

    private DecisionState applyOverride(
            DecisionState state,
            @Nullable ConfigurationSection override,
            @Nullable String phase
    ) {
        if (override == null) return state;
        var enabled = override.contains("enabled")
                ? StrictConfig.bool(override, "enabled", false, override.getCurrentPath() + ".enabled")
                : null;
        if (Boolean.FALSE.equals(enabled)) return DecisionState.denied();

        var phases = StrictConfig.stringList(
                override,
                "phases",
                List.of(),
                override.getCurrentPath() + ".phases"
        );
        if (!phases.isEmpty() && (phase == null || !phases.contains(phase))) return DecisionState.denied();
        return Boolean.TRUE.equals(enabled) ? state.withActive(true) : state;
    }

    private DecisionState applyPolicyDecision(DecisionState state, AbilityDecision decision) {
        if (decision == AbilityDecision.DENY) return DecisionState.denied();
        if (decision == AbilityDecision.ALLOW) return state.withActive(true);
        return state;
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
        } catch (Throwable throwable) {
            logger.warn("[Ability] mode policy failed owner={} ability={} err={}", owner, id, throwable.getMessage(), throwable);
            return AbilityDecision.DENY;
        }
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
        } catch (Throwable throwable) {
            logger.warn("[Ability] policy failed owner={} ability={} err={}", owner, id, throwable.getMessage(), throwable);
            return AbilityDecision.DENY;
        }
    }

    public void reload() {
        reloadSnapshot();
        for (var registered : abilities.entries()) {
            registered.value().reload(config(registered.id()));
        }
    }

    private void collectConfiguredIds(
            CiaNamespace namespace,
            ConfigurationSection section,
            String resourcePath,
            Set<AbilityId> out
    ) {
        if (!resourcePath.isEmpty() && isAbilitySection(section)) {
            try {
                out.add(AbilityId.of(namespace, resourcePath));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Invalid ability configuration path game.abilities."
                                + namespace.value() + "." + resourcePath.replace('/', '.'),
                        exception
                );
            }
            return;
        }
        for (var key : section.getKeys(false)) {
            var child = StrictConfig.section(
                    section,
                    key,
                    section.getCurrentPath() + "." + key
            );
            if (child == null) {
                throw new IllegalArgumentException("Missing ability section at " + section.getCurrentPath() + "." + key);
            }
            var childPath = resourcePath.isEmpty() ? key : resourcePath + "/" + key;
            collectConfiguredIds(namespace, child, childPath, out);
        }
    }

    private boolean isAbilitySection(ConfigurationSection section) {
        return section.contains("enabled") || section.contains("default-active") || section.isConfigurationSection("settings");
    }

    public @NonNull List<RegisteredAbility> registeredAbilities() {
        return abilities.entries().stream()
                .map(entry -> new RegisteredAbility(entry.owner(), entry.id(), entry.value()))
                .toList();
    }

    public @NonNull List<RegisteredAbilityPolicy> registeredPolicies() {
        return List.copyOf(policies);
    }

    public @Nullable RegisteredAbility registeredAbility(AbilityId id) {
        RegisteredComponent<AbilityId, IAbility> registered = abilities.get(id);
        return registered == null
                ? null
                : new RegisteredAbility(registered.owner(), registered.id(), registered.value());
    }

    private record DecisionState(
            boolean hardDenied,
            boolean active
    ) {

        static @NonNull DecisionState active(boolean active) {
            return new DecisionState(false, active);
        }

        static @NonNull DecisionState denied() {
            return new DecisionState(true, false);
        }

        DecisionState withActive(boolean nextActive) {
            return hardDenied ? this : new DecisionState(false, nextActive);
        }

    }

}
