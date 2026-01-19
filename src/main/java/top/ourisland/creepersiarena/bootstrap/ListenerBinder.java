package top.ourisland.creepersiarena.bootstrap;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.function.Supplier;

public final class ListenerBinder {
    @Getter
    private final BootstrapRuntime rt;
    private final PluginManager pm;

    private boolean verbose = false;
    private int registered = 0;

    public ListenerBinder(BootstrapRuntime rt) {
        this.rt = rt;
        this.pm = Bukkit.getPluginManager();
    }

    public ListenerBinder verbose(boolean v) {
        this.verbose = v;
        return this;
    }

    public int registeredCount() {
        return registered;
    }

    public boolean register(Listener listener) {
        if (listener == null) return false;
        return register(listener.getClass().getSimpleName(), () -> listener);
    }

    public boolean register(String name, Supplier<? extends Listener> factory) {
        String safeName = (name == null || name.isBlank()) ? "<unnamed>" : name;
        try {
            pm.registerEvents(factory.get(), rt.plugin());
            registered++;
            if (verbose) rt.log().info("[Listener] registered: {}", safeName);
            return true;
        } catch (Throwable t) {
            rt.log().error("[Listener] failed: {} err={}", safeName, t.getMessage(), t);
            return false;
        }
    }
}
