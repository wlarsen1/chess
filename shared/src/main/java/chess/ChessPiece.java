package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();

        switch (type) {
            case BISHOP -> addSlidingMoves(board, myPosition, moves, new int[][]{
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
            });
            case ROOK -> addSlidingMoves(board, myPosition, moves, new int[][]{
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            });
            case QUEEN -> addSlidingMoves(board, myPosition, moves, new int[][]{
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
            });
            default -> {}
        }

        return moves;
    }

    private void addSlidingMoves(ChessBoard board, ChessPosition start, List<ChessMove> moves, int[][] directions) {
        for (int[] dir : directions) {
            int row = start.getRow() + dir[0];
            int col = start.getColumn() + dir[1];

            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition target = new ChessPosition(row, col);
                ChessPiece pieceAtTarget = board.getPiece(target);

                if (pieceAtTarget == null) {
                    moves.add(new ChessMove(start, target, null));
                } else {
                    if (pieceAtTarget.getTeamColor() != this.pieceColor) {
                        moves.add(new ChessMove(start, target, null));
                    }
                    break;
                }

                row += dir[0];
                col += dir[1];
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return String.format("%s %s", pieceColor, type);
    }
}
