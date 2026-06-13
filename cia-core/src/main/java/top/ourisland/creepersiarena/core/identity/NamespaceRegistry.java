package top.ourisland.creepersiarena.core.identity;

import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Runtime authority for exclusive extension namespace claims.
 */
public final class NamespaceRegistry {

    private final Map<CiaNamespace, RegistrationOwner> owners = new LinkedHashMap<>();

    public NamespaceRegistry() {
        owners.put(RegistrationOwner.CORE.namespace(), RegistrationOwner.CORE);
    }

    public synchronized void claim(@lombok.NonNull RegistrationOwner owner) {
        var namespace = owner.namespace();
        if ("minecraft".equals(namespace.value())) {
            throw new IllegalArgumentException("The minecraft namespace is reserved");
        }
        if (RegistrationOwner.CORE.namespace().equals(namespace) && !RegistrationOwner.CORE.equals(owner)) {
            throw new IllegalArgumentException("The core namespace is reserved");
        }

        var existing = owners.putIfAbsent(namespace, owner);
        if (existing != null && !existing.equals(owner)) {
            throw new IllegalStateException(
                    "Extension \"%s\" cannot claim namespace \"%s\"; it is already owned by \"%s\".".formatted(
                            owner.extensionId().value(),
                            namespace.value(),
                            existing.extensionId().value()
                    )
            );
        }
    }

    public synchronized void release(@lombok.NonNull RegistrationOwner owner) {
        if (RegistrationOwner.CORE.equals(owner)) return;
        owners.remove(owner.namespace(), owner);
    }

    public synchronized void requireOwnership(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull CiaNamespace namespace
    ) {
        var registered = owners.get(namespace);
        if (!owner.equals(registered)) {
            throw new IllegalArgumentException(
                    "Registration owner %s does not own namespace %s".formatted(owner, namespace.value())
            );
        }
    }

    public synchronized RegistrationOwner owner(CiaNamespace namespace) {
        return owners.get(namespace);
    }

    public synchronized Map<CiaNamespace, RegistrationOwner> claims() {
        return Map.copyOf(owners);
    }

}
