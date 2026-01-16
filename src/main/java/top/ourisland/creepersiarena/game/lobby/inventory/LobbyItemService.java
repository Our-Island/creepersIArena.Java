package top.ourisland.creepersiarena.game.lobby.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemFactory;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.JobManager;

import java.util.List;

public final class LobbyItemService {
    private final LobbyItemFactory items;
    private final JobManager jobs;

    public LobbyItemService(LobbyItemFactory items, JobManager jobs) {
        this.items = items;
        this.jobs = jobs;
    }

    public void applyHubKit(Player p, PlayerSession s, GlobalConfig cfg) {
        clear(p);

        var inv = p.getInventory();

        switch (cfg.ui().lobby().jobSelectMode()) {
            case HOTBAR -> fillJobHotbar(inv, s, cfg);
            case INVENTORY -> fillJobsInventory(inv, s, cfg);
        }

        inv.setItem(8, items.teamCycleButton(s.selectedTeam(), cfg.game().battle().maxTeam()));
    }

    private void clear(Player p) {
        var inv = p.getInventory();
        inv.clear();
        inv.setArmorContents(null);
        inv.setItemInOffHand(null);
    }

    private void fillJobHotbar(PlayerInventory inv, PlayerSession s, GlobalConfig cfg) {
        inv.setItem(0, null);
        inv.setItem(7, null);

        List<String> jobIds = jobs.getAllJobIds();
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

    private void fillJobsInventory(PlayerInventory inv, PlayerSession s, GlobalConfig cfg) {
        List<String> jobIds = jobs.getAllJobIds();
        int base = 9;
        int max = Math.min(jobIds.size(), 27);

        for (int i = 0; i < 27; i++) {
            inv.setItem(base + i, null);
        }

        for (int i = 0; i < max; i++) {
            inv.setItem(base + i, items.jobSelectButton(jobIds.get(i), s));
        }

        inv.setItem(6, items.jobPageNextButton(1));
    }

    public void applyDeathKit(Player p, PlayerSession s, GlobalConfig cfg) {
        clear(p);

        var inv = p.getInventory();

        switch (cfg.ui().lobby().jobSelectMode()) {
            case HOTBAR -> fillJobHotbar(inv, s, cfg);
            case INVENTORY -> fillJobsInventory(inv, s, cfg);
        }

        inv.setItem(8, items.backToHubButton());
    }

    public int totalJobs() {
        return jobs.getAllJobIds().size();
    }

    public String firstJobIdOrNull() {
        List<String> ids = jobs.getAllJobIds();
        return ids.isEmpty() ? null : ids.getFirst();
    }

    public boolean hasJobId(String jobIdRaw) {
        JobId jid = JobId.fromId(jobIdRaw);
        return jid != null && jobs.getJob(jid) != null;
    }
}
