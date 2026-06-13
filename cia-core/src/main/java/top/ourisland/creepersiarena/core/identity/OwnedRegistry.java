package top.ourisland.creepersiarena.core.identity;

import top.ourisland.creepersiarena.api.identity.CiaResourceId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic registry enforcing namespace ownership and duplicate rejection.
 */
public final class OwnedRegistry<K extends CiaResourceId, V> {

    private final NamespaceRegistry namespaces;
    private final Map<K, RegisteredComponent<K, V>> entries = new LinkedHashMap<>();

    public OwnedRegistry(
            @lombok.NonNull NamespaceRegistry namespaces
    ) {
        this.namespaces = namespaces;
    }

    public synchronized void register(
            RegistrationOwner owner,
            K id,
            V value
    ) {
        namespaces.requireOwnership(owner, id.namespace());
        var replacement = new RegisteredComponent<>(owner, id, value);
        var existing = entries.putIfAbsent(id, replacement);
        if (existing != null) {
            throw new DuplicateRegistrationException(id.asString(), existing.owner(), owner);
        }
    }

    public synchronized void replaceOwner(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull Collection<Registration<K, V>> replacements
    ) {
        LinkedHashMap<K, RegisteredComponent<K, V>> proposed = new LinkedHashMap<>();
        for (var replacement : replacements) {
            namespaces.requireOwnership(owner, replacement.id().namespace());
            var duplicate = proposed.putIfAbsent(
                    replacement.id(),
                    new RegisteredComponent<>(owner, replacement.id(), replacement.value())
            );
            if (duplicate != null) {
                throw new DuplicateRegistrationException(
                        replacement.id().asString(),
                        duplicate.owner(),
                        owner
                );
            }
            var existing = entries.get(replacement.id());
            if (existing != null && !existing.owner().equals(owner)) {
                throw new DuplicateRegistrationException(
                        replacement.id().asString(),
                        existing.owner(),
                        owner
                );
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
        entries.clear();
    }

    public synchronized void clearOwner(RegistrationOwner owner) {
        entries.entrySet()
                .removeIf(entry -> entry.getValue().owner().equals(owner));
    }

    public record Registration<K, V>(
            @lombok.NonNull K id,
            @lombok.NonNull V value
    ) {

    }

}
