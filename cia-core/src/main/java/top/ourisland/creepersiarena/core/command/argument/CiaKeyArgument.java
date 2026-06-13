package top.ourisland.creepersiarena.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;

import java.util.Collection;
import java.util.List;

/**
 * Strict Brigadier argument for CIA-owned namespace:path identifiers.
 */
public final class CiaKeyArgument implements ArgumentType<CiaKey> {

    private static final DynamicCommandExceptionType INVALID = new DynamicCommandExceptionType(
            value -> () -> "Expected a strict namespaced CIA id (namespace:path), got: " + value
    );
    private static final Collection<String> EXAMPLES = List.of("cia:creeper", "cia:creeper/crossbow");

    private CiaKeyArgument() {
    }

    public static @NonNull CiaKeyArgument ciaKey() {
        return new CiaKeyArgument();
    }

    public static CiaKey get(
            @NonNull CommandContext<?> context,
            String name
    ) {
        return context.getArgument(name, CiaKey.class);
    }

    @Override
    public @NonNull CiaKey parse(@NonNull StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String value = reader.readUnquotedString();
        try {
            return CiaKey.parse(value);
        } catch (IllegalArgumentException exception) {
            reader.setCursor(start);
            throw INVALID.createWithContext(reader, value);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
