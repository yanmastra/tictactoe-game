package com.yanmastra.tictactoe.validators;

import com.yanmastra.tictactoe.models.State;

import java.util.HashMap;
import java.util.Map;

public class ColumnValidator implements Validator {
    private final Map<Integer, Map<State, Integer>> stateScore = new HashMap<>();

    @Override
    public void reset(String sessionId) {
        stateScore.clear();
    }

    @Override
    public State onValidate(String sessionId, int x, int y, State state, int squareNum) {
        if (state == State.NONE) return state;

        Map<State, Integer> scores = stateScore.computeIfAbsent(x, key -> new HashMap<>());
        int score = scores.computeIfAbsent(state, key -> 0);
        score += 1;
        scores.put(state, score);

        log.info("validating column: x:"+x+", y:"+y+", state:"+state+", squareNum:" + squareNum + ", score:"+stateScore);
        if (score == squareNum) return state;
        else return State.NONE;
    }
}
