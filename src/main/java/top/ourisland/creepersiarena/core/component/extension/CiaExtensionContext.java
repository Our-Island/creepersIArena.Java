package top.ourisland.creepersiarena.core.component.extension;

import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.game.mode.IGameMode;
import top.ourisland.creepersiarena.job.IJob;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;

public interface CiaExtensionContext {

    void registerModule(IBootstrapModule module);

    void registerJob(IJob job);

    void registerSkill(ISkillDefinition skill);

    void registerMode(IGameMode mode);

    void registerAnnotated(Plugin owner, String basePackage);

}
