package top.ourisland.creepersiarena.core.job.skill.runtime;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.SkillId;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.identity.OwnedRegistry;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class SkillRegistry {

    private final PlayerSessionStore sessions;
    private final OwnedRegistry<SkillId, ISkillDefinition> skills;

    public SkillRegistry(PlayerSessionStore sessions) {
        this(sessions, new NamespaceRegistry());
    }

    public SkillRegistry(
            PlayerSessionStore sessions,
            NamespaceRegistry namespaces
    ) {
        this.sessions = sessions;
        this.skills = new OwnedRegistry<>(namespaces);
    }

    public synchronized void replaceAll(Collection<ISkillDefinition> values) {
        skills.replaceAllValidated(values.stream()
                .map(skill -> new RegisteredComponent<>(RegistrationOwner.CORE, skill.id(), skill))
                .toList());
    }

    public synchronized void register(ISkillDefinition skill) {
        register(RegistrationOwner.CORE, skill);
    }

    public synchronized void register(
            RegistrationOwner owner,
            ISkillDefinition skill
    ) {
        skills.register(owner, skill.id(), skill);
    }

    public synchronized void clear() {
        skills.clear();
    }

    public synchronized void replaceAllRegistered(
            Collection<RegisteredComponent<SkillId, ISkillDefinition>> values
    ) {
        skills.replaceAllValidated(values);
    }

    public synchronized void clearOwner(RegistrationOwner owner) {
        skills.clearOwner(owner);
    }

    public List<ISkillDefinition> skillsOf(Player player) {
        var session = sessions.get(player);
        if (session == null) return List.of();
        return skillsOf(session.selectedJob());
    }

    public synchronized List<ISkillDefinition> skillsOf(JobId jobId) {
        if (jobId == null) return List.of();
        return skills.entries().stream()
                .map(RegisteredComponent::value)
                .filter(skill -> jobId.equals(skill.jobId()))
                .sorted(Comparator.comparingInt(ISkillDefinition::uiSlot)
                        .thenComparing(skill -> skill.id().asString())
                )
                .toList();
    }

    public synchronized RegistrationOwner ownerOf(SkillId skillId) {
        var registered = skills.get(skillId);
        return registered == null ? null : registered.owner();
    }

    public synchronized List<RegisteredComponent<SkillId, ISkillDefinition>> registeredSkills() {
        return skills.entries();
    }

}
