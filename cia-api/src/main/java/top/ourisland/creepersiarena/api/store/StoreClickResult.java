package top.ourisland.creepersiarena.api.store;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record StoreClickResult(
        StoreClickResultType type,
        @Nullable Component message,
        boolean refresh
) {

    public StoreClickResult {
        type = type == null ? StoreClickResultType.NO_OP : type;
    }

    public static @NonNull StoreClickResult noOp() {
        return new StoreClickResult(StoreClickResultType.NO_OP, null, false);
    }

    public static @NonNull StoreClickResult disabled(Component message) {
        return new StoreClickResult(StoreClickResultType.DISABLED, message, true);
    }

    public static @NonNull StoreClickResult selected(Component message) {
        return new StoreClickResult(StoreClickResultType.SELECTED, message, true);
    }

    public static @NonNull StoreClickResult purchased(Component message) {
        return new StoreClickResult(StoreClickResultType.PURCHASED, message, true);
    }

    public static @NonNull StoreClickResult notEnoughCurrency(Component message) {
        return new StoreClickResult(StoreClickResultType.NOT_ENOUGH_CURRENCY, message, true);
    }

    public static @NonNull StoreClickResult error(Component message) {
        return new StoreClickResult(StoreClickResultType.ERROR, message, true);
    }

}
