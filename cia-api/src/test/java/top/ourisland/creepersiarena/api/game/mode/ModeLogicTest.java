package top.ourisland.creepersiarena.api.game.mode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class ModeLogicTest {

    @Test
    void nullPlayerFlowFallsBackToDefaultFlow() {
        var logic = new ModeLogic(null, null, null);

        assertSame(IModePlayerFlow.DEFAULT, logic.playerFlow());
    }

    @Test
    void twoArgumentConstructorUsesDefaultPlayerFlow() {
        var logic = new ModeLogic(null, null);

        assertSame(IModePlayerFlow.DEFAULT, logic.playerFlow());
    }

}
