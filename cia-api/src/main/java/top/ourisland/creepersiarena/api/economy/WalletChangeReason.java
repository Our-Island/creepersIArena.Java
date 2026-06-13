package top.ourisland.creepersiarena.api.economy;

import org.jspecify.annotations.NonNull;

public record WalletChangeReason(
        @lombok.NonNull WalletReasonId id,
        String detail
) {

    public static final WalletReasonId
            COMMAND = WalletReasonId.parse("core:command"),
            PURCHASE = WalletReasonId.parse("core:purchase"),
            UNKNOWN = WalletReasonId.parse("core:unknown");

    public WalletChangeReason {
        detail = detail == null ? "" : detail;
    }

    public static @NonNull WalletChangeReason command(String detail) {
        return new WalletChangeReason(COMMAND, detail);
    }

    public static @NonNull WalletChangeReason purchase(String detail) {
        return new WalletChangeReason(PURCHASE, detail);
    }

    public static @NonNull WalletChangeReason reward(
            WalletReasonId id,
            String detail
    ) {
        return new WalletChangeReason(id, detail);
    }

}
