package top.ourisland.creepersiarena.core.extension.metadata;

import top.ourisland.creepersiarena.api.extension.CiaExtensionDependency;
import top.ourisland.creepersiarena.api.extension.CiaExtensionDescriptor;
import top.ourisland.creepersiarena.api.extension.CiaExtensionLoadOrder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Reads {@code cia-extension.yml} metadata from CIA extension jars.
 * <p>
 * This reader deliberately stops at descriptor parsing and validation. It does not create class loaders, instantiate
 * the extension main class or call extension lifecycle methods.
 */
public final class CiaExtensionDescriptorReader {

    private static final Pattern EXTENSION_ID_PATTERN = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern JAVA_CLASS_PATTERN = Pattern.compile(
            "[A-Za-z_$][A-Za-z\\d_$]*(\\.[A-Za-z_$][A-Za-z\\d_$]*)+"
    );
    private static final Set<String> ROOT_KEYS = Set.of(
            "id",
            "name",
            "version",
            "main",
            "api-version",
            "cia-version",
            "authors",
            "dependencies",
            "load"
    );

    /**
     * Reads the descriptor entry from a CIA extension jar file.
     *
     * @param jarPath extension jar path
     *
     * @return parsed descriptor
     */
    public CiaExtensionDescriptor read(Path jarPath) {
        Objects.requireNonNull(jarPath, "jarPath");

        if (!Files.isRegularFile(jarPath)) {
            throw new CiaExtensionDescriptorException("Extension jar does not exist: " + jarPath);
        }

        try (var jar = new JarFile(jarPath.toFile())) {
            var entry = jar.getJarEntry(CiaExtensionDescriptor.DESCRIPTOR_ENTRY);
            if (entry == null || entry.isDirectory()) {
                throw new CiaExtensionDescriptorException(
                        "Missing " + CiaExtensionDescriptor.DESCRIPTOR_ENTRY + " in " + jarPath
                );
            }

            try (var input = jar.getInputStream(entry)) {
                return read(input, jarPath + "!" + CiaExtensionDescriptor.DESCRIPTOR_ENTRY);
            }
        } catch (IOException ex) {
            throw new CiaExtensionDescriptorException("Failed to read extension descriptor from " + jarPath, ex);
        }
    }

    /**
     * Reads a descriptor directly from an input stream.
     *
     * @param input      descriptor stream
     * @param sourceName display name used in validation errors
     *
     * @return parsed descriptor
     */
    public CiaExtensionDescriptor read(InputStream input, String sourceName) {
        Objects.requireNonNull(input, "input");

        var source = sourceName == null || sourceName.isBlank()
                ? CiaExtensionDescriptor.DESCRIPTOR_ENTRY
                : sourceName;

        try {
            var raw = parse(input, source);
            return toDescriptor(raw, source);
        } catch (IOException ex) {
            throw new CiaExtensionDescriptorException("Failed to read extension descriptor from " + source, ex);
        }
    }

