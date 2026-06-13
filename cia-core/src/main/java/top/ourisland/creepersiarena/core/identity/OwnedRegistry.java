package top.ourisland.creepersiarena.core.identity;

import top.ourisland.creepersiarena.api.identity.CiaResourceId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Generic registry enforcing namespace ownership, duplicate rejection, and atomic initialized registration.
 */
public final class OwnedRegistry<K extends CiaResourceId, V> {

    private final NamespaceRegistry namespaces;
    private final Map<K, RegisteredComponent<K, V>> entries = new LinkedHashMap<>();
    private final Map<K, RegisteredComponent<K, V>> initializing = new LinkedHashMap<>();

    public OwnedRegistry(
            @lombok.NonNull NamespaceRegistry namespaces
    ) {
        this.namespaces = namespaces;
    }

    public void register(
            RegistrationOwner owner,
            K id,
            V value
    ) {
        registerInitialized(owner, id, value, _ -> {
        });
    }

    /**
     * Validates ownership and duplicate availability before initializing the value, then commits the registration only
     * when initialization succeeds.
     */
    public void registerInitialized(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull K id,
            @lombok.NonNull V value,
            @lombok.NonNull Consumer<? super V> initializer
    ) {
        registerAllInitialized(
                owner,
                List.of(new Registration<>(id, value)),
                initializer
        );
    }

    /**
     * Atomically registers a batch after validating every id. The registry monitor is held for the complete operation,
     * so another thread cannot claim an id between validation and commit. No entry is committed unless every
     * initializer completes successfully.
     */
    public synchronized void registerAllInitialized(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull Collection<Registration<K, V>> registrations,
            @lombok.NonNull Consumer<? super V> initializer
    ) {
        ensureNoInitializationInProgress("start another registration batch");

        var requested = List.copyOf(registrations);
        LinkedHashMap<K, RegisteredComponent<K, V>> proposed = new LinkedHashMap<>();

        for (var registration : requested) {
            namespaces.requireOwnership(owner, registration.id().namespace());
            var candidate = new RegisteredComponent<>(owner, registration.id(), registration.value());
            var duplicate = proposed.putIfAbsent(registration.id(), candidate);
            if (duplicate != null) {
                throw duplicate(registration.id(), duplicate, candidate);
            }

            var existing = claimed(registration.id());
            if (existing != null) {
                throw duplicate(registration.id(), existing, candidate);
            }
        }

        initializing.putAll(proposed);
        try {
            requested.forEach(registration -> initializer.accept(registration.value()));
            entries.putAll(proposed);
        } finally {
            proposed.keySet().forEach(initializing::remove);
        }
    }

    private void ensureNoInitializationInProgress(String operation) {
        if (!initializing.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot " + operation + " while registrations are being initialized: "
                            + initializing.keySet().stream()
                            .map(CiaResourceId::asString)
                            .toList()
            );
        }
    }

    private DuplicateRegistrationException duplicate(
            K id,
            RegisteredComponent<K, V> existing,
            RegisteredComponent<K, V> candidate
    ) {
        return new DuplicateRegistrationException(
                id.asString(),
                existing.owner(),
                existing.value(),
                candidate.owner(),
                candidate.value()
        );
    }

    private RegisteredComponent<K, V> claimed(K id) {
        var existing = entries.get(id);
        return existing != null ? existing : initializing.get(id);
    }

    public synchronized void replaceOwner(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull Collection<Registration<K, V>> replacements
    ) {
        ensureNoInitializationInProgress("replace an owner snapshot");

        LinkedHashMap<K, RegisteredComponent<K, V>> proposed = new LinkedHashMap<>();
        for (var replacement : replacements) {
            namespaces.requireOwnership(owner, replacement.id().namespace());
            var candidate = new RegisteredComponent<>(owner, replacement.id(), replacement.value());
            var duplicate = proposed.putIfAbsent(replacement.id(), candidate);
            if (duplicate != null) {
                throw duplicate(replacement.id(), duplicate, candidate);
            }
            var existing = entries.get(replacement.id());
            if (existing != null && !existing.owner().equals(owner)) {
                throw duplicate(replacement.id(), existing, candidate);
            }
        }

        var next = entries.entrySet().stream()
                .filter(entry -> !entry.getValue().owner().equals(owner))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
        next.putAll(proposed);
        entries.clear();
        entries.putAll(next);
    }

    public synchronized void replaceAllValidated(
            @lombok.NonNull Collection<RegisteredComponent<K, V>> replacements
    ) {
        ensureNoInitializationInProgress("replace the registry snapshot");

        LinkedHashMap<K, RegisteredComponent<K, V>> proposed = new LinkedHashMap<>();
        for (var replacement : replacements) {
            namespaces.requireOwnership(replacement.owner(), replacement.id().namespace());
            var duplicate = proposed.putIfAbsent(replacement.id(), replacement);
            if (duplicate != null) {
                throw duplicate(replacement.id(), duplicate, replacement);
            }
        }
        entries.clear();
        entries.putAll(proposed);
    }

    public synchronized RegisteredComponent<K, V> get(K id) {
        return entries.get(id);
    }

    public synchronized List<RegisteredComponent<K, V>> entries() {
        return List.copyOf(entries.values());
    }

    public synchronized Collection<V> values() {
        return entries.values().stream()
                .map(RegisteredComponent::value)
                .toList();
    }

    public synchronized void clear() {
        ensureNoInitializationInProgress("clear the registry");
        entries.clear();
    }

    public synchronized void clearOwner(RegistrationOwner owner) {
        ensureNoInitializationInProgress("clear an owner");
        entries.entrySet()
                .removeIf(entry -> entry.getValue().owner().equals(owner));
    }

    public record Registration<K, V>(
            @lombok.NonNull K id,
            @lombok.NonNull V value
    ) {

    }

}
