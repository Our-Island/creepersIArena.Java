package top.ourisland.creepersiarena.defaultcontent.game.death;

import top.ourisland.creepersiarena.api.game.death.DeathCauseId;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentIds;

public final class DefaultContentDeathCauses {

    private DefaultContentDeathCauses() {
    }

    public static DeathCauseId creeperFireworkCrossbow() {
        return skill("creeper", "firework_crossbow");
    }

    private static DeathCauseId skill(String jobPath, String causePath) {
        return DeathCauseId.of(DefaultContentIds.NAMESPACE, "skill/" + jobPath + "/" + causePath);
    }

    public static DeathCauseId creeperFireworkMount() {
        return skill("creeper", "firework_mount");
    }

    public static DeathCauseId moisonArrow1() {
        return skill("moison", "arrow_1");
    }

    public static DeathCauseId moisonArrow2() {
        return skill("moison", "arrow_2");
    }

    public static DeathCauseId avengerNormalHit() {
        return skill("avenger", "normal_hit");
    }

    public static DeathCauseId avengerStrongHit() {
        return skill("avenger", "strong_hit");
    }

    public static DeathCauseId bloodlineSkill1() {
        return skill("bloodline", "skill_1");
    }

    public static DeathCauseId bloodlineSkill2() {
        return skill("bloodline", "skill_2");
    }

    public static DeathCauseId golemSkill1() {
        return skill("golem", "skill_1");
    }

    public static DeathCauseId golemFangs() {
        return skill("golem", "fangs");
    }

    public static DeathCauseId wolongFan() {
        return skill("wolong", "fan");
    }

    public static DeathCauseId wolongArrow() {
        return skill("wolong", "arrow");
    }

    public static DeathCauseId ysahanRod() {
        return skill("ysahan", "rod");
    }

    public static DeathCauseId ysahanFishExplosion() {
        return skill("ysahan", "fish_explosion");
    }

    public static boolean isCreeperExplosion(DeathCauseId causeId) {
        return creeperExplosionEnemy().equals(causeId)
                || creeperExplosionFriendly().equals(causeId)
                || creeperExplosionSelf().equals(causeId);
    }

    public static DeathCauseId creeperExplosionEnemy() {
        return skill("creeper", "explosion_enemy");
    }

    public static DeathCauseId creeperExplosionFriendly() {
        return skill("creeper", "explosion_friendly");
    }

    public static DeathCauseId creeperExplosionSelf() {
        return skill("creeper", "explosion_self");
    }

}
