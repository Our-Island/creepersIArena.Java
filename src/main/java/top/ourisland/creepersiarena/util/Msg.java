package top.ourisland.creepersiarena.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

/**
 * Message helper to unify output through Adventure {@link Audience}.
 *
 * <p>On Paper, {@link CommandSender} implements {@link Audience}, so any sender can be used directly.</p>
 */
public final class Msg {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private Msg() {
    }

    /* =========================
     * Basic: Component / plain
     * ========================= */

    public static void send(@NonNull CommandSender sender, @NonNull Component message) {
        sender.sendMessage(message);
    }

    public static void send(@NonNull CommandSender sender, @NonNull String plainText) {
        sender.sendMessage(Component.text(plainText));
    }

    public static void actionBar(@NonNull CommandSender sender, @NonNull Component message) {
        sender.sendActionBar(message);
    }

    public static void actionBar(@NonNull CommandSender sender, @NonNull String plainText) {
        sender.sendActionBar(Component.text(plainText));
    }

    /* =========================
     * MiniMessage
     * ========================= */

    public static void sendMini(@NonNull CommandSender sender, @NonNull String miniMessage) {
        sender.sendMessage(MINI.deserialize(miniMessage));
    }

    public static void actionBarMini(@NonNull CommandSender sender, @NonNull String miniMessage) {
        sender.sendActionBar(MINI.deserialize(miniMessage));
    }

    /* =========================
     * Title / Subtitle
     * ========================= */

    public static void title(
            @NonNull CommandSender sender,
            @Nullable Component title,
            @Nullable Component subtitle
    ) {
        sender.showTitle(Title.title(nn(title), nn(subtitle)));
    }

    public static void title(
            @NonNull CommandSender sender,
            @Nullable Component title,
            @Nullable Component subtitle,
            @NonNull Duration fadeIn,
            @NonNull Duration stay,
            @NonNull Duration fadeOut
    ) {
        sender.showTitle(Title.title(nn(title), nn(subtitle), Title.Times.times(fadeIn, stay, fadeOut)));
    }

    public static void titleMini(
            @NonNull CommandSender sender,
            @Nullable String titleMini,
            @Nullable String subtitleMini
    ) {
        sender.showTitle(Title.title(
                MINI.deserialize(ns(titleMini)),
                MINI.deserialize(ns(subtitleMini))
        ));
    }

    public static void titleMini(
            @NonNull CommandSender sender,
            @Nullable String titleMini,
            @Nullable String subtitleMini,
            @NonNull Duration fadeIn,
            @NonNull Duration stay,
            @NonNull Duration fadeOut
    ) {
        sender.showTitle(Title.title(
                MINI.deserialize(ns(titleMini)),
                MINI.deserialize(ns(subtitleMini)),
                Title.Times.times(fadeIn, stay, fadeOut)
        ));
    }

    /* =========================
     * Audience overloads
     * ========================= */

    public static void send(@NonNull Audience audience, @NonNull Component message) {
        audience.sendMessage(message);
    }

    public static void sendMini(@NonNull Audience audience, @NonNull String miniMessage) {
        audience.sendMessage(MINI.deserialize(miniMessage));
    }

    public static void actionBar(@NonNull Audience audience, @NonNull Component message) {
        audience.sendActionBar(message);
    }

    public static void actionBarMini(@NonNull Audience audience, @NonNull String miniMessage) {
        audience.sendActionBar(MINI.deserialize(miniMessage));
    }

    public static void title(
            @NonNull Audience audience,
            @Nullable Component title,
            @Nullable Component subtitle
    ) {
        audience.showTitle(Title.title(nn(title), nn(subtitle)));
    }

    public static void titleMini(
            @NonNull Audience audience,
            @Nullable String titleMini,
            @Nullable String subtitleMini
    ) {
        audience.showTitle(Title.title(
                MINI.deserialize(ns(titleMini)),
                MINI.deserialize(ns(subtitleMini))
        ));
    }

    /* =========================
     * Private helpers
     * ========================= */

    private static @NonNull Component nn(@Nullable Component c) {
        return c == null ? Component.empty() : c;
    }

    private static @NonNull String ns(@Nullable String s) {
        return s == null ? "" : s;
    }
}
