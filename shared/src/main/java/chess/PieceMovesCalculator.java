package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Interface outline for each class of PieceMovesCalculator
 */
public interface PieceMovesCalculator {

    Collection<ChessMove> pieceMoves(
            ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor
    );

    default Boolean isNotBlocked(
            ChessBoard board,
            ChessPosition position,
            ChessGame.TeamColor currColor,
            boolean canCapture
    ) {
        if (position.getRow() < 1 || position.getRow() > 8) {
            return false;
        } else if (position.getColumn() < 1 || position.getColumn() > 8) {
            return false;
        } else if (board.getPiece(position) == null) {
            return true;
        } else return board.getPiece(position).getTeamColor() != currColor && canCapture;
    }
}


/**
 * Calculates the moves available for the King piece
 */
class KingMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(
            ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor
    ) {

        List<ChessMove> moves = new ArrayList<>();

        int currRow = myPosition.getRow();
        int currCol = myPosition.getColumn();

        final Collection<ChessPosition> positions = getKingPositions(currRow, currCol);
        for (ChessPosition newPosition : positions) {
            if (Boolean.TRUE.equals(isNotBlocked(board, newPosition, currColor, true))) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
        return moves;
    }


    private static Collection<ChessPosition> getKingPositions(int currRow, int currCol) {
        final ChessPosition up = new ChessPosition(currRow, currCol + 1);
        final ChessPosition down = new ChessPosition(currRow, currCol - 1);
        final ChessPosition left = new ChessPosition(currRow - 1, currCol);
        final ChessPosition right = new ChessPosition(currRow + 1, currCol);
        final ChessPosition upRight = new ChessPosition(currRow + 1, currCol + 1);
        final ChessPosition upLeft = new ChessPosition(currRow - 1, currCol + 1);
        final ChessPosition downRight = new ChessPosition(currRow + 1, currCol - 1);
        final ChessPosition downLeft = new ChessPosition(currRow - 1, currCol - 1);

        return List.of(up, down, left, right, upRight, upLeft, downRight, downLeft);
    }
}

/**
 * Calculates the moves available for the Queen piece
 */
class QueenMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor) {
        throw new RuntimeException("Not implemented");
    }
}

/**
 * Calculates the moves available for the Bishop piece
 */
class BishopMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor) {
        throw new RuntimeException("Not implemented");
    }
}

/**
 * Calculates the moves available for the Knight piece
 */
class KnightMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor) {
        throw new RuntimeException("Not implemented");
    }
}

/**
 * Calculates the moves available for the Rook piece
 */
class RookMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor) {
        throw new RuntimeException("Not implemented");
    }
}

/**
 * Calculates the moves available for the Pawn piece
 */
class PawnMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(
            ChessBoard board,
            ChessPosition myPosition,
            ChessGame.TeamColor currColor
    ) {
        if (currColor == ChessGame.TeamColor.WHITE) {
            return getWhitePawnMoves(board, myPosition, currColor);
        } else return getBlackPawnMoves(board, myPosition, currColor);
    }

    /**
     * Gets the possible moves for a white pawn.
     *
     * @param board the current state of the chessboard
     * @param myPosition the current position of the pawn
     * @param currColor the color of the pawn
     * @return a collection of possible moves
     */
    private Collection<ChessMove> getWhitePawnMoves(
            ChessBoard board,
            ChessPosition myPosition,
            ChessGame.TeamColor currColor
    ) {
        ChessPosition[] positions = {
                new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1),
                new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1)
        };

        return new ArrayList<>(getMoves(board, myPosition, currColor, positions));
    }

    /**
     * Gets the possible moves for a black pawn.
     *
     * @param board the current state of the chessboard
     * @param myPosition the current position of the pawn
     * @param currColor the color of the pawn
     * @return a collection of possible moves
     */
    private Collection<ChessMove> getBlackPawnMoves(
            ChessBoard board,
            ChessPosition myPosition,
            ChessGame.TeamColor currColor
    ) {
        ChessPosition[] positions = {
                new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1),
                new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1)
        };

        return new ArrayList<>(getMoves(board, myPosition, currColor, positions));
    }

    /**
     * Gets the possible moves for a pawn.
     *
     * @param board the current state of the chessboard
     * @param myPosition the current position of the pawn
     * @param currColor the color of the pawn
     * @param positions the possible positions the pawn can move to
     * @return a collection of possible moves
     */
    private Collection<ChessMove> getMoves(
            ChessBoard board,
            ChessPosition myPosition,
            ChessGame.TeamColor currColor,
            ChessPosition[] positions
    ) {
        Collection<ChessMove> moves = new ArrayList<>();

        ChessPosition forwardOne = positions[0];
        ChessPosition forwardTwo = positions[1];
        ChessPosition forwardLeft = positions[2];
        ChessPosition forwardRight = positions[3];

        boolean whiteStart = currColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2;
        boolean blackStart = currColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7;

        // start of game logic
        if (whiteStart || blackStart) {
            if (board.getPiece(forwardOne) == null && isNotBlocked(board, forwardOne, currColor, false)) {
                moves.add(new ChessMove(myPosition, forwardOne, null));
                if (board.getPiece(forwardTwo) == null && isNotBlocked(board, forwardTwo, currColor, false)) {
                    moves.add(new ChessMove(myPosition, forwardTwo, null));
                }
            }
        // forward logic
        } else if (board.getPiece(forwardOne) == null && (forwardOne.getRow() == 8 || forwardOne.getRow() == 1)) {
            moves.addAll(getPromotions(myPosition, forwardOne));
        } else if (board.getPiece(forwardOne) == null && isNotBlocked(board, forwardOne, currColor, false)) {
            moves.add(new ChessMove(myPosition, forwardOne, null));
        }
        // get forward left moves
        moves.addAll(getDiagonals(board, myPosition, currColor, forwardLeft));
        // get forward right moves
        moves.addAll(getDiagonals(board, myPosition, currColor, forwardRight));

        return moves;
    }

    /**
     * Gets the possible promotion moves for a pawn.
     *
     * @param myPosition the current position of the pawn
     * @param newPosition the new position of the pawn
     * @return a collection of possible promotion moves
     */
    private Collection<ChessMove> getPromotions(ChessPosition myPosition, ChessPosition newPosition) {
        return List.of(
                new ChessMove(myPosition, newPosition, ChessPiece.PieceType.QUEEN),
                new ChessMove(myPosition, newPosition, ChessPiece.PieceType.BISHOP),
                new ChessMove(myPosition, newPosition, ChessPiece.PieceType.ROOK),
                new ChessMove(myPosition, newPosition, ChessPiece.PieceType.KNIGHT)
        );
    }

    /**
     * Gets the possible diagonal moves for a pawn.
     *
     * @param board the current state of the chessboard
     * @param myPosition the current position of the pawn
     * @param currColor the color of the pawn
     * @param diagonal the diagonal position to check
     * @return a collection of possible diagonal moves
     */
    private Collection<ChessMove> getDiagonals(
            ChessBoard board,
            ChessPosition myPosition,
            ChessGame.TeamColor currColor,
            ChessPosition diagonal
    ) {
        Collection<ChessMove> moves = new ArrayList<>();

        // catch out of bounds
        if (diagonal.getColumn() < 1 || diagonal.getColumn() > 8) {
            return moves;
        }

        if (board.getPiece(diagonal) != null && board.getPiece(diagonal).getTeamColor() != currColor) {
            if (diagonal.getRow() == 1 || diagonal.getRow() == 8) {
                moves.addAll(getPromotions(myPosition, diagonal));
            } else {
                moves.add(new ChessMove(myPosition, diagonal, null));
            }
        }
        return moves;
    }
}
