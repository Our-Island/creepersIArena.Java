package top.ourisland.creepersiarena.core.extension.loading;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

public final class CiaExtensionClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public CiaExtensionClassLoader(
            @lombok.NonNull URL[] urls,
            @lombok.NonNull ClassLoader parent
    ) {
        super(urls, parent);
    }

    /**
     * Opens a resource from this extension jar/directory only.
     *
     * <p>{@link ClassLoader#getResourceAsStream(String)} is parent-first, which is correct for shared API classes
     * but wrong for extension-owned resources such as {@code lang/en_us.properties}: core can contain a resource with
     * the same path. Resource installation and merge operations must therefore read the extension copy first.
     */
    public InputStream openLocalResource(String name) throws IOException {
        URL resource = findResource(name);
        return resource == null ? null : resource.openStream();
    }

}
