package top.ourisland.creepersiarena.defaultcontent.death;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.ourisland.creepersiarena.api.game.death.DeathResult;
import top.ourisland.creepersiarena.api.game.event.ArenaPlayerDeathResolvedEvent;

/**
 * Default-content presentation for resolved kills.
 * <p>
 * Core owns death resolution, stats, streak calculation and death messages. This listener intentionally only consumes
 * the resolved {@link DeathResult}: it gives the killer the legacy feedback effects and sounds without recomputing
 * streaks or broadcasting another death message.
 */
public final class BuiltinKillFeedbackService implements Listener {

    private static final int REGENERATION_TICKS = 3 * 20;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeathResolved(ArenaPlayerDeathResolvedEvent event) {
        DeathResult result = event.result();
        if (!result.hasKiller() || result.selfKill()) return;

        var killer = result.killer();
        if (killer == null || !killer.isOnline()) return;

        giveKillReward(killer);
        playKillSounds(killer, result.killerStreakAfterKill());
    }

    private void giveKillReward(Player killer) {
        killer.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                REGENERATION_TICKS,
                1,
                true,
                true,
                true
        ));
    }

    private void playKillSounds(Player killer, int streak) {
        play(killer, "minecraft:entity.player.attack.strong", 1.0F, 1.0F);
        play(killer, "minecraft:block.anvil.land", 0.7F, 1.4F);

        if (streak >= 2) play(killer, "minecraft:entity.warden.sonic_boom", 0.8F, 1.0F);
        if (streak >= 3) play(killer, "minecraft:entity.player.breath", 0.8F, 1.0F);
        if (streak >= 4) play(killer, "minecraft:entity.ender_dragon.growl", 0.8F, 1.0F);
        if (streak >= 6) play(killer, "minecraft:entity.wither.ambient", 0.8F, 1.0F);
        if (streak >= 7) play(killer, "minecraft:entity.wither.death", 0.8F, 1.0F);
        if (streak >= 9) play(killer, "minecraft:ui.toast.challenge_complete", 0.8F, 1.0F);
    }

    private void play(
            Player player,
            String sound,
            float volume,
            float pitch
    ) {
        player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, volume, pitch);
    }

}
