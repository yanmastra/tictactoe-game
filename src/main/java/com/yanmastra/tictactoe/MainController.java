package com.yanmastra.tictactoe;

import com.yanmastra.tictactoe.models.GameSession;
import com.yanmastra.tictactoe.models.State;
import com.yanmastra.tictactoe.validators.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
public class MainController {
    private static final Logger log = LogManager.getLogger(MainController.class);

    @Autowired
    GameUtils gameUtils;
    @Autowired
    GameValidatorService validator;

    @GetMapping("/")
    public ResponseEntity<String> getIndex(
            @CookieValue(value = "game_session", defaultValue = "") String gameSession,
            @RequestParam(value = "message", defaultValue = "", required = false) String message
    ) {
        if (StringUtils.isBlank(gameSession))
            return gameUtils.createBeginningGame(message);

        GameSession session = gameUtils.fromSession(gameSession);
        if (session == null || StringUtils.isBlank(session.sessionId)) {
            return gameUtils.createBeginningGame(message);
        } else {
            return gameUtils.createGameBoard(session);
        }
    }

    @PostMapping("play")
    public ResponseEntity<String> play(
            @RequestParam("square") int square,
            @RequestParam(value = "rule", required = false) Set<String> rules,
            HttpServletResponse response) {
        log.info("square:"+square);


        if (rules == null || rules.isEmpty()) {
            return ResponseEntity.status(302)
                    .header("Location", "/?message=Please+select+at+least+one+of+Game+Rule!")
                    .build();
        }

        GameSession gameSession = new GameSession();
        String session = gameUtils.createGame(square, rules, gameSession);

        Cookie cookie = new Cookie("game_session", session);
        cookie.setMaxAge(900);
        response.addCookie(cookie);

        return ResponseEntity.status(302)
                .header("Location", "/")
                .build();
    }

    @GetMapping("mark")
    public ResponseEntity<String> mark(
            @RequestParam(value = "x", defaultValue = "-1") int x,
            @RequestParam(value = "y", defaultValue = "-1") int y,
            @CookieValue(value = "game_session", defaultValue = "") String gameSession,
            HttpServletResponse response
    ) {
        GameSession objGameSession = gameUtils.fromSession(gameSession);
        if (x >= 0 && x < objGameSession.squareNum
                && y >= 0 && y < objGameSession.squareNum
                && objGameSession.boards[x][y] == State.NONE
                && objGameSession.winner == State.NONE
        ) {
            objGameSession.boards[x][y] = objGameSession.playerPlaying;
            objGameSession.usedBoard += 1;

            for (String rule: objGameSession.rules) {
                switch (rule){
                    case "RULE_COLUMN" -> validator.addValidator(objGameSession.sessionId, new ColumnValidator());
                    case "RULE_ROW" -> validator.addValidator(objGameSession.sessionId, new RowValidator());
                    case "RULE_DIAGONAL" -> {
                        validator.addValidator(objGameSession.sessionId, new DiagonalValidator());
                        validator.addValidator(objGameSession.sessionId, new Diagonal2Validator());
                    }
                }
            }
            gameUtils.loop(objGameSession.squareNum, (x1, y1, number) -> {
                objGameSession.winner = validator.onValidate(objGameSession.sessionId, x1, y1, objGameSession.boards[x1][y1], objGameSession.squareNum);
                log.info("after validating: " + objGameSession.winner);
                return objGameSession.winner == State.NONE;
            });
            validator.reset(objGameSession.sessionId);

            objGameSession.playerPlaying = objGameSession.playerPlaying == State.PLAYER_O ? State.PLAYER_X : State.PLAYER_O;
            String session = gameUtils.toSession(objGameSession);
            Cookie cookie = new Cookie("game_session", session);
            cookie.setMaxAge(900);
            response.addCookie(cookie);
        }
        return ResponseEntity.status(302)
                .header("Location", "/")
                .build();
    }

    @GetMapping("start-new")
    public ResponseEntity<String> mark(@CookieValue(name = "game_session") String cookieSession, HttpServletResponse response) {
        if (!"{}".equals(cookieSession)) {
            GameSession session = gameUtils.fromSession(cookieSession);
            validator.removeValidator(session.sessionId);
        }

        Cookie cookie = new Cookie("game_session", "{}");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.status(302)
                .header("Location", "/")
                .build();
    }
}
