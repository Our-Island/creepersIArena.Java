package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.identity.ExtensionSessionData;

/**
 * Runtime-issued identity scope for bundled content. Installed before annotated content classes are initialized.
 */
public final class DefaultContentRuntimeIdentity {

    private static volatile ExtensionSessionData sessionData;

    private DefaultContentRuntimeIdentity() {
    }

    public static synchronized void install(@lombok.NonNull ICiaExtensionContext context) {
        install(context.sessionData());
    }

    static synchronized void install(@lombok.NonNull ExtensionSessionData issued) {
        if (sessionData != null && sessionData != issued) {
            throw new IllegalStateException("Default-content runtime identity is already installed");
        }
        sessionData = issued;
    }

    public static ExtensionSessionData sessionData() {
        var current = sessionData;
        if (current == null) {
            throw new IllegalStateException("Default-content runtime identity has not been installed");
        }
        return current;
    }

}
