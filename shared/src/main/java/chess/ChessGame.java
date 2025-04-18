package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor turn;
    private ChessBoard board;

    public ChessGame() {
        board = new ChessBoard();
        turn = TeamColor.WHITE;
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        TeamColor teamColor = piece.getTeamColor();

        Collection<ChessMove> origMoves = piece.pieceMoves(board, startPosition);
        ArrayList<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : origMoves) {
            ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
            board.addPiece(move.getStartPosition(), null);
            board.addPiece(move.getEndPosition(), piece);

            boolean inCheck = isInCheck(teamColor);

            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), capturedPiece);

            if (!inCheck) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        String invalidMove = "Invalid move";

        if (
                move == null
                || validMoves(move.getStartPosition()) == null
                || board.getPiece(move.getStartPosition()).getTeamColor() != turn
                || !validMoves(move.getStartPosition()).contains(move)
        ) {
            throw new InvalidMoveException(invalidMove);
        } else if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(turn, move.getPromotionPiece()));
            board.addPiece(move.getStartPosition(), null);
            setTeamTurn(getOtherTeam(turn));
        } else {
            board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
            board.addPiece(move.getStartPosition(), null);
            setTeamTurn(getOtherTeam(turn));
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = getKingPosition(teamColor);

        TeamColor otherTeam = getOtherTeam(teamColor);

        // Loop through game board
        for (int i=1; i<=8; i++) {
            for (int j=1; j<=8; j++) {

                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);

                // If piece is not null and is of the other team
                if (piece != null && piece.getTeamColor() == otherTeam) {
                    // Get all possible moves for the piece
                    Collection<ChessMove> moves = board.getPiece(position).pieceMoves(board, position);

                    if (checkForCheck(moves, kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        Collection<ChessMove> moves = new ArrayList<>();

        for (int i=1; i<=8; i++) {
            for (int j=1; j<=8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && teamColor == piece.getTeamColor()) {
                    if (!validMoves(new ChessPosition(i, j)).isEmpty()) {
                        return false;
                    }
                    moves.addAll(validMoves(position));
                }
            }
        }
        return moves.isEmpty() && isInCheck(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        for (int i=1; i<=8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                if (piece != null
                        && piece.getTeamColor() == teamColor
                        && !validMoves(new ChessPosition(i, j)).isEmpty()
                ) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private ChessPosition getKingPosition(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        for (int i=1; i<=8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                if (piece != null
                        && piece.getTeamColor() == teamColor
                        && piece.getPieceType() == ChessPiece.PieceType.KING
                ) {
                    kingPosition = new ChessPosition(i, j);
                }
            }
        }
        return kingPosition;
    }

    private TeamColor getOtherTeam(TeamColor teamColor) {
        return teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Checks if any of the moves are the same as the king's position (i.e. the king is in check)
     *
     * @param moves the moves to check
     * @param kingPosition the king's position
     * @return true if the king is in check
     */
    private boolean checkForCheck(Collection<ChessMove> moves, ChessPosition kingPosition) {

        // Check if any of the moves are the same as the king's position
        for (ChessMove move : moves) {
            if (move.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }
}
