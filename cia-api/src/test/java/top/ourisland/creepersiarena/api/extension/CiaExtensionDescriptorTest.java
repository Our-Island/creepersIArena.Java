package top.ourisland.creepersiarena.api.extension;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CiaExtensionDescriptorTest {

    @Test
    void copiesInputListsAndFiltersDependencyKinds() {
        List<String> authors = new ArrayList<>(List.of("Alice"));
        List<CiaExtensionDependency> dependencies = new ArrayList<>(List.of(
                CiaExtensionDependency.required("base"),
                CiaExtensionDependency.optional("integration")
        ));

        CiaExtensionDescriptor descriptor = new CiaExtensionDescriptor(
                "sample",
                "Sample",
                "1.0.0",
                "com.example.SampleExtension",
                1,
                "[0.1.0,0.2.0)",
                authors,
                dependencies,
                CiaExtensionLoadOrder.NORMAL
        );

        authors.add("Mallory");
        dependencies.add(CiaExtensionDependency.required("late"));

        assertEquals(List.of("Alice"), descriptor.authors());
        assertEquals(List.of("base"), descriptor.requiredDependencyIds());
        assertEquals(List.of("integration"), descriptor.optionalDependencyIds());
        assertThrows(UnsupportedOperationException.class, () -> descriptor.authors().add("Bob"));
        assertThrows(UnsupportedOperationException.class, () -> descriptor.dependencies().clear());
    }

    @Test
    void rejectsNullRequiredFields() {
        assertThrows(NullPointerException.class, () -> new CiaExtensionDescriptor(
                null,
                "Name",
                "1.0.0",
                "com.example.Main",
                1,
                "0.1.0",
                List.of(),
                List.of(),
                CiaExtensionLoadOrder.NORMAL
        ));
    }

}
