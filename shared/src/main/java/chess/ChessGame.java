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

        Collection<ChessMove> origMoves = piece.pieceMoves(board, startPosition);
        ArrayList<ChessMove> moves = new ArrayList<>(origMoves);

        for (ChessMove move : moves) {
            ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
            board.addPiece(move.getStartPosition(), null);
            board.addPiece(move.getEndPosition(), piece);
            if (isInCheck(turn)) {
                origMoves.remove(move);
            }

            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), capturedPiece);
        }

        return origMoves;
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
                || isInCheck(turn)
        ) {
            throw new InvalidMoveException(invalidMove);
        } else if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(turn, move.getPromotionPiece()));
            board.addPiece(move.getStartPosition(), null);
            if (isInCheck(turn)) {
                board.addPiece(move.getStartPosition(), new ChessPiece(turn, ChessPiece.PieceType.PAWN));
                board.addPiece(move.getEndPosition(), null);
                throw new InvalidMoveException(invalidMove);
            }
            setTeamTurn(getOtherTeam(turn));
        } else {
            board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
            board.addPiece(move.getStartPosition(), null);
            if (isInCheck(turn)) {
                board.addPiece(move.getStartPosition(), board.getPiece(move.getEndPosition()));
                board.addPiece(move.getEndPosition(), null);
                throw new InvalidMoveException(invalidMove);
            }
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

                    if (!checkForCheckMate(moves, kingMoves)) {
                        return false;
                    }
                }
            }
        }
        return true;
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

    private boolean checkForCheckMate(Collection<ChessMove> moves, Collection<ChessMove> kingMoves) {
        for (ChessMove move : moves) {
            if (kingMoves != null && kingMoves.stream().anyMatch(
                    kingMove -> move.getEndPosition().equals(kingMove.getEndPosition()))
            ) {
                return true;
            }
        }
        return false;
    }
}
