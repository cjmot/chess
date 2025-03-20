package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import java.util.*;

import static chess.ChessGame.TeamColor.*;

import static ui.EscapeSequences.*;

public class GameUI {
    private final String[][] grid;
    private final ChessBoard board;
    private final ChessGame.TeamColor color;

    public GameUI(GameData game, String color) {
        grid = new String[10][10];
        this.board = game.game().getBoard();
        this.color = color.equals("WHITE") ? WHITE : BLACK;
    }

    public String printGame() {
        StringBuilder result = new StringBuilder();

        result.append(SET_TEXT_BOLD);

        ArrayList<String> rowNames = new ArrayList<>(List.of(" 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 "));
        ArrayList<String> colNames = new ArrayList<>(List.of("   ", " h ", " g ", " f ", " e ", " d ", " c ", " b ", " a ", "   "));
        ArrayList<Integer> rows = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        ArrayList<Integer> cols = new ArrayList<>(List.of(9, 8, 7, 6, 5, 4, 3, 2, 1, 0));

        if (color.equals(WHITE)) {
            Collections.reverse(rowNames);
            Collections.reverse(colNames);
            Collections.reverse(rows);
            Collections.reverse(cols);
        }

        for (int i=0; i<=9; i++) {
            if (i == 0 || i == 9) {
                setEndRows(colNames, i, cols);
            } else {
                setRow(i, rows, cols, rowNames.get(i - 1));
            }
        }

        for (String[] strings : grid) {
            result.append(String.join(RESET_BG_COLOR, strings)).append("\n");
        }

        return result.toString();
    }

    private void setRow(int row, ArrayList<Integer> rows, ArrayList<Integer> cols, String rowName) {

        String oddsColor = SET_BG_COLOR_WHITE;
        String evensColor = SET_BG_COLOR_BLACK;
        if (row%2==1){
            oddsColor = SET_BG_COLOR_BLACK;
            evensColor = SET_BG_COLOR_WHITE;
        }

        for (int i=0; i<=9; i++) {
            if (i == 0 || i == 9) {
                grid[row][i] = RESET_TEXT_COLOR + SET_BG_COLOR_DARK_GREY + rowName + RESET_BG_COLOR;
            } else {
                String piece = getPiece(board.getPiece(new ChessPosition(rows.get(row), cols.get(i))));

                if (i%2==0) {
                    grid[row][i] = evensColor + piece;
                } else {
                    grid[row][i] = oddsColor + piece;
                }
            }
        }
    }

    private void setEndRows(ArrayList<String> colNames, int row, ArrayList<Integer> cols) {
        for (int col : cols) {
            grid[row][col] = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_LIGHT_GREY + colNames.get(col) + RESET_BG_COLOR;
        }
    }

    private String getPiece(ChessPiece boardPiece) {
        if (boardPiece == null) {
            return "   ";
        }
        String result = "";
        if (boardPiece.getTeamColor().equals(WHITE)) {
            result += SET_TEXT_COLOR_BLUE;
        } else {
            result += SET_TEXT_COLOR_LIGHT_GREY;
        }
        return result + getPieceLetter(boardPiece.getPieceType());
    }

    private String getPieceLetter(ChessPiece.PieceType pieceType) {
        return switch (pieceType) {
            case PAWN -> " P ";
            case ROOK -> " R ";
            case BISHOP -> " B ";
            case KNIGHT -> " N ";
            case QUEEN -> " Q ";
            case KING -> " K ";
        };
    }
}
