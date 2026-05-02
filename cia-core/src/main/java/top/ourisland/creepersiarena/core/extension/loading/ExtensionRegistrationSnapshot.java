package top.ourisland.creepersiarena.core.extension.loading;

import java.util.List;

public record ExtensionRegistrationSnapshot(
        List<String> jobs,
        List<String> skills,
        List<String> modes,
        List<String> listeners,
        List<String> installedResources,
        List<String> mergedYamlResources,
        List<String> mergedPropertiesResources
) {

    public ExtensionRegistrationSnapshot {
        jobs = List.copyOf(jobs);
        skills = List.copyOf(skills);
        modes = List.copyOf(modes);
        listeners = List.copyOf(listeners);
        installedResources = List.copyOf(installedResources);
        mergedYamlResources = List.copyOf(mergedYamlResources);
        mergedPropertiesResources = List.copyOf(mergedPropertiesResources);
    }

    public int totalComponents() {
        return jobs.size() + skills.size() + modes.size() + listeners.size();
    }

    public int totalResources() {
        return installedResources.size() + mergedYamlResources.size() + mergedPropertiesResources.size();
    }

}
