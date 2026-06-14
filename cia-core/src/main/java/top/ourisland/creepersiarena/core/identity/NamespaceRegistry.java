package top.ourisland.creepersiarena.core.identity;

import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runtime authority for exclusive extension namespace claims.
 */
public final class NamespaceRegistry {

    private final RegistrationOwner coreOwner;
    private final Map<CiaNamespace, RegistrationOwner> owners = new LinkedHashMap<>();

    public NamespaceRegistry() {
        this.coreOwner = RegistrationOwnerAuthority.core();
        owners.put(coreOwner.namespace(), coreOwner);
    }

    public RegistrationOwner coreOwner() {
        return coreOwner;
    }

    public synchronized void claim(@lombok.NonNull RegistrationOwner owner) {
        RegistrationOwnerAuthority.requireRuntimeIssued(owner);
        var namespace = owner.namespace();
        if ("minecraft".equals(namespace.value())) {
            throw new IllegalArgumentException("The minecraft namespace is reserved");
        }
        if (coreOwner.namespace().equals(namespace) && owner != coreOwner) {
            throw new IllegalArgumentException("The core namespace is reserved");
        }

        var existing = owners.putIfAbsent(namespace, owner);
        if (existing != null && existing != owner) {
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
        RegistrationOwnerAuthority.requireRuntimeIssued(owner);
        if (owner == coreOwner) return;
        var registered = owners.get(owner.namespace());
        if (registered == owner) {
            owners.remove(owner.namespace());
        }
    }

    public synchronized void requireOwnership(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull CiaNamespace namespace
    ) {
        RegistrationOwnerAuthority.requireRuntimeIssued(owner);
        var registered = owners.get(namespace);
        if (registered != owner) {
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
