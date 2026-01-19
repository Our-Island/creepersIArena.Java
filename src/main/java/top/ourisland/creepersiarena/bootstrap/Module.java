package top.ourisland.creepersiarena.bootstrap;

public interface Module {
    String name();

    default StageTask install(BootstrapRuntime rt) {
        return null;
    }

    default StageTask start(BootstrapRuntime rt) {
        return null;
    }

    default StageTask stop(BootstrapRuntime rt) {
        return null;
    }

    default boolean registerListeners(ListenerBinder binder) {
        return false;
    }
}
