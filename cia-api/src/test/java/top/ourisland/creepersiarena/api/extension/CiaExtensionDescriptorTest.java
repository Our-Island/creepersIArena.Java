package top.ourisland.creepersiarena.api.extension;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CiaExtensionDescriptorTest {

    @Test
    void copiesInputListsAndFiltersDependencyKinds() {
        var authors = new ArrayList<>(List.of("Alice"));
        var dependencies = new ArrayList<>(List.of(
                CiaExtensionDependency.required(ExtensionId.parse("base")),
                CiaExtensionDependency.optional(ExtensionId.parse("integration"))
        ));

        var descriptor = new CiaExtensionDescriptor(
                ExtensionId.parse("sample"),
                CiaNamespace.parse("sample"),
                "Sample",
                "1.0.0",
                "com.example.SampleExtension",
                CiaExtensionDescriptor.CURRENT_API_VERSION,
                "[0.1.0,0.2.0)",
                authors,
                dependencies,
                CiaExtensionLoadOrder.NORMAL
        );

        authors.add("Mallory");
        dependencies.add(CiaExtensionDependency.required(ExtensionId.parse("late")));

        assertEquals(List.of("Alice"), descriptor.authors());
        assertEquals(List.of(ExtensionId.parse("base")), descriptor.requiredDependencyIds());
        assertEquals(List.of(ExtensionId.parse("integration")), descriptor.optionalDependencyIds());
        assertEquals(CiaNamespace.parse("sample"), descriptor.owner().namespace());
        assertThrows(UnsupportedOperationException.class, () -> descriptor.authors().add("Bob"));
        assertThrows(UnsupportedOperationException.class, () -> descriptor.dependencies().clear());
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void rejectsNullRequiredFields() {
        assertThrows(NullPointerException.class, () -> new CiaExtensionDescriptor(
                null,
                CiaNamespace.parse("sample"),
                "Name",
                "1.0.0",
                "com.example.Main",
                CiaExtensionDescriptor.CURRENT_API_VERSION,
                "0.1.0",
                List.of(),
                List.of(),
                CiaExtensionLoadOrder.NORMAL
        ));
    }

}
