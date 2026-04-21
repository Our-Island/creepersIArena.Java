package top.ourisland.creepersiarena.core.component.discovery;

import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.component.metadata.BootstrapModuleMetadata;

import java.util.*;

public final class ModuleOrderResolver {

    public List<IBootstrapModule> sort(@lombok.NonNull List<IBootstrapModule> modules) {
        List<IBootstrapModule> ordered = new ArrayList<>(modules);
        ordered.sort(Comparator
                .comparingInt((IBootstrapModule m) -> BootstrapModuleMetadata.of(m.getClass()).order())
                .thenComparing(IBootstrapModule::name));

        Map<Class<?>, IBootstrapModule> byClass = new LinkedHashMap<>();
        for (IBootstrapModule m : ordered) {
            byClass.put(m.getClass(), m);
        }

        List<IBootstrapModule> out = new ArrayList<>();
        Set<Class<?>> visiting = new HashSet<>();
        Set<Class<?>> visited = new HashSet<>();

        for (IBootstrapModule module : ordered) {
            visit(module.getClass(), byClass, visiting, visited, out);
        }

        return List.copyOf(out);
    }

    private void visit(
            Class<?> type,
            Map<Class<?>, IBootstrapModule> byClass,
            Set<Class<?>> visiting,
            Set<Class<?>> visited,
            List<IBootstrapModule> out
    ) {
        if (visited.contains(type)) return;
        if (!visiting.add(type)) {
            throw new IllegalStateException("Circular bootstrap module dependency: " + type.getName());
        }

        var meta = BootstrapModuleMetadata.of(type);
        for (Class<? extends IBootstrapModule> dep : meta.after()) {
            if (byClass.containsKey(dep)) {
                visit(dep, byClass, visiting, visited, out);
            }
        }

        visiting.remove(type);
        visited.add(type);
        out.add(byClass.get(type));
    }

}
