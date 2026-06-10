package top.ourisland.creepersiarena.core.data;

import org.bukkit.configuration.file.YamlConfiguration;

public final class PlayerDataDocument {

    private final YamlConfiguration yaml;
    private boolean dirty;

    public PlayerDataDocument(YamlConfiguration yaml) {
        this.yaml = yaml == null ? new YamlConfiguration() : yaml;
    }

    public synchronized long getLong(
            String path,
            long fallback
    ) {
        return yaml.getLong(path, fallback);
    }

    public synchronized void setLong(
            String path,
            long value
    ) {
        yaml.set(path, value);
        dirty = true;
    }

    public synchronized boolean getBoolean(
            String path,
            boolean fallback
    ) {
        return yaml.getBoolean(path, fallback);
    }

    public synchronized void setBoolean(
            String path,
            boolean value
    ) {
        yaml.set(path, value);
        dirty = true;
    }

    public synchronized String getString(
            String path,
            String fallback
    ) {
        return yaml.getString(path, fallback);
    }

    public synchronized void setString(
            String path,
            String value
    ) {
        yaml.set(path, value);
        dirty = true;
    }

    public synchronized void remove(String path) {
        yaml.set(path, null);
        dirty = true;
    }

    public synchronized boolean dirty() {
        return dirty;
    }

    public synchronized String saveToStringAndClearDirty() {
        dirty = false;
        return yaml.saveToString();
    }

}
