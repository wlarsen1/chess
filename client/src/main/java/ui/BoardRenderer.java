package ui;

import chess.ChessBoard;
import chess.ChessGame.TeamColor;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class BoardRenderer {

    public static String render(ChessBoard board, TeamColor perspective) {
        boolean flipped = perspective == TeamColor.BLACK;
        String files = flipped
                ? "    h  g  f  e  d  c  b  a"
                : "    a  b  c  d  e  f  g  h";

        StringBuilder output = new StringBuilder();
        output.append(files).append('\n');

        for (int displayRow = 0; displayRow < 8; displayRow++) {
            int rank = flipped ? displayRow + 1 : 8 - displayRow;
            output.append(' ').append(rank).append(' ');

            for (int displayCol = 0; displayCol < 8; displayCol++) {
                int file = flipped ? 8 - displayCol : displayCol + 1;
                ChessPiece piece = board.getPiece(new ChessPosition(rank, file));
                appendSquare(output, rank, file, piece);
            }

            output.append(' ').append(rank).append('\n');
        }

        return output.append(files).append('\n').toString();
    }

    private static void appendSquare(
            StringBuilder output, int rank, int file, ChessPiece piece) {
        boolean lightSquare = (rank + file) % 2 != 0;
        output.append(lightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_DARK_GREY);

        if (piece == null) {
            output.append("   ");
        } else {
            boolean white = piece.getTeamColor() == TeamColor.WHITE;
            output.append(white ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE);
            output.append(' ').append(pieceLetter(piece)).append(' ');
        }

        output.append(RESET_TEXT_COLOR).append(RESET_BG_COLOR);
    }

    private static char pieceLetter(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> 'K';
            case QUEEN -> 'Q';
            case BISHOP -> 'B';
            case KNIGHT -> 'N';
            case ROOK -> 'R';
            case PAWN -> 'P';
        };
    }
}
