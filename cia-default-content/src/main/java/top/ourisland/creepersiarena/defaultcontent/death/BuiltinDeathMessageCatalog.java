package top.ourisland.creepersiarena.defaultcontent.death;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import top.ourisland.creepersiarena.api.game.death.DeathCauseId;
import top.ourisland.creepersiarena.api.game.death.DeathMessageLabel;
import top.ourisland.creepersiarena.api.game.death.StandardDeathCauses;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class BuiltinDeathMessageCatalog {

    private static final String RESOURCE_PATH = "default-content/death-messages.yml";
    private static final String GENERIC_KEY = StandardDeathCauses.GENERIC.toString();

    private final Map<DeathMessageLabel, LabelEntry> labels;
    private final Map<String, LabelEntry> namedLabels;
    private final Map<String, MessagePool> messages;

    private BuiltinDeathMessageCatalog(
            Map<DeathMessageLabel, LabelEntry> labels,
            Map<String, LabelEntry> namedLabels,
            Map<String, MessagePool> messages
    ) {
        this.labels = labels;
        this.namedLabels = namedLabels;
        this.messages = messages;
    }

    public static BuiltinDeathMessageCatalog load(Path file, ClassLoader classLoader) {
        var yaml = loadYaml(file, classLoader);
        return new BuiltinDeathMessageCatalog(loadLabels(yaml), loadNamedLabels(yaml), loadMessages(yaml));
    }

    private static YamlConfiguration loadYaml(Path file, ClassLoader classLoader) {
        if (file != null && Files.exists(file)) {
            return YamlConfiguration.loadConfiguration(file.toFile());
        }

        var yaml = new YamlConfiguration();
        try (var input = classLoader.getResourceAsStream(RESOURCE_PATH)) {
            if (input == null) return yaml;
            try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                yaml.load(reader);
            }
        } catch (Exception _) {
            return new YamlConfiguration();
        }
        return yaml;
    }

    private static Map<DeathMessageLabel, LabelEntry> loadLabels(YamlConfiguration yaml) {
        var loaded = new EnumMap<DeathMessageLabel, LabelEntry>(DeathMessageLabel.class);
        Map<String, LabelEntry> named = loadNamedLabels(yaml);

        for (var label : DeathMessageLabel.values()) {
            String key = label.name().toLowerCase(Locale.ROOT);
            LabelEntry entry = named.get(key);
            if (entry != null) loaded.put(label, entry);
        }
        return loaded;
    }

    private static Map<String, LabelEntry> loadNamedLabels(YamlConfiguration yaml) {
        var loaded = new HashMap<String, LabelEntry>();
        ConfigurationSection section = yaml.getConfigurationSection("labels");
        if (section == null) return loaded;

        for (String key : section.getKeys(false)) {
            String normalized = key.toLowerCase(Locale.ROOT);
            String text = section.getString(key + ".text", key);
            String color = section.getString(key + ".color", "gray");
            loaded.put(normalized, new LabelEntry(text, color));
        }
        return loaded;
    }

    private static Map<String, MessagePool> loadMessages(YamlConfiguration yaml) {
        var loaded = new HashMap<String, MessagePool>();
        ConfigurationSection section = yaml.getConfigurationSection("messages");
        if (section == null) return loaded;

        for (var key : section.getKeys(false)) {
            List<String> killer = section.getStringList(key + ".killer");
            List<String> solo = section.getStringList(key + ".solo");
            loaded.put(key, new MessagePool(List.copyOf(killer), List.copyOf(solo)));
        }
        return loaded;
    }

    public LabelEntry label(DeathMessageLabel label) {
        LabelEntry entry = labels.get(label);
        if (entry != null) return entry;

        String key = label.name().toLowerCase(Locale.ROOT);
        entry = namedLabels.get(key);
        if (entry != null) return entry;

        return new LabelEntry(key, "gray");
    }

    public LabelEntry namedLabel(String key, String fallbackText) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        LabelEntry entry = namedLabels.get(normalized);
        if (entry != null) return entry;
        return new LabelEntry(fallbackText == null ? normalized : fallbackText, "gray");
    }

    public List<String> templates(DeathCauseId causeId, boolean hasKiller) {
        MessagePool pool = messages.get(causeId.toString());
        if (pool == null) pool = messages.get(GENERIC_KEY);
        if (pool == null) return fallbackTemplates(hasKiller);

        List<String> templates = hasKiller ? pool.killer() : pool.solo();
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
