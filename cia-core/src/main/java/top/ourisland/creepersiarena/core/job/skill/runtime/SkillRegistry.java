package top.ourisland.creepersiarena.core.job.skill.runtime;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
import java.util.function.Function;

public final class SkillRegistry {

    private final PlayerSessionStore sessions;
    private final OwnedRegistry<SkillId, ISkillDefinition> skills;
    private final Function<JobId, RegistrationOwner> jobOwnerLookup;

    public SkillRegistry(
            @lombok.NonNull PlayerSessionStore sessions,
            @lombok.NonNull NamespaceRegistry namespaces,
            @lombok.NonNull Function<JobId, RegistrationOwner> jobOwnerLookup
    ) {
        this.sessions = sessions;
        this.skills = new OwnedRegistry<>(namespaces);
        this.jobOwnerLookup = jobOwnerLookup;
    }

    public synchronized void replaceAllRegistered(
            Collection<RegisteredComponent<SkillId, ISkillDefinition>> values
    ) {
        var snapshot = List.copyOf(values);
        snapshot.forEach(registered -> SkillRegistrationValidator.validate(
                registered.owner(),
                registered.value(),
                jobOwnerLookup
        ));
        skills.replaceAllValidated(snapshot);
    }

    public synchronized void register(
            RegistrationOwner owner,
            ISkillDefinition skill
    ) {
        SkillRegistrationValidator.validate(owner, skill, jobOwnerLookup);
        skills.register(owner, skill.id(), skill);
    }

    public synchronized void registerAll(
            RegistrationOwner owner,
            Collection<ISkillDefinition> values
    ) {
        var snapshot = List.copyOf(values);
        validateAll(owner, snapshot);
        skills.registerAll(
                owner,
                snapshot.stream()
                        .map(skill -> new OwnedRegistry.Registration<>(skill.id(), skill))
                        .toList()
        );
    }

    public synchronized void validateAll(
            RegistrationOwner owner,
            Collection<ISkillDefinition> values
    ) {
        var snapshot = List.copyOf(values);
        snapshot.forEach(skill -> SkillRegistrationValidator.validate(owner, skill, jobOwnerLookup));
        skills.validateAll(
                owner,
                snapshot.stream()
                        .map(skill -> new OwnedRegistry.Registration<>(skill.id(), skill))
                        .toList()
        );
    }

    public synchronized void clear() {
        skills.clear();
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

    public synchronized @Nullable RegistrationOwner ownerOf(SkillId skillId) {
        var registered = skills.get(skillId);
        return registered == null ? null : registered.owner();
    }

    public synchronized @NonNull List<RegisteredComponent<SkillId, ISkillDefinition>> registeredSkills() {
        return skills.entries();
    }

}
