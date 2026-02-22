package top.ourisland.creepersiarena.game.mode.impl.battle;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.job.IJob;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry;
import top.ourisland.creepersiarena.job.skill.ui.SkillHotbarRenderer;

import java.util.function.LongSupplier;

public final class BattleKitService {

    private final JobManager jobs;
    private final SkillRegistry skillRegistry;
    private final SkillHotbarRenderer skillRenderer;
    private final LongSupplier nowTick;

    public BattleKitService(
            @lombok.NonNull JobManager jobs,
            @lombok.NonNull SkillRegistry skillRegistry,
            @lombok.NonNull SkillHotbarRenderer skillRenderer,
            @lombok.NonNull LongSupplier nowTick
    ) {
        this.jobs = jobs;
        this.skillRegistry = skillRegistry;
        this.skillRenderer = skillRenderer;
        this.nowTick = nowTick;
    }

    public void apply(Player p, PlayerSession s) {
        if (p == null || s == null) return;

        clear(p);

        JobId jobId = s.selectedJob();
        if (jobId == null) return;

        IJob job = jobs.getJob(jobId);
        if (job == null) return;

        ItemStack[] armor = job.armorTemplate();
        if (armor != null && armor.length == 4) {
            ItemStack[] cloned = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                cloned[i] = (armor[i] == null ? null : armor[i].clone());
            }
            p.getInventory().setArmorContents(cloned);
        }

        skillRenderer.render(p, skillRegistry.skillsOf(p), nowTick.getAsLong());
    }

    private void clear(Player p) {
        var inv = p.getInventory();
        inv.clear();
        inv.setArmorContents(null);
        inv.setItemInOffHand(null);
    }
}
