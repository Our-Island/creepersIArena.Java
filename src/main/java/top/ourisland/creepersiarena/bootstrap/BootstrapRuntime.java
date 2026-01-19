package top.ourisland.creepersiarena.bootstrap;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public final class BootstrapRuntime {
    private final JavaPlugin plugin;
    private final Logger log;
    private final ServiceRegistry services = new ServiceRegistry();
    private final List<BukkitTask> tasks = new ArrayList<>();

    public BootstrapRuntime(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getSLF4JLogger();
    }

    public <T> T requireService(Class<T> type) {
        return services.require(type);
    }

    public <T> T getService(Class<T> type) {
        return services.get(type);
    }

    public <T> void putService(Class<T> type, T value) {
        services.put(type, value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void putAllServices(Map<Class<?>, Object> providedServices) {
        if (providedServices == null) return;

        providedServices.forEach(
                (clazz, instance) -> this.services.put((Class) clazz, instance)
        );
    }

    public void trackTask(BukkitTask task) {
        if (task != null) tasks.add(task);
    }

    public void cancelTrackedTasks() {
        for (BukkitTask t : tasks) {
            try {
                t.cancel();
            } catch (Throwable ignored) {
            }
        }
        tasks.clear();
    }
}
