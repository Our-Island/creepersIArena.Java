package top.ourisland.creepersiarena.api.game.mode;

public record ModeLogic(
        IModeRules rules,
        IModeTimeline timeline,
        IModePlayerFlow playerFlow
) {

    public ModeLogic(IModeRules rules, IModeTimeline timeline) {
        this(rules, timeline, IModePlayerFlow.DEFAULT);
    }

    public ModeLogic {
        if (playerFlow == null) {
            playerFlow = IModePlayerFlow.DEFAULT;
        }
    }

}