    private RawDescriptor parse(InputStream input, String source) throws IOException {
        var raw = new RawDescriptor();
        var section = "";
        var dependencySection = "";

        try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            var lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                var withoutComment = stripComment(line);
                if (withoutComment.isBlank()) {
                    continue;
                }

                var indent = leadingSpaces(withoutComment);
                var trimmed = withoutComment.trim();

                if (trimmed.startsWith("- ")) {
                    var item = unquote(trimmed.substring(2).trim());
                    if (item.isBlank()) {
                        throw invalid(source, lineNumber, "List item must not be blank");
                    }

                    if ("authors".equals(section)) {
                        raw.authors.add(item);
                    } else if ("dependencies".equals(section) && "required".equals(dependencySection)) {
                        raw.requiredDependencies.add(item);
                    } else if ("dependencies".equals(section) && "optional".equals(dependencySection)) {
                        raw.optionalDependencies.add(item);
                    } else {
                        throw invalid(source, lineNumber, "List item is not inside a supported list");
                    }
                    continue;
                }

                var split = splitKeyValue(trimmed, source, lineNumber);
                var key = split.key();
                var value = split.value();

                if (indent == 0) {
                    if (!ROOT_KEYS.contains(key)) {
                        throw invalid(source, lineNumber, "Unknown root key: " + key);
                    }

                    section = key;
                    dependencySection = "";

                    switch (key) {
                        case "id" -> raw.scalars.put(key, requiredInlineValue(value, source, lineNumber, key));
                        case "name" -> raw.scalars.put(key, requiredInlineValue(value, source, lineNumber, key));
                        case "version" -> raw.scalars.put(key, requiredInlineValue(value, source, lineNumber, key));
                        case "main" -> raw.scalars.put(key, requiredInlineValue(value, source, lineNumber, key));
                        case "api-version" -> raw.scalars.put(key, requiredInlineValue(value, source, lineNumber, key));
                        case "cia-version" -> raw.scalars.put(key, requiredInlineValue(value, source, lineNumber, key));
                        case "authors" -> raw.authors.addAll(parseInlineList(value, source, lineNumber, key));
                        case "dependencies" -> requireEmptyOrMap(value, source, lineNumber, key);
                        case "load" -> requireEmptyOrMap(value, source, lineNumber, key);
                        default -> throw invalid(source, lineNumber, "Unsupported root key: " + key);
                    }
                    continue;
                }

                if ("dependencies".equals(section)) {
                    if (indent < 2) {
                        throw invalid(source, lineNumber, "dependencies children must be indented");
                    }

                    if (!"required".equals(key) && !"optional".equals(key)) {
                        throw invalid(source, lineNumber, "Unsupported dependency key: " + key);
                    }

                    dependencySection = key;
                    var ids = parseInlineList(value, source, lineNumber, key);
                    if ("required".equals(key)) {
                        raw.requiredDependencies.addAll(ids);
                    } else {
                        raw.optionalDependencies.addAll(ids);
                    }
                    continue;
                }

                if ("load".equals(section)) {
                    if (indent < 2) {
                        throw invalid(source, lineNumber, "load children must be indented");
                    }

                    if (!"order".equals(key)) {
                        throw invalid(source, lineNumber, "Unsupported load key: " + key);
                    }

                    raw.scalars.put("load.order", requiredInlineValue(value, source, lineNumber, key));
                    continue;
                }

                if ("authors".equals(section)) {
                    throw invalid(source, lineNumber, "authors must use list items or an inline list");
                }

                throw invalid(source, lineNumber, "Unexpected nested key under " + section);
            }
        }

        return raw;
    }

    private CiaExtensionDescriptor toDescriptor(RawDescriptor raw, String source) {
        var id = requireScalar(raw, source, "id");
        var name = requireScalar(raw, source, "name");
        var version = requireScalar(raw, source, "version");
        var main = requireScalar(raw, source, "main");
        var apiVersion = parseApiVersion(requireScalar(raw, source, "api-version"), source);
        var ciaVersion = requireScalar(raw, source, "cia-version");
        var loadOrder = parseLoadOrder(raw.scalars.getOrDefault("load.order", CiaExtensionLoadOrder.NORMAL.name()), source);

        validateExtensionId(id, source, "id");
        validateMainClass(main, source);

        var dependencies = new ArrayList<CiaExtensionDependency>();
        for (var dependency : raw.requiredDependencies) {
            validateExtensionId(dependency, source, "required dependency");
            dependencies.add(CiaExtensionDependency.required(dependency));
        }
        for (var dependency : raw.optionalDependencies) {
            validateExtensionId(dependency, source, "optional dependency");
            dependencies.add(CiaExtensionDependency.optional(dependency));
        }

        return new CiaExtensionDescriptor(
                id,
                name,
                version,
                main,
                apiVersion,
                ciaVersion,
                raw.authors,
                dependencies,
                loadOrder
        );
    }

    private static String stripComment(String line) {
        var quoted = false;
        var quote = '\0';
        for (var i = 0; i < line.length(); i++) {
            var current = line.charAt(i);
            if ((current == '\'' || current == '"') && (i == 0 || line.charAt(i - 1) != '\\')) {
                if (!quoted) {
                    quoted = true;
                    quote = current;
                } else if (quote == current) {
                    quoted = false;
                }
            }

            if (current == '#' && !quoted) {
                return line.substring(0, i).stripTrailing();
            }
        }
        return line.stripTrailing();
    }

    private static int leadingSpaces(String line) {
        var count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            var first = value.charAt(0);
            var last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private static CiaExtensionDescriptorException invalid(
            String source,
            int lineNumber,
            String message
    ) {
        return new CiaExtensionDescriptorException(source + ":" + lineNumber + ": " + message);
    }

    private static KeyValue splitKeyValue(
            String trimmed,
            String source,
            int lineNumber
    ) {
        var index = trimmed.indexOf(':');
        if (index < 0) {
            throw invalid(source, lineNumber, "Expected key: value");
        }

        var key = trimmed.substring(0, index).trim();
        var value = trimmed.substring(index + 1).trim();
        if (key.isBlank()) {
            throw invalid(source, lineNumber, "Key must not be blank");
        }

        return new KeyValue(key, value);
    }

    private static String requiredInlineValue(
            String value,
            String source,
            int lineNumber,
            String key
    ) {
        var unquoted = unquote(value);
        if (unquoted.isBlank()) {
            throw invalid(source, lineNumber, key + " must have a value");
        }
        return unquoted;
    }

    private static List<String> parseInlineList(
            String value,
            String source,
            int lineNumber,
            String key
    ) {
        if (value.isBlank()) {
            return List.of();
        }

        var trimmed = value.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw invalid(source, lineNumber, key + " must be a list");
        }

        var body = trimmed.substring(1, trimmed.length() - 1).trim();
        if (body.isBlank()) {
            return List.of();
        }

        var values = new ArrayList<String>();
        for (var part : body.split(",")) {
            var item = unquote(part.trim());
            if (item.isBlank()) {
                throw invalid(source, lineNumber, key + " contains a blank item");
            }
            values.add(item);
        }
        return values;
    }

    private static void requireEmptyOrMap(
            String value,
            String source,
            int lineNumber,
            String key
    ) {
        if (!value.isBlank()) {
            throw invalid(source, lineNumber, key + " must be a mapping");
        }
    }

    private static String requireScalar(
            RawDescriptor raw,
            String source,
            String key
    ) {
        var value = raw.scalars.get(key);
        if (value == null || value.isBlank()) {
            throw new CiaExtensionDescriptorException(source + ": missing required key " + key);
        }
        return value;
    }

    private static int parseApiVersion(String raw, String source) {
        try {
            var value = Integer.parseInt(raw);
            if (value < 1) {
                throw new NumberFormatException("api-version must be positive");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new CiaExtensionDescriptorException(source + ": api-version must be a positive integer", ex);
        }
    }

    private static CiaExtensionLoadOrder parseLoadOrder(String raw, String source) {
        try {
            return CiaExtensionLoadOrder.parse(raw);
        } catch (IllegalArgumentException ex) {
            var allowed = String.join(", ",
                    CiaExtensionLoadOrder.EARLY.name(),
                    CiaExtensionLoadOrder.NORMAL.name(),
                    CiaExtensionLoadOrder.LATE.name()
            );
            throw new CiaExtensionDescriptorException(source + ": load.order must be one of " + allowed, ex);
        }
    }

    private static void validateExtensionId(
            String id,
            String source,
            String fieldName
    ) {
        if (!EXTENSION_ID_PATTERN.matcher(id).matches()) {
            throw new CiaExtensionDescriptorException(
                    source + ": invalid " + fieldName + " '" + id + "'; expected [a-z0-9_.-]+"
            );
        }
    }

    private static void validateMainClass(String main, String source) {
        if (!JAVA_CLASS_PATTERN.matcher(main).matches()) {
            throw new CiaExtensionDescriptorException(source + ": main must be a fully-qualified Java class name");
        }
    }

    private record KeyValue(
            String key,
            String value
    ) {

    }

    private static final class RawDescriptor {

        private final Map<String, String> scalars = new LinkedHashMap<>();
        private final List<String> authors = new ArrayList<>();
        private final List<String> requiredDependencies = new ArrayList<>();
        private final List<String> optionalDependencies = new ArrayList<>();

    }

}
