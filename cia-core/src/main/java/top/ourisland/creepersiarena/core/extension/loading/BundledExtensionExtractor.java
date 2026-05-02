package top.ourisland.creepersiarena.core.extension.loading;

import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;

public final class BundledExtensionExtractor {

    private static final String BUNDLED_RESOURCE_ROOT = "META-INF/cia/bundled-extensions";
    private static final String DEFAULT_CONTENT_FILE = "cia-default-content.cia.jar";
    private static final String CACHE_FILE = "bundled-extensions.properties";

    private final JavaPlugin plugin;
    private final Logger log;
    private final Path extensionsDirectory;
    private final Path cacheFile;

    public BundledExtensionExtractor(
            @lombok.NonNull JavaPlugin plugin,
            @lombok.NonNull Logger log
    ) {
        this.plugin = plugin;
        this.log = log;
        var dataDir = plugin.getDataFolder().toPath();
        this.extensionsDirectory = dataDir.resolve("extensions");
        this.cacheFile = dataDir.resolve("extension-cache").resolve(CACHE_FILE);
    }

    public void extractAll() {
        extract(DEFAULT_CONTENT_FILE);
    }

    private void extract(String fileName) {
        var resourcePath = BUNDLED_RESOURCE_ROOT + "/" + fileName;
        try (var input = plugin.getResource(resourcePath)) {
            if (input == null) {
                log.warn("[Extension] Bundled extension resource not found: {}", resourcePath);
                return;
            }

            var bundledBytes = input.readAllBytes();
            var bundledSha256 = sha256(bundledBytes);
            var destination = extensionsDirectory.resolve(fileName);
            var cache = loadCache();
            var cacheKey = fileName + ".sha256";
            var previousManagedSha256 = cache.getProperty(cacheKey);

            Files.createDirectories(extensionsDirectory);
            Files.createDirectories(cacheFile.getParent());

            if (Files.exists(destination)) {
                var existingSha256 = sha256(Files.readAllBytes(destination));
                if (existingSha256.equals(bundledSha256)) {
                    updateCache(cache, cacheKey, bundledSha256);
                    return;
                }

                if (previousManagedSha256 == null || !previousManagedSha256.equals(existingSha256)) {
                    log.warn(
                            "[Extension] Bundled extension target {} already exists and is not managed by this runtime; "
                                    + "leaving it unchanged. Remove the file to restore the bundled copy.",
                            destination.getFileName()
                    );
                    return;
                }
            }

            Files.write(destination, bundledBytes);
            updateCache(cache, cacheKey, bundledSha256);
            log.info("[Extension] Extracted bundled extension {}", destination.getFileName());
        } catch (IOException ex) {
            throw new CiaExtensionLoadException("Failed to extract bundled extension " + fileName, ex);
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private Properties loadCache() throws IOException {
        var props = new Properties();
        if (!Files.isRegularFile(cacheFile)) return props;
        try (InputStream input = Files.newInputStream(cacheFile)) {
            props.load(input);
        }
        return props;
    }

    private void updateCache(Properties cache, String key, String sha256) throws IOException {
        cache.setProperty(key, sha256);
        Files.createDirectories(cacheFile.getParent());
        try (var output = Files.newOutputStream(cacheFile)) {
            cache.store(output, "CreepersIArena bundled extension cache");
        }
    }

}
