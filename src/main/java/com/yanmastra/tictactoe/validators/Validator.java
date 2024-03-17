package com.yanmastra.tictactoe.validators;

import com.yanmastra.tictactoe.models.State;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Validator {
    Logger log = LogManager.getLogger(Validator.class);
    State onValidate(String sessionId, int x, int y, State state, int squareNum);
    void reset(String sessionId);
}
