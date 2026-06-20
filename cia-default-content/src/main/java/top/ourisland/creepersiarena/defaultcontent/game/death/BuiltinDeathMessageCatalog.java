package top.ourisland.creepersiarena.defaultcontent.game.death;

import org.bukkit.configuration.file.YamlConfiguration;
import top.ourisland.creepersiarena.api.config.StrictConfig;
import top.ourisland.creepersiarena.api.game.death.DeathCauseId;
import top.ourisland.creepersiarena.api.game.death.DeathMessageLabel;
import top.ourisland.creepersiarena.api.game.death.StandardDeathCauses;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class BuiltinDeathMessageCatalog {

    private static final String RESOURCE_PATH = "default-content/death-messages.yml";
    private static final DeathCauseId GENERIC_KEY = StandardDeathCauses.GENERIC;
    private static final List<DeathCauseId> REQUIRED_STANDARD_CAUSES = List.of(
            StandardDeathCauses.GENERIC,
            StandardDeathCauses.CONTACT,
            StandardDeathCauses.DIRECT_HIT,
            StandardDeathCauses.VOID,
            StandardDeathCauses.FIRE,
            StandardDeathCauses.FALL,
            StandardDeathCauses.DROWNING
    );
    private static final Set<String> COLORS = Set.of(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"
    );

    private final Map<DeathMessageLabel, LabelEntry> labels;
    private final Map<String, LabelEntry> namedLabels;
    private final Map<DeathCauseId, MessagePool> messages;

    private BuiltinDeathMessageCatalog(
            Map<DeathMessageLabel, LabelEntry> labels,
            Map<String, LabelEntry> namedLabels,
            Map<DeathCauseId, MessagePool> messages
    ) {
        this.labels = labels;
        this.namedLabels = namedLabels;
        this.messages = messages;
    }

    public static BuiltinDeathMessageCatalog load(Path file, ClassLoader classLoader) {
        var yaml = loadYaml(file, classLoader);
        var namedLabels = loadNamedLabels(yaml);
        return new BuiltinDeathMessageCatalog(loadLabels(namedLabels), namedLabels, loadMessages(yaml));
    }

    private static YamlConfiguration loadYaml(Path file, ClassLoader classLoader) {
        var yaml = new YamlConfiguration();
        try {
            if (file != null && Files.exists(file)) {
                yaml.load(file.toFile());
                return yaml;
            }

            try (var input = classLoader.getResourceAsStream(RESOURCE_PATH)) {
                if (input == null) {
                    throw new IllegalStateException("Missing bundled death-message resource " + RESOURCE_PATH);
                }
                try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                    yaml.load(reader);
                }
            }
            return yaml;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to load death messages from " + (file == null ? RESOURCE_PATH : file),
                    exception
            );
        }
    }

    private static Map<String, LabelEntry> loadNamedLabels(YamlConfiguration yaml) {
        var section = StrictConfig.section(yaml, "labels", "labels");
        if (section == null) throw new IllegalArgumentException("Missing required configuration section labels");

        var loaded = new HashMap<String, LabelEntry>();
        for (String key : section.getKeys(false)) {
            if (!key.equals(key.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Invalid label id at labels." + key + ": expected lowercase id");
            }
            var path = "labels." + key;
            var label = StrictConfig.section(section, key, path);
            if (label == null) throw new IllegalArgumentException("Missing label section at " + path);
            var text = StrictConfig.string(label, "text", key, path + ".text");
            var color = StrictConfig.string(label, "color", "gray", path + ".color");
            if (!COLORS.contains(color)) {
                throw new IllegalArgumentException("Invalid value at " + path + ".color: unknown named color " + color);
            }
            loaded.put(key, new LabelEntry(text, color));
        }
        return Map.copyOf(loaded);
    }

    private static Map<DeathMessageLabel, LabelEntry> loadLabels(Map<String, LabelEntry> named) {
        var loaded = new EnumMap<DeathMessageLabel, LabelEntry>(DeathMessageLabel.class);
        for (var label : DeathMessageLabel.values()) {
            var key = label.name().toLowerCase(Locale.ROOT);
            var entry = named.get(key);
            if (entry != null) loaded.put(label, entry);
        }
        return Map.copyOf(loaded);
    }

    private static Map<DeathCauseId, MessagePool> loadMessages(YamlConfiguration yaml) {
        var section = StrictConfig.section(yaml, "messages", "messages");
        if (section == null) throw new IllegalArgumentException("Missing required configuration section messages");

        var loaded = new HashMap<DeathCauseId, MessagePool>();
        for (var rawId : section.getKeys(false)) {
            var causeId = DeathCauseId.parse(rawId);
            var path = "messages." + rawId;
            var messageSection = StrictConfig.section(section, rawId, path);
            if (messageSection == null) throw new IllegalArgumentException("Missing message section at " + path);
            var killer = StrictConfig.stringList(messageSection, "killer", List.of(), path + ".killer");
            var solo = StrictConfig.stringList(messageSection, "solo", List.of(), path + ".solo");
            if (killer.isEmpty() && solo.isEmpty()) {
                throw new IllegalArgumentException("Message pool at " + path + " must contain killer or solo templates");
            }
            var previous = loaded.put(causeId, new MessagePool(killer, solo));
            if (previous != null) throw new IllegalArgumentException("Duplicate death-message id " + causeId);
        }

        var missingStandardPools = REQUIRED_STANDARD_CAUSES.stream()
                .filter(id -> !loaded.containsKey(id))
                .toList();
        if (!missingStandardPools.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required standard death-message pools: " + missingStandardPools.stream()
                            .map(DeathCauseId::asString)
                            .collect(Collectors.joining(", "))
            );
        }
        return Map.copyOf(loaded);
    }

    public LabelEntry label(DeathMessageLabel label) {
        var entry = labels.get(label);
        if (entry != null) return entry;

        var key = label.name().toLowerCase(Locale.ROOT);
        entry = namedLabels.get(key);
        if (entry != null) return entry;
        return new LabelEntry(key, "gray");
    }

    public LabelEntry namedLabel(String key, String fallbackText) {
        var normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        var entry = namedLabels.get(normalized);
        if (entry != null) return entry;
        return new LabelEntry(fallbackText == null ? normalized : fallbackText, "gray");
    }

    public List<String> templates(DeathCauseId causeId, boolean hasKiller) {
        var pool = messages.get(causeId);
        if (pool == null) pool = messages.get(GENERIC_KEY);
        if (pool == null) return fallbackTemplates(hasKiller);

        var templates = hasKiller ? pool.killer() : pool.solo();
        if (!templates.isEmpty()) return templates;
        templates = hasKiller ? pool.solo() : pool.killer();
        return templates.isEmpty() ? fallbackTemplates(hasKiller) : templates;
    }

    private static List<String> fallbackTemplates(boolean hasKiller) {
        if (hasKiller) return List.of("{label}{victim} 被 {killer} 击败了");
        return List.of("{death}{victim} 倒下了");
    }

    public record LabelEntry(
            String text,
            String color
    ) {

    }

    private record MessagePool(
            List<String> killer,
            List<String> solo
    ) {

    }

}
