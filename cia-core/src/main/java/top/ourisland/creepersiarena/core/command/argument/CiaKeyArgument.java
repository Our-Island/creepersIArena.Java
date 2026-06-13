package top.ourisland.creepersiarena.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;

/**
 * Strict Paper command argument for CIA-owned {@code namespace:path} identifiers.
 *
 * <p>The client sees Paper's native namespaced-key argument, so identifiers that
 * contain {@code :} and {@code /} are represented correctly in the command tree. The server still parses the original
 * token through {@link CiaKey#parse(String)} and therefore rejects bare IDs, invalid CIA characters, and malformed
 * paths.</p>
 */
public final class CiaKeyArgument implements CustomArgumentType<CiaKey, NamespacedKey> {

    private static final DynamicCommandExceptionType INVALID = new DynamicCommandExceptionType(
            value -> () -> "Expected a strict namespaced CIA id (namespace:path), got: " + value
    );

    private CiaKeyArgument() {
    }

    public static @NonNull CiaKeyArgument ciaKey() {
        return new CiaKeyArgument();
    }

    public static @NonNull CiaKey get(
            @NonNull CommandContext<?> context,
            @NonNull String name
    ) {
        return context.getArgument(name, CiaKey.class);
    }

    @Override
    public @NonNull CiaKey parse(@NonNull StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        while (reader.canRead() && !Character.isWhitespace(reader.peek())) {
            reader.skip();
        }

        var value = reader.getString().substring(start, reader.getCursor());
        try {
            return CiaKey.parse(value);
        } catch (IllegalArgumentException exception) {
            reader.setCursor(start);
            throw INVALID.createWithContext(reader, value);
        }
    }

    @Override
    public @NonNull ArgumentType<NamespacedKey> getNativeType() {
        return ArgumentTypes.namespacedKey();
    }

}
