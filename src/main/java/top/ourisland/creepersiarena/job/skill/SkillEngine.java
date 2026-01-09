package top.ourisland.creepersiarena.job.skill;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class SkillEngine {
    private final SkillItemCodec codec;
    private final Map<String, Skill> byId = new HashMap<>();
    private final SkillGate gate;

    public SkillEngine(SkillItemCodec codec) {
        this(codec, (p, s, ctx) -> true);
    }

    public SkillEngine(SkillItemCodec codec, SkillGate gate) {
        this.codec = codec;
        this.gate = gate;
    }

    public void register(Skill skill) {
        byId.put(skill.id(), skill);
    }

    public @Nullable Skill getById(String id) {
        return byId.get(id);
    }

    /**
     * @return 是否成功触发并执行
     */
    public boolean dispatch(SkillContext ctx) {
        Player p = ctx.executor();
        int slot = ctx.hotbarSlot();

        // 硬约束：只允许 0/1/2
        if (slot < 0 || slot > 2) return false;

        ItemStack item = ctx.sourceItem();
        String skillId = codec.readSkillId(item);
        if (skillId == null) return false;

        Skill skill = byId.get(skillId);
        if (skill == null) return false;

        // PDC slot 与 Skill.slot 双保险
        Integer markedSlot = codec.readSkillSlot(item);
        if (markedSlot != null && markedSlot != slot) return false;
        if (skill.slot() != slot) return false;

        // 触发规则（你想要的：写 Skill 时指定触发方式）
        if (!skill.triggerSpec().matches(ctx)) return false;

        // 门禁（职业、阶段、冷却等未来塞这里）
        if (!gate.allow(p, skill, ctx)) return false;

        skill.run(ctx);
        return true;
    }

    /**
     * 可插拔的“门禁”：比如要求玩家已选择对应职业、比赛阶段允许等 现在先默认放行（你说迁移过程中不要求能编译/跑通也 OK）
     */
    @FunctionalInterface
    public interface SkillGate {
        boolean allow(Player player, Skill skill, SkillContext ctx);
    }
}
