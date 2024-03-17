package com.yanmastra.tictactoe.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameSession {
    @JsonProperty("session_id")
    public String sessionId;
    @JsonProperty("square_num")
    public int squareNum;
    @JsonProperty("used_board")
    public int usedBoard = 0;
    @JsonProperty("total_board")
    public int totalBoard = 0;
    @JsonProperty("boards")
    public State[][] boards;
    @JsonProperty("winner")
    public State winner = State.NONE;
    @JsonProperty("player_playing")
    public State playerPlaying = State.NONE;
    @JsonProperty("rule")
    public Set<String> rules;
}
