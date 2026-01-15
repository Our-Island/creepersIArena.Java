package top.ourisland.creepersiarena.job.skill;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class SkillEngine {
    private final SkillItemCodec codec;
    private final SkillItemFactory items;
    private final Map<String, Skill> byId = new HashMap<>();
    private final SkillGate gate;

    private final Map<UUID, Map<String, Long>> cooldownEnds = new HashMap<>();

    public SkillEngine(SkillItemCodec codec, SkillItemFactory items) {
        this(
                codec,
                items,
                (p, s, ctx) -> true
        );
    }

    public SkillEngine(SkillItemCodec codec, SkillItemFactory items, SkillGate gate) {
        this.codec = codec;
        this.items = items;
        this.gate = gate;
    }

    public void register(Skill skill) {
        byId.put(skill.id(), skill);
    }

    public @Nullable Skill getById(String id) {
        return byId.get(id);
    }

    public boolean dispatch(SkillContext ctx) {
        Player p = ctx.executor();
        int slot = ctx.hotbarSlot();

        if (slot < 0 || slot > 2) return false;

        ItemStack item = ctx.sourceItem();
        String skillId = codec.readSkillId(item);
        if (skillId == null) return false;

        Skill skill = byId.get(skillId);
        if (skill == null) return false;

        Integer markedSlot = codec.readSkillSlot(item);
        if (markedSlot != null && markedSlot != slot) return false;
        if (skill.slot() != slot) return false;

        if (!skill.triggerSpec().matches(ctx)) return false;

        if (!gate.allow(p, skill, ctx)) return false;

        long nowTick = ctx.nowTick();
        long remain = remainingTicks(p.getUniqueId(), skill.id(), nowTick);
        if (remain > 0) {
            applyCooldownItem(p, skill, remain);
            return false;
        }

        skill.run(ctx);

        int cdSeconds = skill.cooldown();
        if (cdSeconds > 0) {
            long endTick = nowTick + (cdSeconds * 20L);
            cooldownEnds
                    .computeIfAbsent(p.getUniqueId(), __ -> new HashMap<>())
                    .put(skill.id(), endTick);

            applyCooldownItem(p, skill, endTick - nowTick);
        }

        return true;
    }

    private long remainingTicks(UUID player, String skillId, long nowTick) {
        return Optional.ofNullable(cooldownEnds.get(player))
                .map(map -> map.get(skillId))
                .map(end -> Math.max(0, end - nowTick))
                .orElse(0L);
    }

    private void applyCooldownItem(Player p, Skill skill, long remainTicks) {
        int slot = skill.slot();

        ItemStack cdItem;
        if (remainTicks <= 20) {
            cdItem = items.createCooldownLastSecondTicks(skill, (int) remainTicks);
        } else {
            int secondsLeft = (int) ((remainTicks + 19) / 20);
            cdItem = items.createCooldownSeconds(skill, secondsLeft);
        }

        p.getInventory().setItem(slot, cdItem);
    }

    public void tickCooldowns(Iterable<? extends Player> players, long nowTick) {
        for (Player p : players) {
            UUID uid = p.getUniqueId();
            Map<String, Long> map = cooldownEnds.get(uid);
            if (map == null || map.isEmpty()) continue;

            Iterator<Map.Entry<String, Long>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> e = it.next();
                String skillId = e.getKey();
                long endTick = e.getValue();

                Skill skill = byId.get(skillId);
                if (skill == null) {
                    it.remove();
                    continue;
                }

                int slot = skill.slot();
                ItemStack cur = p.getInventory().getItem(slot);
                String curId = codec.readSkillId(cur);
                if (curId == null || !curId.equals(skillId)) {
                    it.remove();
                    continue;
                }

                long remain = endTick - nowTick;
                if (remain <= 0) {
                    p.getInventory().setItem(slot, items.create(skill));
                    it.remove();
                    continue;
                }

                if (remain > 20) {
                    if (remain % 20 != 0) continue;
                }
                applyCooldownItem(p, skill, remain);
            }

            if (map.isEmpty()) cooldownEnds.remove(uid);
        }
    }

    @FunctionalInterface
    public interface SkillGate {
        boolean allow(Player player, Skill skill, SkillContext ctx);
    }
}
