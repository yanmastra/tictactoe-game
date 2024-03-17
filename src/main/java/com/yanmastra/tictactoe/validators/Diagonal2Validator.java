package com.yanmastra.tictactoe.validators;

import com.yanmastra.tictactoe.models.State;

import java.util.HashMap;
import java.util.Map;


public class Diagonal2Validator implements Validator {

    private final Map<State, Integer> scores = new HashMap<>();

    @Override
    public void reset(String sessionId) {
        scores.clear();
    }

    @Override
    public State onValidate(String sessionId, int x, int y, State state, int squareNum) {
        if (y == (squareNum -1 -x)) {
            log.info("validating diagonal 2: x:"+x+", y:"+y);
            int score = scores.computeIfAbsent(state, key -> 0);
            scores.put(state, score +1);

            if (scores.get(state) >= squareNum) return state;
        }
        return State.NONE;
    }
}
