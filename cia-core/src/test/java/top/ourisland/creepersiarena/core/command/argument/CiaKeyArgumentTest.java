package top.ourisland.creepersiarena.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.identity.CiaKey;

import static org.junit.jupiter.api.Assertions.*;

class CiaKeyArgumentTest {

    @Test
    void isRegisteredAsPaperCustomArgument() {
        assertInstanceOf(CustomArgumentType.class, CiaKeyArgument.ciaKey());
    }

    @Test
    void parsesStrictNamespacedIdIncludingNestedPath() throws CommandSyntaxException {
        var reader = new StringReader("cia:creeper/crossbow");

        var parsed = CiaKeyArgument.ciaKey().parse(reader);

        assertEquals(CiaKey.parse("cia:creeper/crossbow"), parsed);
        assertEquals(reader.getString().length(), reader.getCursor());
    }

    @Test
    void rejectsBareId() {
        assertThrows(
                CommandSyntaxException.class,
                () -> CiaKeyArgument.ciaKey().parse(new StringReader("battle"))
        );
    }

    @Test
    void rejectsPathOutsideCiaSyntax() {
        assertThrows(
                CommandSyntaxException.class,
                () -> CiaKeyArgument.ciaKey().parse(new StringReader("cia:bad.path"))
        );
    }

}
