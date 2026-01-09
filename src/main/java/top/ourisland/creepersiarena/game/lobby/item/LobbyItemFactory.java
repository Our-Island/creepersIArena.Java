package top.ourisland.creepersiarena.game.lobby.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class LobbyItemFactory {
    private final LobbyItemCodec codec;

    public LobbyItemFactory(LobbyItemCodec codec) {
        this.codec = codec;
    }

    public ItemStack jobSelectButton(String jobId, boolean selected) {
        ItemStack it = new ItemStack(selected ? Material.LIME_DYE : Material.GRAY_DYE);
        var meta = it.getItemMeta();
        meta.displayName(text("选择职业: " + jobId));
        meta.lore(List.of(Component.text(selected ? "已选择" : "点击选择")));
        it.setItemMeta(meta);
        codec.markJobId(it, jobId);
        return it;
    }

    private static Component text(String s) {
        return Component.text(s);
    }

    public ItemStack jobPageNextButton(int nextPage) {
        ItemStack it = new ItemStack(Material.ARROW);
        var meta = it.getItemMeta();
        meta.displayName(text("下一页"));
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
        meta.displayName(text("切换队伍"));
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
        ItemStack it = new ItemStack(Material.OAK_DOOR);
        var meta = it.getItemMeta();
        meta.displayName(text("返回大厅"));
        meta.lore(List.of(Component.text("右键返回大厅")));
        it.setItemMeta(meta);
        codec.markAction(it, LobbyAction.BACK_TO_HUB);
        return it;
    }
}
