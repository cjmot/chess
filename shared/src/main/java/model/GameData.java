package model;

import chess.ChessGame;

public class  GameData {
    private final Integer gameID;
    private String whiteUsername;
    private String blackUsername;
    private final String gameName;
    private boolean gameOver = false;
    private final ChessGame game;

    public GameData(Integer gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
        this.game = game;
    }

    public Integer gameID() {
        return gameID;
    }

    public String whiteUsername() {
        return whiteUsername;
    }

    public String blackUsername() {
        return this.blackUsername;
    }

    public String gameName() {
        return this.gameName;
    }

    public void setWhiteUsername(String username) {
        this.whiteUsername = username;
    }

    public void setBlackUsername(String username) {
        this.blackUsername = username;
    }

    public ChessGame game() {
        return this.game;
    }

    public boolean gameOver() {
        return this.gameOver;
    }
    public void setGameOver(boolean value) {
        this.gameOver = value;
    }
}
