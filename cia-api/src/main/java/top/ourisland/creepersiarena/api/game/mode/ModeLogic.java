package top.ourisland.creepersiarena.api.game.mode;

public record ModeLogic(
        IModeRules rules,
        IModeTimeline timeline
) {

}
