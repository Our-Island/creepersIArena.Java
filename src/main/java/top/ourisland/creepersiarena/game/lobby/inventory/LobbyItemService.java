package top.ourisland.creepersiarena.game.lobby.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemFactory;
import top.ourisland.creepersiarena.game.player.PlayerSession;
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

        // UI: 职业选择模式
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
        // 0 空
        // 1-? 职业列表
        // 6 翻页
        // 7 空
        // 8 留给 team/back
        inv.setItem(0, null);
        inv.setItem(7, null);

        List<String> jobIds = jobs.getAllJobIds();
        int per = Math.max(1, cfg.ui().lobby().jobsPerPage());

        int page = Math.max(0, s.lobbyJobPage());
        int from = page * per;
        int to = Math.min(jobIds.size(), from + per);

        int slot = 1;
        for (int i = from; i < to; i++) {
            String jobId = jobIds.get(i);
            inv.setItem(slot++, items.jobSelectButton(jobId,
                    s.selectedJob() != null && s.selectedJob().id().equals(jobId)
            ));
        }

        // 填充空位
        while (slot <= 5) {
            inv.setItem(slot++, null);
        }

        inv.setItem(6, items.jobPageNextButton(page + 1));
    }

    private void fillJobsInventory(PlayerInventory inv, PlayerSession s, GlobalConfig cfg) {
        // 物品栏形式：把职业按钮塞在 9..(9+n)
        List<String> jobIds = jobs.getAllJobIds();
        int base = 9;
        int max = Math.min(jobIds.size(), 27); // 起步阶段简单一点：最多塞 27 个

        for (int i = 0; i < 27; i++) {
            inv.setItem(base + i, null);
        }

        for (int i = 0; i < max; i++) {
            String jobId = jobIds.get(i);
            inv.setItem(base + i, items.jobSelectButton(jobId,
                    s.selectedJob() != null && s.selectedJob().id().equals(jobId)
            ));
        }

        // 仍然在 hotbar 放一个“下一页”（你要做真正翻页 UI 的话后续再扩展）
        inv.setItem(6, items.jobPageNextButton(1));
    }

    public void applyDeathKit(Player p, PlayerSession s, GlobalConfig cfg) {
        clear(p);

        var inv = p.getInventory();

        // 死亡大厅同样提供职业选择
        switch (cfg.ui().lobby().jobSelectMode()) {
            case HOTBAR -> fillJobHotbar(inv, s, cfg);
            case INVENTORY -> fillJobsInventory(inv, s, cfg);
        }

        // DEATH：快捷栏 8 改为“返回大厅”
        inv.setItem(8, items.backToHubButton());
    }

    public int totalJobs() {
        return jobs.getAllJobIds().size();
    }
}
