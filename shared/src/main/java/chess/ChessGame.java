package chess;

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
        return board.getPiece(startPosition).pieceMoves(board, startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (
                move == null
                || board.getPiece(move.getStartPosition()) == null
                || !validMoves(move.getStartPosition()).contains(move)
                || board.getPiece(move.getStartPosition()).getTeamColor() != turn
                || isInCheck(turn)
        ) {
            throw new InvalidMoveException("Invalid move");
        } else if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(turn, move.getPromotionPiece()));
            board.addPiece(move.getStartPosition(), null);
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
        Collection<ChessMove> kingMoves = getKingMoves(teamColor);

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

                    return checkForCheck(moves, kingMoves);
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
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
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

    /**
     * Returns the set of moves of teamColor's king
     *
     * @param teamColor the color of the king to get moves for
     * @return the set of moves of teamColor's king
     */
    private Collection<ChessMove> getKingMoves(TeamColor teamColor) {
        for (int i=1; i<=8; i++) {
            for (int j=1; j<=8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                if (piece != null
                        && piece.getPieceType() == ChessPiece.PieceType.KING
                        && piece.getTeamColor() == teamColor) {
                    return piece.pieceMoves(board, new ChessPosition(i, j));
                }
            }
        }
        return Collections.emptyList();
    }
    private TeamColor getOtherTeam(TeamColor teamColor) {
        return teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Checks if any of the moves are the same as the king's position (i.e. the king is in check)
     *
     * @param moves the moves to check
     * @param kingMoves the king's moves
     * @return true if any of the moves are the same as the king's position
     */
    private boolean checkForCheck(Collection<ChessMove> moves, Collection<ChessMove> kingMoves) {

        // Check if any of the moves are the same as the king's position
        for (ChessMove move : moves) {
            if (kingMoves != null && kingMoves.stream().anyMatch(
                    kingMove -> move.getEndPosition().equals(kingMove.getStartPosition()))
            ) {
                return true;
            }
        }
        return false;
    }
}
