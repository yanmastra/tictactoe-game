package com.yanmastra.tictactoe.validators;

import com.yanmastra.tictactoe.models.State;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class GameValidatorService implements Validator {

    private final Map<String, Set<Validator>> validators = new HashMap<>();

    @Override
    public void reset(String sessionId) {
        for (Validator val: validators.get(sessionId)) val.reset(sessionId);
    }

    @Override
    public State onValidate(String sessionId, int x, int y, State state, int squareNum) {
        for (Validator val: validators.get(sessionId)) {
            State result = val.onValidate(sessionId, x, y, state, squareNum);
            log.info("on validating result of "+val.getClass().getSimpleName()+": "+result);
            if (result != State.NONE) return result;
        }
        return State.NONE;
    }

    public void addValidator(String sessionId, Validator validator) {
        this.validators.computeIfAbsent(sessionId, key -> new HashSet<>()).add(validator);
    }

    public void removeValidator(String sessionId) {
        this.validators.remove(sessionId);
    }
}
