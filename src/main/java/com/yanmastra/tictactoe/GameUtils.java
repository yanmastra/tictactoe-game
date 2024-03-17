package com.yanmastra.tictactoe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanmastra.tictactoe.models.GameSession;
import com.yanmastra.tictactoe.models.State;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@Service
public class GameUtils {
    private static final Logger log = LogManager.getLogger(GameUtils.class);

    @Autowired
    ObjectMapper objectMapper;

    public <E> E fromJson(String json, Class<E> eClass) {
        try {
            return objectMapper.readValue(json, eClass);
        } catch (JsonProcessingException e) {
            log.error(e);
            return null;
        }
    }

    public <E> E fromJson(String json, TypeReference<E> eClass) {
        try {
            return objectMapper.readValue(json, eClass);
        } catch (JsonProcessingException e) {
            log.error(e);
            return null;
        }
    }

    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error(e);
            return "";
        }
    }

    public ResponseEntity<String> responseWeb(String content) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ResourceUtils.getFile("classpath:templates/index.html"))))) {
            StringBuilder resultBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if ("${content}".equals(line)) {
                    resultBuilder.append(content);
                } else {
                    resultBuilder.append(line.trim());
                }
                resultBuilder.append("\n");
            }
            return ResponseEntity.ok(resultBuilder.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public synchronized void loop(int squareNum, LooperBoard looperBoard) {
        int number = 1;
        for (int y = (squareNum-1); y >= 0; y--) {
            for (int x = 0; x < squareNum; x++) {
                if (!looperBoard.onPosition(x, y, number)) return;
                number ++;
            }
        }
    }

    public interface LooperBoard {
        /**
         * to do lopping based on game
         * @param x
         * @param y
         * @param number
         * @return false to break the loop
         */
         boolean onPosition(int x, int y, int number);
    }

    public String boardView(State state, int number, int x, int y, int width) {
        String btnType = switch (state) {
            case PLAYER_O -> "background-color: #218838; color: white;";
            case PLAYER_X -> "background-color: #138496; color: white;";
            case NONE -> "background-color: #e2e6ea; color: #808080;";
        };
        String textColor = switch (state) {
            case PLAYER_O, PLAYER_X -> "color: white;";
            case NONE -> "color: #808080;";
        };
        String btnText = switch (state) {
            case PLAYER_O -> "O";
            case PLAYER_X -> "X";
            case NONE -> ""+number;
        };
        String style = "display:block; padding: "+(width / 2)+"px;"+textColor;
        return "<td class='board' style=\"%s\"><a href=\"/mark?x=%s&y=%s\"><span style=\"%s\"> %s </span></a></td>".formatted(btnType, x, y, style, btnText);
    }

    public String createGame(int squareNum, Set<String> rules, GameSession gameSession) {
        gameSession.squareNum = squareNum;
        gameSession.boards = new State[squareNum][squareNum];
        gameSession.sessionId = UUID.randomUUID().toString();
        gameSession.playerPlaying = State.PLAYER_O;
        gameSession.totalBoard = squareNum * squareNum;
        gameSession.rules = rules;

        this.loop(squareNum, (x, y, number) -> {
            gameSession.boards[x][y] = State.NONE;
            return true;
        });
        return toSession(gameSession);
    }

    public GameSession fromSession(String session) {
        String json = new String(Base64.getDecoder().decode(session.getBytes(StandardCharsets.UTF_8)));
        return this.fromJson(json, GameSession.class);
    }

    public String toSession(GameSession gameSession) {
        String json = this.toJson(gameSession);
        LogManager.getLogger(GameUtils.class).info("gameSession:"+json);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public ResponseEntity<String> createBeginningGame(String message) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("<div class='row justify-content-md-center'>");
        contentBuilder.append("<div class='col-lg-12'>");
        contentBuilder.append("<h2>Welcome to Tic-tac-toe Game</h2><br/><br/>");
        contentBuilder.append("<div class='card'><div class='card-body'><div class='row'><div class='col-lg-6 col-md-12'>");
        contentBuilder.append("<form method='post' action='play'>");

        contentBuilder.append("<div class='form-group'>");
        contentBuilder.append("<label for=\"square\">Please input how many ? x ? squares would you like to play the game ?</label>");
        contentBuilder.append("<input id=\"square\" type='number' name='square' min='3' max='15' class='form-control' placeholder='Minimum value is 3, maximum value is 15' required />");
        contentBuilder.append("</div><br/>");


        if (StringUtils.isNotBlank(message)) {
            contentBuilder.append("<br/><span style=\"color: red\">").append(message).append("</span><br/>");
        }

        contentBuilder.append("\n<div class='form-group'>\n<label>How is the player winning the Game ?</label>\n");
        contentBuilder.append("<div class='form-check'>\n");
        contentBuilder.append("<input id=\"rule\" type='checkbox' name='rule' value='RULE_COLUMN' class='form-check-input' checked><label class=\"form-check-label\">Get all squares in a column line</label></input>\n");
        contentBuilder.append("</div><div class='form-check'>\n");
        contentBuilder.append("<input id=\"rule\" type='checkbox' name='rule' value='RULE_ROW' class='form-check-input' checked><label class=\"form-check-label\">Get all squares in a row line</label></input>\n");
        contentBuilder.append("</div><div class='form-check'>\n");
        contentBuilder.append("<input id=\"rule\" type='checkbox' name='rule' value='RULE_DIAGONAL' class='form-check-input' checked><label class=\"form-check-label\">Get all squares in a diagonal line</label></input>\n");
        contentBuilder.append("</div></div><br/>");

        contentBuilder.append("<button type=\"submit\" class=\"btn btn-primary\"> START GAME </button>");
        contentBuilder.append("</form");
        contentBuilder.append("</div></div></div></div>");
        contentBuilder.append("</div>");
        contentBuilder.append("</div>");
        return responseWeb(contentBuilder.toString());
    }

    public ResponseEntity<String> createGameBoard(GameSession session) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("<div class='row justify-content-md-center'>");
        contentBuilder.append("<div class='col-lg-8 col-md-12'>");
        contentBuilder.append("<h2>Tic-tac-toe Game</h2><br/><br/>");
        contentBuilder.append("<div class='card'><div class='card-body'><center>").append("\n");

        if (session.winner == State.NONE && session.usedBoard < session.totalBoard) {
            contentBuilder.append("<h4>Player %s is playing </h4>".formatted(session.playerPlaying == State.PLAYER_O ? "O" : session.playerPlaying == State.PLAYER_X ? "X" : " - "));
        } else if (session.winner != State.NONE) {
            contentBuilder.append("<h4>Player %s is Win  </h4>".formatted(session.winner == State.PLAYER_O ? "O" : session.winner == State.PLAYER_X ? "X" : " - "));
        } else {
            contentBuilder.append("<h4> GAME IS DRAW </h4>");
        }

        contentBuilder.append("<br/><p style=\"text-align: justify;\">Game Rule:<ol>\n");
        for (String rule: session.rules) {
            switch (rule) {
                case "RULE_COLUMN" -> contentBuilder.append("<li>Get all squares in a column line</li>");
                case "RULE_ROW" -> contentBuilder.append("<li>Get all squares in a row line</li>");
                case "RULE_DIAGONAL" -> contentBuilder.append("<li>Get all squares in a diagonal line</li>");
            }
        }
        contentBuilder.append("<br/></ol></p>\n");

        contentBuilder.append("<br/>\n");
        contentBuilder.append("<table border='0'>");

        int width = Math.min (80, (600 / session.squareNum) - 20);
        loop(session.squareNum, (x, y, number) -> {
            if (x == 0)
                contentBuilder.append("<tr>");

            contentBuilder.append(boardView(session.boards[x][y], number, x, y, width));
            if (x == (session.squareNum -1)) {
                contentBuilder.append("</tr>\n");
            }
            return true;
        });

        contentBuilder.append("</table>");

        if (session.winner != State.NONE || session.usedBoard == session.totalBoard) {
            contentBuilder.append("<br/><a href='start-new'><button class='btn btn-lg btn-primary'> START NEW GAME </button> </a>");
        } else {
            contentBuilder.append("<br/><a href='start-new'><button class='btn btn-lg btn-light'> RESET GAME </button> </a>");
        }
        contentBuilder.append("</center></div></div>");
        contentBuilder.append("</div>");
        contentBuilder.append("</div>");
        return responseWeb(contentBuilder.toString());
    }
}
