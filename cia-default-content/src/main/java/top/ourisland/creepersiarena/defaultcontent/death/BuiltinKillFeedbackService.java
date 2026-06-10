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
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilityChecks;
import top.ourisland.creepersiarena.game.GameManager;

/**
 * Default-content presentation for resolved kills.
 * <p>
 * Core owns death resolution, stats, streak calculation and death messages. This listener intentionally only consumes
 * the resolved {@link DeathResult}: it gives the killer default feedback effects and sounds without recomputing streaks
 * or broadcasting another death message.
 */
public final class BuiltinKillFeedbackService implements Listener {

    private static final int REGENERATION_TICKS = 3 * 20;

    private final GameManager gameManager;

    public BuiltinKillFeedbackService(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeathResolved(ArenaPlayerDeathResolvedEvent event) {
        DeathResult result = event.result();
        if (!result.hasKiller() || result.selfKill()) return;

        var killer = result.killer();
        if (killer == null || !killer.isOnline()) return;
        if (!DefaultContentAbilityChecks.enabled(
                gameManager == null ? null : gameManager.runtime(),
                gameManager == null ? null : gameManager.active(),
                killer,
                DefaultContentAbilities.KILL_FEEDBACK,
                null,
                "kill_feedback"
        )) return;

        giveKillReward(killer);
        playKillSounds(killer, result.killerStreakAfterKill());
    }

    private void giveKillReward(Player killer) {
        killer.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                REGENERATION_TICKS,
                2,
                true,
                true,
                true
        ));
    }

    private void playKillSounds(Player killer, int streak) {
        play(killer, "minecraft:entity.player.attack.strong", 80.0F, 0.0F);
        play(killer, "minecraft:block.anvil.land", 1.0F, 1.0F);

        if (streak >= 2) play(killer, "minecraft:entity.warden.sonic_boom", 1.0F, 2.0F);
        if (streak >= 3) play(killer, "minecraft:entity.player.breath", 1.0F, 0.0F);
        if (streak >= 4) play(killer, "minecraft:entity.ender_dragon.growl", 1.0F, 1.5F);
        if (streak >= 6) play(killer, "minecraft:entity.wither.ambient", 1.0F, 0.2F);
        if (streak >= 7) play(killer, "minecraft:entity.wither.death", 1.0F, 0.0F);
        if (streak >= 9) play(killer, "minecraft:ui.toast.challenge_complete", 1.0F, 1.0F);
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
