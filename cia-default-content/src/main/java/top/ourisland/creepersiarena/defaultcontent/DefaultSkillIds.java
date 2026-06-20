package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.skill.SkillId;

public final class DefaultSkillIds {

    public static final SkillId
            AVENGER_BLOOD_BLINK = skill("avenger/blood_blink"),
            AVENGER_REVENGE_GRASP = skill("avenger/revenge_grasp"),
            BLOODLINE_BLOOD_ORB = skill("bloodline/blood_orb"),
            BLOODLINE_LEAP = skill("bloodline/leap"),
            BLOODLINE_SPRINT = skill("bloodline/sprint"),
            CREEPER_CREEPER = skill("creeper/creeper"),
            CREEPER_CROSSBOW = skill("creeper/crossbow"),
            CREEPER_FIREWORKS = skill("creeper/fireworks"),
            GOLEM_STONEFORM = skill("golem/stoneform"),
            GOLEM_RIFT_FANGS = skill("golem/rift_fangs"),
            MOISON_BLOWGUN = skill("moison/blowgun"),
            MOISON_VOLLEY = skill("moison/volley"),
            MOISON_SHADOWSTEP = skill("moison/shadowstep"),
            MOISON_SPECTRAL_RESERVE = skill("moison/spectral_reserve"),
            WOLONG_FAN_DASH = skill("wolong/fan_dash"),
            WOLONG_SKY_LANTERN = skill("wolong/sky_lantern"),
            WOLONG_REPEATING_CROSSBOW = skill("wolong/repeating_crossbow"),
            WOLONG_EMPTY_FORT = skill("wolong/empty_fort"),
            YSAHAN_PUMPKIN_TRICK = skill("ysahan/pumpkin_trick"),
            YSAHAN_WHALE = skill("ysahan/whale"),
            YSAHAN_IT_WAS_ME = skill("ysahan/it_was_me");

    private DefaultSkillIds() {
    }

    private static SkillId skill(String path) {
        return SkillId.of(DefaultContentIds.key(path));
    }

}
