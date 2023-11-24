package game.moves;

import game.GameState;
import game.Piece;
import game.PieceType;
import game.Player;

import java.util.Arrays;
import java.util.stream.IntStream;

public record MoveRecord(
    PlayerMove move,
    boolean isCapture,
    boolean isCheck,
    boolean isMate,
    GameState resultingState,
    Piece[][] board,
    boolean whiteShortCastle,
    boolean whiteLongCastle,
    boolean blackShortCastle,
    boolean blackLongCastle,
    double halfMoveCount,
    double moveCount
) {
    public Player player() {
        return move.getPlayer();
    }

    public boolean positionEquals(MoveRecord that) {
        return Arrays.deepEquals(this.board, that.board);
    }

    public boolean gameEnded() {
        return resultingState != GameState.UNFINISHED;
    }

    @Override
    public String toString() {
        return switch (move) {
            case RegularMove regularMove -> {
                StringBuilder result = new StringBuilder();

                if (!(regularMove.getPiece().type() == PieceType.PAWN)) {
                    result.append(regularMove.getPiece().type().toChar());
                    // check how many pieces are in the given file
                    int fileCount = (int) IntStream.range(0, 8)
                        .mapToObj(i -> board[i][regularMove.getFrom().file()])
                        .filter(move.getPiece()::equals)
                        .count();
                    int rankCount = (int) Arrays.stream(board[regularMove.getFrom().rank()])
                        .filter(move.getPiece()::equals)
                        .count();

                    if (fileCount > 1)
                        result.append(regularMove.getFrom().toString().charAt(0)); // specify file
                    if (rankCount > 1)
                        result.append(regularMove.getFrom().toString().charAt(1)); // specify rank
                } else if (isCapture) result.append(regularMove.getFrom().toString().charAt(0)); // specify file

                if (isCapture) result.append("x");

                result.append(regularMove.getTo().toString());

                if (isMate) result.append("#");
                else if (isCheck) result.append("+");

                yield result.toString();
            }

            case Promotion promotion -> {
                StringBuilder result = new StringBuilder();

                if (isCapture()) {
                    result.append(promotion.getFrom().toString().charAt(0)) // specify file
                            .append("x");
                }

                result.append(promotion.getTo().toString())
                        .append("=")
                        .append(promotion.getNewPiece().type().toChar());

                if (isMate) result.append("#");
                else if (isCheck) result.append("+");

                yield result.toString();
            }

            default -> move.toString(); // we trust these other types to have correct toString methods
        };
    }
}
