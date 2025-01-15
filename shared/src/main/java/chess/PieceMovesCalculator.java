package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public interface PieceMovesCalculator {

    Collection<ChessMove> pieceMoves(
            ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor
    );
    default Boolean isNotBlocked(
            ChessBoard board, ChessPosition position, ChessGame.TeamColor currColor
    ) {
        if (position.getRow() < 1 || position.getRow() > 8) {
            return false;
        } else if (position.getColumn() < 1 || position.getColumn() > 8) {
            return false;
        } else if (board.getPiece(position) == null) {
            return true;
        }
        else return board.getPiece(position).getTeamColor() != currColor;
    }
}

class KingMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(
            ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor
    ) {

        List<ChessMove> moves = new ArrayList<>();

        int currRow = myPosition.getRow();
        int currCol = myPosition.getColumn();

        final Collection<ChessPosition> positions = getPositions(currRow, currCol);
        for (ChessPosition newPosition : positions) {
            if (Boolean.TRUE.equals(isNotBlocked(board, newPosition, currColor))) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
        return moves;
    }

    private static Collection<ChessPosition> getPositions(int currRow, int currCol) {
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

class QueenMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor) {
        throw new RuntimeException("Not implemented");
    }
}

class BishopMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor) {
        throw new RuntimeException("Not implemented");
    }
}

class KnightMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor) {
        throw new RuntimeException("Not implemented");
    }
}

class RookMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor) {
        throw new RuntimeException("Not implemented");
    }
}

class PawnMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor currColor) {
        throw new RuntimeException("Not implemented");
    }
}
