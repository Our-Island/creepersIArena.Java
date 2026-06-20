package top.ourisland.creepersiarena.api.identity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CiaKeyTest {

    @Test
    void parsesAndSerializesCanonicalIdsWithoutNormalization() {
        var key = CiaKey.parse("my_extension:item-a/child_2");

        assertEquals("my_extension", key.namespace().value());
        assertEquals("item-a/child_2", key.path().value());
        assertEquals("my_extension:item-a/child_2", key.asString());
        assertEquals(key, CiaKey.parse(key.asString()));
    }

    @Test
    void rejectsBareMalformedAndNormalizedLookingIds() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> CiaKey.parse("battle")),
                () -> assertThrows(IllegalArgumentException.class, () -> CiaKey.parse(":battle")),
                () -> assertThrows(IllegalArgumentException.class, () -> CiaKey.parse("cia:")),
                () -> assertThrows(IllegalArgumentException.class, () -> CiaKey.parse("cia:a:b")),
                () -> assertThrows(IllegalArgumentException.class, () -> CiaKey.parse("CIA:battle")),
                () -> assertThrows(IllegalArgumentException.class, () -> CiaKey.parse("cia:item.path")),
                () -> assertThrows(IllegalArgumentException.class, () -> CiaKey.parse("cia:item path"))
        );
    }

    @Test
    void doesNotCollapseHyphensAndUnderscores() {
        var first = CiaKey.parse("my_extension:item-a");
        var second = CiaKey.parse("my-extension:item_a");

        assertNotEquals(first, second);
        assertEquals("my_extension:item-a", first.asString());
        assertEquals("my-extension:item_a", second.asString());
    }

}
