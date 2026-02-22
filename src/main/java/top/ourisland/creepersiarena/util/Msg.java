package top.ourisland.creepersiarena.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static void send(@NotNull CommandSender sender, @NotNull Component message) {
        sender.sendMessage(message);
    }

    public static void send(@NotNull CommandSender sender, @NotNull String plainText) {
        sender.sendMessage(Component.text(plainText));
    }

    public static void actionBar(@NotNull CommandSender sender, @NotNull Component message) {
        sender.sendActionBar(message);
    }

    public static void actionBar(@NotNull CommandSender sender, @NotNull String plainText) {
        sender.sendActionBar(Component.text(plainText));
    }

    /* =========================
     * MiniMessage
     * ========================= */

    public static void sendMini(@NotNull CommandSender sender, @NotNull String miniMessage) {
        sender.sendMessage(MINI.deserialize(miniMessage));
    }

    public static void actionBarMini(@NotNull CommandSender sender, @NotNull String miniMessage) {
        sender.sendActionBar(MINI.deserialize(miniMessage));
    }

    /* =========================
     * Title / Subtitle
     * ========================= */

    public static void title(
            @NotNull CommandSender sender,
            @Nullable Component title,
            @Nullable Component subtitle
    ) {
        sender.showTitle(Title.title(nn(title), nn(subtitle)));
    }

    public static void title(
            @NotNull CommandSender sender,
            @Nullable Component title,
            @Nullable Component subtitle,
            @NotNull Duration fadeIn,
            @NotNull Duration stay,
            @NotNull Duration fadeOut
    ) {
        sender.showTitle(Title.title(nn(title), nn(subtitle), Title.Times.times(fadeIn, stay, fadeOut)));
    }

    public static void titleMini(
            @NotNull CommandSender sender,
            @Nullable String titleMini,
            @Nullable String subtitleMini
    ) {
        sender.showTitle(Title.title(
                MINI.deserialize(ns(titleMini)),
                MINI.deserialize(ns(subtitleMini))
        ));
    }

    public static void titleMini(
            @NotNull CommandSender sender,
            @Nullable String titleMini,
            @Nullable String subtitleMini,
            @NotNull Duration fadeIn,
            @NotNull Duration stay,
            @NotNull Duration fadeOut
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

    public static void send(@NotNull Audience audience, @NotNull Component message) {
        audience.sendMessage(message);
    }

    public static void sendMini(@NotNull Audience audience, @NotNull String miniMessage) {
        audience.sendMessage(MINI.deserialize(miniMessage));
    }

    public static void actionBar(@NotNull Audience audience, @NotNull Component message) {
        audience.sendActionBar(message);
    }

    public static void actionBarMini(@NotNull Audience audience, @NotNull String miniMessage) {
        audience.sendActionBar(MINI.deserialize(miniMessage));
    }

    public static void title(
            @NotNull Audience audience,
            @Nullable Component title,
            @Nullable Component subtitle
    ) {
        audience.showTitle(Title.title(nn(title), nn(subtitle)));
    }

    public static void titleMini(
            @NotNull Audience audience,
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

    private static @NotNull Component nn(@Nullable Component c) {
        return c == null ? Component.empty() : c;
    }

    private static @NotNull String ns(@Nullable String s) {
        return s == null ? "" : s;
    }
}
