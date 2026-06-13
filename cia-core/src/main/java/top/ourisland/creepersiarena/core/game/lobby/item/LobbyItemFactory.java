package top.ourisland.creepersiarena.core.game.lobby.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.team.TeamId;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.job.JobManager;
import top.ourisland.creepersiarena.core.utils.I18n;
import top.ourisland.creepersiarena.core.utils.LangKeyResolver;

import java.util.List;

public final class LobbyItemFactory {

    private final LobbyItemCodec codec;
    private final JobManager jobs;

    public LobbyItemFactory(
            @lombok.NonNull LobbyItemCodec codec,
            @lombok.NonNull JobManager jobs
    ) {
        this.codec = codec;
        this.jobs = jobs;
    }

    public @Nullable ItemStack jobSelectButton(
            @lombok.NonNull JobId jobId,
            @lombok.NonNull PlayerSession s
    ) {
        boolean selected = s.selectedJob() != null && s.selectedJob().equals(jobId);

        var job = jobs.getJob(jobId);
        if (job == null) return null;

        var item = job.display().clone();
        item.setAmount(1);

        var meta = item.getItemMeta();
        if (meta != null) {
            var baseName = resolveJobName(meta, jobId, jobId.asString());

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

    private Component resolveJobName(
            ItemMeta meta,
            JobId jid,
            String fallback
    ) {
        var key = LangKeyResolver.jobName(jid);
        if (I18n.has(key)) return I18n.langNP(key);
        var itemName = meta.displayName();
        return itemName != null ? itemName : Component.text(fallback);
    }

    public ItemStack jobPageNextButton(int nextPage) {
        var it = new ItemStack(Material.OAK_HANGING_SIGN);
        var meta = it.getItemMeta();
        meta.displayName(Component.text("下一页"));
        meta.lore(List.of(Component.text("右键翻到第 " + nextPage + " 页")));
        it.setItemMeta(meta);
        codec.markAction(it, LobbyAction.JOB_PAGE_NEXT);
        codec.markJobPage(it, nextPage);
        return it;
    }

    public ItemStack teamCycleButton(@Nullable TeamId team, List<TeamId> selectableTeams) {
        var it = new ItemStack(teamMaterial(team, selectableTeams));
        var meta = it.getItemMeta();

        var label = team == null ? "随机分队" : team.value();
        meta.displayName(Component.text("切换队伍"));
        meta.lore(List.of(
                Component.text("当前: " + label),
                Component.text("可选: " + selectableTeams.size()),
                Component.text("右键切换")
        ));
        it.setItemMeta(meta);

        codec.markAction(it, LobbyAction.TEAM_CYCLE);
        return it;
    }

    private Material teamMaterial(@Nullable TeamId team, List<TeamId> selectableTeams) {
        if (team == null) return Material.WHITE_WOOL;
        return switch (team.value()) {
            case "red" -> Material.RED_WOOL;
            case "blue" -> Material.LIGHT_BLUE_WOOL;
            case "green" -> Material.LIME_WOOL;
            case "yellow" -> Material.YELLOW_WOOL;
            case "aqua", "cyan" -> Material.CYAN_WOOL;
            case "purple" -> Material.PURPLE_WOOL;
            case "white" -> Material.WHITE_WOOL;
            case "black" -> Material.BLACK_WOOL;
            default -> {
                int index = selectableTeams.indexOf(team);
                Material[] colors = {
                        Material.RED_WOOL,
                        Material.LIGHT_BLUE_WOOL,
                        Material.LIME_WOOL,
                        Material.YELLOW_WOOL,
                        Material.CYAN_WOOL,
                        Material.PURPLE_WOOL,
                        Material.WHITE_WOOL,
                        Material.BLACK_WOOL
                };
                yield index < 0 ? Material.WHITE_WOOL : colors[index % colors.length];
            }
        };
    }

    public ItemStack backToHubButton() {
        var it = new ItemStack(Material.CAMPFIRE);
        var meta = it.getItemMeta();
        meta.displayName(Component.text("返回大厅"));
        meta.lore(List.of(Component.text("右键返回大厅")));
        it.setItemMeta(meta);
        codec.markAction(it, LobbyAction.BACK_TO_HUB);
        return it;
    }

}
