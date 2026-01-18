package top.ourisland.creepersiarena.game.lobby.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.job.Job;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.util.I18n;
import top.ourisland.creepersiarena.util.LangKeyResolver;

import java.util.List;
import java.util.Objects;

public final class LobbyItemFactory {
    private final LobbyItemCodec codec;
    private final JobManager jobs;

    public LobbyItemFactory(LobbyItemCodec codec, JobManager jobs) {
        this.codec = Objects.requireNonNull(codec, "codec");
        this.jobs = Objects.requireNonNull(jobs, "jobs");
    }

    public @Nullable ItemStack jobSelectButton(String jobId, PlayerSession s) {
        Objects.requireNonNull(jobId, "jobId");
        Objects.requireNonNull(s, "s");

        boolean selected = s.selectedJob() != null && s.selectedJob().id().equals(jobId);

        JobId jid = JobId.fromId(jobId);
        Job job = (jid == null) ? null : jobs.getJob(jid);
        if (job == null) return null;

        ItemStack item = job.display().clone();
        item.setAmount(1);

        var meta = item.getItemMeta();
        if (meta != null) {
            Component baseName = I18n.langNP(LangKeyResolver.jobName(jid));

            meta.displayName(selected
                    ? Component.text("[选择中] ").append(baseName)
                    : baseName
            );

            meta.setEnchantmentGlintOverride(selected);

            item.setItemMeta(meta);
        }

        codec.markJobId(item, jobId);
        return item;
    }

    public ItemStack jobPageNextButton(int nextPage) {
        ItemStack it = new ItemStack(Material.OAK_HANGING_SIGN);
        var meta = it.getItemMeta();
        meta.displayName(Component.text("下一页"));
        meta.lore(List.of(Component.text("右键翻到第 " + nextPage + " 页")));
        it.setItemMeta(meta);
        codec.markAction(it, LobbyAction.JOB_PAGE_NEXT);
        codec.markJobPage(it, nextPage);
        return it;
    }

    public ItemStack teamCycleButton(@Nullable Integer team, int maxTeam) {
        ItemStack it = new ItemStack(teamMaterial(team));
        var meta = it.getItemMeta();

        String label = (team == null) ? "随机分队" : ("队伍 " + team + "/" + maxTeam);
        meta.displayName(Component.text("切换队伍"));
        meta.lore(List.of(Component.text("当前: " + label), Component.text("右键切换")));
        it.setItemMeta(meta);

        codec.markAction(it, LobbyAction.TEAM_CYCLE);
        return it;
    }

    private Material teamMaterial(@Nullable Integer team) {
        if (team == null) return Material.WHITE_WOOL;
        Material[] colors = {
                Material.RED_WOOL,
                Material.LIGHT_BLUE_WOOL,
                Material.LIME_WOOL,
                Material.YELLOW_WOOL,
                Material.PURPLE_WOOL,
                Material.ORANGE_WOOL,
                Material.CYAN_WOOL,
                Material.PINK_WOOL
        };
        int idx = Math.max(1, team) - 1;
        return colors[idx % colors.length];
    }

    public ItemStack backToHubButton() {
        ItemStack it = new ItemStack(Material.CAMPFIRE);
        var meta = it.getItemMeta();
        meta.displayName(Component.text("返回大厅"));
        meta.lore(List.of(Component.text("右键返回大厅")));
        it.setItemMeta(meta);
        codec.markAction(it, LobbyAction.BACK_TO_HUB);
        return it;
    }
}
