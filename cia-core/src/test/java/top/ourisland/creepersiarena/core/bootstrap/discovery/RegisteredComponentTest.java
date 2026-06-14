package top.ourisland.creepersiarena.core.bootstrap.discovery;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.identity.RegistrationOwnerAuthority;

import static org.junit.jupiter.api.Assertions.*;

class RegisteredComponentTest {

    private static final RegistrationOwner OWNER = RegistrationOwnerAuthority.issue(
            ExtensionId.parse("custom-extension"),
            CiaNamespace.parse("custom")
    );

    @Test
    void retainsTypedOwnerAndId() {
        var id = JobId.parse("custom:job");
        var component = new RegisteredComponent<>(OWNER, id, "value");

        assertEquals(OWNER, component.owner());
        assertEquals(id, component.id());
        assertEquals("value", component.value());
    }

    @Test
    void rejectsNullOwnerIdAndValue() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new RegisteredComponent<>(null, JobId.parse("custom:job"), "value")),
                () -> assertThrows(NullPointerException.class, () -> new RegisteredComponent<>(OWNER, null, "value")),
                () -> assertThrows(NullPointerException.class, () -> new RegisteredComponent<>(OWNER, JobId.parse("custom:job"), null))
        );
    }

}
