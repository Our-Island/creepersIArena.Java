package top.ourisland.creepersiarena.core.extension.loading;

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

}
