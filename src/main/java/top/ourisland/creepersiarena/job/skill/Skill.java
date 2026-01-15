package top.ourisland.creepersiarena.job.skill;

import org.bukkit.Material;
import top.ourisland.creepersiarena.job.JobId;

public interface Skill {
    String id();          // 全局唯一，如 "golem:slam"

    JobId jobId();        // 你已有 JobId 枚举 :contentReference[oaicite:3]{index=3}

    /**
     * 固定技能槽：0/1/2（对应玩家快捷栏）
     */
    int slot();

    /**
     * 技能物品外观：弩/斧/工具/图标等
     */
    Material itemType();

    int cooldown();

    /**
     * 写 Skill 时声明触发规则（可组合） 建议至少包含：TriggerSpec.triggers(...) + TriggerSpec.mainHandOnly() +
     * TriggerSpec.hotbarSlot(slot())
     */
    TriggerSpec triggerSpec();

    void run(SkillContext ctx);
}
