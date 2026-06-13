package top.ourisland.creepersiarena.api.economy;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

/**
 * Domain-specific globally namespaced resource identifier.
 */
public record WalletReasonId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull WalletReasonId parse(String raw) {
        return new WalletReasonId(CiaKey.parse(raw));
    }

    public static @NonNull WalletReasonId of(CiaKey key) {
        return new WalletReasonId(key);
    }

    public static @NonNull WalletReasonId of(
            CiaNamespace namespace,
            String path
    ) {
        return new WalletReasonId(CiaKey.of(namespace, path));
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

}
