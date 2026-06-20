package top.ourisland.creepersiarena.defaultcontent.game.mutation.acceleratedtime;

import org.bukkit.configuration.ConfigurationSection;
import top.ourisland.creepersiarena.api.config.StrictConfig;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record AcceleratedTimeMessageSet(
        List<String> startMessages,
        List<String> endMessages
) {

    public AcceleratedTimeMessageSet {
        if (startMessages == null || startMessages.isEmpty()) {
            throw new IllegalArgumentException("accelerated-time.messages.start-messages must not be empty");
        }
        if (endMessages == null || endMessages.isEmpty()) {
            throw new IllegalArgumentException("accelerated-time.messages.end-messages must not be empty");
        }
        startMessages = List.copyOf(startMessages);
        endMessages = List.copyOf(endMessages);
    }

    public static AcceleratedTimeMessageSet fromSection(ConfigurationSection section) {
        if (section == null) return defaults();
        var defaults = defaults();
        return new AcceleratedTimeMessageSet(
                StrictConfig.stringList(
                        section,
                        "start-messages",
                        defaults.startMessages(),
                        "game.mutations.cia.accelerated_time.start-messages"
                ),
                StrictConfig.stringList(
                        section,
                        "end-messages",
                        defaults.endMessages(),
                        "game.mutations.cia.accelerated_time.end-messages"
                )
        );
    }

    public static AcceleratedTimeMessageSet defaults() {
        return new AcceleratedTimeMessageSet(defaultStartMessages(), defaultEndMessages());
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

    public String randomStart() {
        return startMessages.get(ThreadLocalRandom.current().nextInt(startMessages.size()));
    }

    public String randomEnd() {
        return endMessages.get(ThreadLocalRandom.current().nextInt(endMessages.size()));
    }

}
