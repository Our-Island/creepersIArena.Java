package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.game.mode.impl.steal.config.StealArenaConfig;
import top.ourisland.creepersiarena.game.mode.impl.steal.config.StealModeConfig;
import top.ourisland.creepersiarena.game.mode.impl.steal.model.StealTeam;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StealStateTest {

    @Test
    void tracksParticipantsTeamsAliveAndCooldownsWithoutBukkitRuntime() {
        var state = state();
        UUID red = UUID.randomUUID();
        UUID blue = UUID.randomUUID();

        state.participants.add(red);
        state.participants.add(blue);
        state.alive.add(red);
        state.alive.add(blue);
        state.setTeam(red, StealTeam.RED);
        state.setTeam(blue, StealTeam.BLUE);
        state.startMineCooldown(blue, 2);

        assertTrue(state.isParticipant(red));
        assertEquals(1, state.participantsOn(StealTeam.RED));
        assertEquals(1, state.livingOn(StealTeam.BLUE));
        assertEquals(2, state.mineCooldown(blue));

        state.tickMineCooldowns();
        assertEquals(1, state.mineCooldown(blue));
        state.tickMineCooldowns();
        assertEquals(0, state.mineCooldown(blue));

        state.markDead(red);
        assertFalse(state.isAlive(red));
        assertEquals(0, state.livingOn(StealTeam.RED));
    }

    private StealState state() {
        return new StealState(
                new StealModeConfig(2, true, 15, 11, 10, 7, 180, 10, 4, 5, 10, 3, false, false),
                new StealArenaConfig(List.of(), List.of(), List.of())
        );
    }

    @Test
    void wholeGameResetClearsModeOwnedRuntimeCollections() {
        var state = state();
        UUID player = UUID.randomUUID();
        state.phase = StealPhase.ROUND_PLAYING;
        state.remainingSeconds = 99;
        state.roundIndex = 3;
        state.redWins = 2;
        state.blueWins = 1;
        state.minedBlocks = 7;
        state.targetMineCount = 3;
        state.participants.add(player);
        state.alive.add(player);
        state.setTeam(player, StealTeam.BLUE);
        state.startMineCooldown(player, 3);

        state.resetWholeGame();

        assertEquals(StealPhase.LOBBY, state.phase);
        assertEquals(0, state.remainingSeconds);
        assertEquals(0, state.roundIndex);
        assertEquals(0, state.redWins);
        assertEquals(0, state.blueWins);
        assertEquals(0, state.minedBlocks);
        assertEquals(10, state.targetMineCount);
        assertTrue(state.participants.isEmpty());
        assertTrue(state.alive.isEmpty());
        assertTrue(state.teams.isEmpty());
        assertTrue(state.mineCooldowns.isEmpty());
    }

}
