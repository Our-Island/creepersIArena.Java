package top.ourisland.creepersiarena.game.mutation.effect.acceleratedtime;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record AcceleratedTimeMessageSet(
        List<String> startMessages,
        List<String> endMessages
) {

    public AcceleratedTimeMessageSet {
        startMessages = List.copyOf(startMessages == null || startMessages.isEmpty()
                ? defaultStartMessages()
                : startMessages);
        endMessages = List.copyOf(endMessages == null || endMessages.isEmpty()
                ? defaultEndMessages()
                : endMessages);
    }

    public static AcceleratedTimeMessageSet defaults() {
        return new AcceleratedTimeMessageSet(defaultStartMessages(), defaultEndMessages());
    }

    public static AcceleratedTimeMessageSet fromSection(ConfigurationSection section) {
        if (section == null) return defaults();
        var defaults = defaults();
        return new AcceleratedTimeMessageSet(
                stringListOrDefault(section, "start-messages", defaults.startMessages()),
                stringListOrDefault(section, "end-messages", defaults.endMessages())
        );
    }

    public String randomStart() {
        return startMessages.get(ThreadLocalRandom.current().nextInt(startMessages.size()));
    }

    public String randomEnd() {
        return endMessages.get(ThreadLocalRandom.current().nextInt(endMessages.size()));
    }

    private static List<String> stringListOrDefault(
            ConfigurationSection section,
            String path,
            List<String> fallback
    ) {
        var list = section.getStringList(path);
        return list.isEmpty() ? fallback : list;
    }

    private static List<String> defaultStartMessages() {
        return List.of(
                "🕸 神秘的力量开始操控魔法",
                "🕸 一股神秘的力量...",
                "🕸 时间要开始加速了？还好，只加速了一部分...吗？",
                "🕸 快快快！太慢了太慢了！"
        );
    }

    private static List<String> defaultEndMessages() {
        return List.of(
                "🕸 神秘的力量消失了...",
                "🕸 时间流速恢复正常...",
                "🕸 你感觉身体变慢了...",
                "🕸 好像世界变慢了？"
        );
    }

}
