package top.ourisland.creepersiarena.defaultcontent.death;

import top.ourisland.creepersiarena.api.game.death.DeathCauseId;

public final class DefaultContentDeathCauses {

    private DefaultContentDeathCauses() {
    }

    public static DeathCauseId creeperFireworkCrossbow() {
        return DeathCauseId.skill("creeper", "firework_crossbow");
    }

    public static DeathCauseId creeperFireworkMount() {
        return DeathCauseId.skill("creeper", "firework_mount");
    }

    public static DeathCauseId moisonArrow1() {
        return DeathCauseId.skill("moison", "arrow_1");
    }

    public static DeathCauseId moisonArrow2() {
        return DeathCauseId.skill("moison", "arrow_2");
    }

    public static DeathCauseId avengerNormalHit() {
        return DeathCauseId.skill("avenger", "normal_hit");
    }

    public static DeathCauseId avengerStrongHit() {
        return DeathCauseId.skill("avenger", "strong_hit");
    }

    public static DeathCauseId bloodlineSkill1() {
        return DeathCauseId.skill("bloodline", "skill_1");
    }

    public static DeathCauseId bloodlineSkill2() {
        return DeathCauseId.skill("bloodline", "skill_2");
    }

    public static DeathCauseId golemSkill1() {
        return DeathCauseId.skill("golem", "skill_1");
    }

    public static DeathCauseId golemFangs() {
        return DeathCauseId.skill("golem", "fangs");
    }

    public static DeathCauseId wolongFan() {
        return DeathCauseId.skill("wolong", "fan");
    }

    public static DeathCauseId wolongArrow() {
        return DeathCauseId.skill("wolong", "arrow");
    }

    public static DeathCauseId ysahanRod() {
        return DeathCauseId.skill("ysahan", "rod");
    }

    public static DeathCauseId ysahanFishExplosion() {
        return DeathCauseId.skill("ysahan", "fish_explosion");
    }

    public static boolean isCreeperExplosion(DeathCauseId causeId) {
        return creeperExplosionEnemy().equals(causeId)
                || creeperExplosionFriendly().equals(causeId)
                || creeperExplosionSelf().equals(causeId);
    }

    public static DeathCauseId creeperExplosionEnemy() {
        return DeathCauseId.skill("creeper", "explosion_enemy");
    }

    public static DeathCauseId creeperExplosionFriendly() {
        return DeathCauseId.skill("creeper", "explosion_friendly");
    }

    public static DeathCauseId creeperExplosionSelf() {
        return DeathCauseId.skill("creeper", "explosion_self");
    }

}
