package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

public final class DefaultContentIds {

    public static final String EXTENSION_ID = "cia-default-content";
    public static final String NAMESPACE_VALUE = "cia";
    public static final CiaNamespace NAMESPACE = new CiaNamespace(NAMESPACE_VALUE);
    public static final RegistrationOwner OWNER = new RegistrationOwner(new ExtensionId(EXTENSION_ID), NAMESPACE);

    private DefaultContentIds() {
    }

    public static CiaKey key(String path) {
        return CiaKey.of(NAMESPACE, path);
    }

}
