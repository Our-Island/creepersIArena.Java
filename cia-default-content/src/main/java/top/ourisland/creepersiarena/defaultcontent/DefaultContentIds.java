package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;

public final class DefaultContentIds {

    public static final String EXTENSION_ID = "cia-default-content";
    public static final String NAMESPACE_VALUE = "cia";
    public static final CiaNamespace NAMESPACE = new CiaNamespace(NAMESPACE_VALUE);

    private DefaultContentIds() {
    }

    public static CiaKey key(String path) {
        return CiaKey.of(NAMESPACE, path);
    }

}
