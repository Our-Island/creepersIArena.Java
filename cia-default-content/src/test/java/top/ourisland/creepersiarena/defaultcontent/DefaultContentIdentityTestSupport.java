package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.identity.ExtensionSessionData;
import top.ourisland.creepersiarena.api.identity.TestRegistrationOwners;

public final class DefaultContentIdentityTestSupport {

    private static final ExtensionSessionData SESSION_DATA = new ExtensionSessionData(
            TestRegistrationOwners.issue("cia-default-content-test", "cia")
    );

    private DefaultContentIdentityTestSupport() {
    }

    public static void install() {
        DefaultContentRuntimeIdentity.install(SESSION_DATA);
    }

}
