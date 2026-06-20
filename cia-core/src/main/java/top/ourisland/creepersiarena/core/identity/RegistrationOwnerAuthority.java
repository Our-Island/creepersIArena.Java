package top.ourisland.creepersiarena.core.identity;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

/**
 * Core-runtime authority that issues opaque {@link RegistrationOwner} capabilities.
 * <p>
 * This class is supplied by {@code cia-core}, not by the public API artifact. Calls are accepted only from classes
 * loaded by the core plugin class loader, preventing an extension class loader from using this internal bridge even
 * when it deliberately adds {@code cia-core} to its compile classpath.
 */
public final class RegistrationOwnerAuthority {

    private static final String CORE_PACKAGE_PREFIX = "top.ourisland.creepersiarena.core.";
    private static final StackWalker CALLERS = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final RegistrationOwner CORE = new RuntimeRegistrationOwner(
            new ExtensionId("core"),
            CiaNamespace.CORE
    );

    private RegistrationOwnerAuthority() {
    }

    public static @NonNull RegistrationOwner core() {
        requireCoreCaller();
        return CORE;
    }

    private static void requireCoreCaller() {
        var caller = CALLERS.walk(frames -> frames
                .map(StackWalker.StackFrame::getDeclaringClass)
                .filter(type -> type != RegistrationOwnerAuthority.class)
                .findFirst()
                .orElseThrow());
        if (caller.getClassLoader() != RegistrationOwnerAuthority.class.getClassLoader()
                || !caller.getName().startsWith(CORE_PACKAGE_PREFIX)) {
            throw new SecurityException("Registration owners can only be issued by the CIA core runtime");
        }
    }

    public static @NonNull RegistrationOwner issue(
            @NonNull ExtensionId extensionId,
            @NonNull CiaNamespace namespace
    ) {
        requireCoreCaller();
        if (CiaNamespace.CORE.equals(namespace)) {
            throw new IllegalArgumentException("The core namespace is reserved");
        }
        if ("minecraft".equals(namespace.value())) {
            throw new IllegalArgumentException("The minecraft namespace is reserved");
        }
        return new RuntimeRegistrationOwner(extensionId, namespace);
    }

    static void requireRuntimeIssued(@lombok.NonNull RegistrationOwner owner) {
        if (!(owner instanceof RuntimeRegistrationOwner)) {
            throw new SecurityException("Registration owner was not issued by the CIA core runtime");
        }
    }

}
