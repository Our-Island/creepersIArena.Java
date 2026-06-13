package top.ourisland.creepersiarena.core.game.lobby.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.team.TeamId;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.config.model.GlobalConfig;
import top.ourisland.creepersiarena.core.job.JobManager;

import java.util.List;
import java.util.stream.IntStream;

public final class LobbyItemService {

    private final LobbyItemFactory items;
    private final JobManager jobs;

    public LobbyItemService(
            LobbyItemFactory items,
            JobManager jobs
    ) {
        this.items = items;
        this.jobs = jobs;
    }

    public void applyHubKit(
            Player p,
            PlayerSession s,
            GlobalConfig cfg,
            List<TeamId> selectableTeams,
            boolean showJobSelector
    ) {
        clear(p);

        var inv = p.getInventory();

        if (showJobSelector) {
            fillJobSelector(inv, s, cfg);
        }

        if (!selectableTeams.isEmpty()) {
            inv.setItem(8, items.teamCycleButton(s.selectedTeam(), selectableTeams));
        } else {
            inv.setItem(8, null);
        }
    }

    private void clear(Player p) {
        var inv = p.getInventory();
        inv.clear();
        inv.setArmorContents(null);
        inv.setItemInOffHand(null);
    }

    private void fillJobSelector(
            PlayerInventory inv,
            PlayerSession s,
            GlobalConfig cfg
    ) {
        switch (cfg.ui().lobby().jobSelectMode()) {
            case HOTBAR -> fillJobHotbar(inv, s, cfg);
            case INVENTORY -> fillJobsInventory(inv, s, cfg);
        }
    }

    private void fillJobHotbar(
            PlayerInventory inv,
            PlayerSession s,
            GlobalConfig cfg
    ) {
        inv.setItem(0, null);
        inv.setItem(7, null);

        var jobIds = jobs.getAllJobIds();
        int per = Math.max(1, cfg.ui().lobby().jobsPerPage());
        int page = Math.max(0, s.lobbyJobPage());
        int from = page * per;
        int to = Math.min(jobIds.size(), from + per);

        int slot = 1;
        for (int i = from; i < to; i++) {
            inv.setItem(slot++, items.jobSelectButton(jobIds.get(i), s));
        }

        while (slot <= 5) {
            inv.setItem(slot++, null);
        }
        inv.setItem(6, items.jobPageNextButton(page + 1));
    }

    private void fillJobsInventory(
            PlayerInventory inv,
            PlayerSession s,
            GlobalConfig cfg
    ) {
        List<JobId> jobIds = jobs.getAllJobIds();
        int base = 9;
        int max = Math.min(jobIds.size(), 27);

        IntStream.range(0, 27)
                .forEach(i -> inv.setItem(base + i, null));

        IntStream.range(0, max)
                .forEach(i -> inv.setItem(base + i, items.jobSelectButton(jobIds.get(i), s)));

        inv.setItem(6, items.jobPageNextButton(1));
    }

    public void applyDeathKit(
            Player p,
            PlayerSession s,
            GlobalConfig cfg
    ) {
        applyDeathKit(p, s, cfg, true);
    }

    public void applyDeathKit(
            Player p,
            PlayerSession s,
            GlobalConfig cfg,
            boolean showJobSelector
    ) {
        clear(p);

        if (showJobSelector) {
            fillJobSelector(p.getInventory(), s, cfg);
        }

        p.getInventory().setItem(8, items.backToHubButton());
    }

    public void applyJobSelectionKit(
            Player p,
            PlayerSession s,
            GlobalConfig cfg
    ) {
        clear(p);
        fillJobSelector(p.getInventory(), s, cfg);
    }

    public int totalJobs() {
        return jobs.getAllJobIds().size();
    }

    public JobId firstJobIdOrNull() {
        var ids = jobs.getAllJobIds();
        return ids.isEmpty() ? null : ids.getFirst();
    }

    public boolean hasJobId(JobId jobId) {
        return jobId != null && jobs.getJob(jobId) != null;
    }

}
