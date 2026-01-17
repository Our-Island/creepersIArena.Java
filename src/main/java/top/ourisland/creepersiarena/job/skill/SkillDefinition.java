package top.ourisland.creepersiarena.job.skill;

import top.ourisland.creepersiarena.job.skill.event.Trigger;

import java.util.List;

public interface SkillDefinition {
    String id();            // 例如 "creeper.boom"

    String jobId();         // 例如 "creeper"

    SkillType kind();       // ACTIVE / PASSIVE

    int uiSlot();           // 主动技能槽位(建议 0..2)，被动可用 -1

    int cooldownSeconds();  // 0 表示无冷却

    List<Trigger> triggers();

    SkillIcon icon();       // 负责生成 UI 展示 ItemStack（任意组件）

    SkillExecutor executor();
}
