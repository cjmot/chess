package chess;
import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] squares;

    public ChessBoard() {
        this.squares = new ChessPiece[8][8];
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.squares = new ChessPiece[8][8];

        for (int row = 1; row <= 8; row++){
            switch (row) {
                case 1:
                    this.setRow(row, TeamColor.WHITE);
                    break;
                case 2:
                    this.setPawns(row, TeamColor.WHITE);
                    break;
                case 7:
                    this.setPawns(row, TeamColor.BLACK);
                    break;
                case 8:
                    this.setRow(row, TeamColor.BLACK);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Sets the row of the board specified with the correct pieces
     * @param row The row of the board to put pieces
     * @param color The color of the pieces to set
     */
    private void setRow(int row, TeamColor color) {
        for (int col = 1; col<=8; col++) {
            switch (col) {
                case 1, 8:
                    this.addPiece(new ChessPosition(row, col), new ChessPiece(color, PieceType.ROOK));
                    break;
                case 2, 7:
                    this.addPiece(new ChessPosition(row, col), new ChessPiece(color, PieceType.KNIGHT));
                    break;
                case 3, 6:
                    this.addPiece(new ChessPosition(row, col), new ChessPiece(color, PieceType.BISHOP));
                    break;
                case 4:
                    this.addPiece(new ChessPosition(row, col), new ChessPiece(color, PieceType.QUEEN));
                    break;
                case 5:
                    this.addPiece(new ChessPosition(row, col), new ChessPiece(color, PieceType.KING));
                    break;
                default:
                    throw new RuntimeException("ChessBoard.setRow case fault");
            }
        }
    }

    private void setPawns(int row, TeamColor color) {
        for (int col = 1; col <= 8; col++) {
            this.addPiece(new ChessPosition(row, col), new ChessPiece(color, PieceType.PAWN));
        }
    }
}
