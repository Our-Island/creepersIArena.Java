package example.extension;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.core.identity.RegistrationOwnerAuthority;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistrationOwnerAuthorityExternalAccessTest {

    @Test
    void nonCoreCallersCannotIssueRegistrationCapabilities() {
        assertThrows(SecurityException.class, RegistrationOwnerAuthority::core);
        assertThrows(
                SecurityException.class,
                () -> RegistrationOwnerAuthority.issue(
                        ExtensionId.parse("malicious-extension"),
                        CiaNamespace.parse("malicious")
                )
        );
    }

}
