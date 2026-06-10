package top.ourisland.creepersiarena.api.economy;

import org.jspecify.annotations.NonNull;

public record WalletChangeReason(
        String category,
        String detail
) {

    public WalletChangeReason {
        category = normalize(category, "unknown");
        detail = normalize(detail, "");
    }

    private static String normalize(
            String raw,
            String fallback
    ) {
        if (raw == null || raw.isBlank()) return fallback;
        return raw.trim();
    }

    public static @NonNull WalletChangeReason command(String detail) {
        return new WalletChangeReason("command", detail);
    }

    public static @NonNull WalletChangeReason purchase(String detail) {
        return new WalletChangeReason("purchase", detail);
    }

    public static @NonNull WalletChangeReason reward(
            String category,
            String detail
    ) {
        return new WalletChangeReason(category, detail);
    }

}
