package top.ourisland.creepersiarena.game.mode.impl.battle;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.job.Job;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.Skill;
import top.ourisland.creepersiarena.job.skill.SkillItemFactory;

import java.util.List;
import java.util.Objects;

public final class BattleKitService {

    private final JobManager jobs;
    private final SkillItemFactory skillItems;

    public BattleKitService(JobManager jobs, SkillItemFactory skillItems) {
        this.jobs = Objects.requireNonNull(jobs, "jobs");
        this.skillItems = Objects.requireNonNull(skillItems, "skillItems");
    }

    public void apply(Player p, PlayerSession s) {
        if (p == null || s == null) return;

        clear(p);

        JobId jobId = s.selectedJob();
        if (jobId == null) return;

        Job job = jobs.getJob(jobId);
        if (job == null) return;

        // 1) 技能物品：约定 0/1/2
        List<Skill> skills = job.skills();
        for (int i = 0; i < 3 && i < skills.size(); i++) {
            Skill skill = skills.get(i);
            if (skill != null) {
                p.getInventory().setItem(i, skillItems.create(skill));
            }
        }

        // 2) 盔甲：约定 boots, leggings, chestplate, helmet（和 Bukkit setArmorContents 顺序一致）
        ItemStack[] armor = job.armorTemplate();
        if (armor != null && armor.length == 4) {
            ItemStack[] cloned = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                cloned[i] = (armor[i] == null ? null : armor[i].clone());
            }
            p.getInventory().setArmorContents(cloned);
        }
    }

    private void clear(Player p) {
        var inv = p.getInventory();
        inv.clear();
        inv.setArmorContents(null);
        inv.setItemInOffHand(null);
    }
}
