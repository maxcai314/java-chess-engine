package game.moves;

import game.*;

public record Promotion(
        Piece newPiece,
        Piece oldPiece,
        BoardCoordinate from,
        BoardCoordinate to
) implements PlayerMove {

    public static Promotion[] allPromotions(Piece piece, BoardCoordinate from, BoardCoordinate to) {
        return new Promotion[] {
                new Promotion(new Piece(piece.owner(), PieceType.QUEEN), piece, from, to),
                new Promotion(new Piece(piece.owner(), PieceType.ROOK), piece, from, to),
                new Promotion(new Piece(piece.owner(), PieceType.BISHOP), piece, from, to),
                new Promotion(new Piece(piece.owner(), PieceType.KNIGHT), piece, from, to)
        };
    }

    @Override
    public void execute(Board board) {
        board.placePiece(newPiece, to);
        board.removePiece(from);

        board.switchTurn();

        board.incrementHalfMoves();
        board.resetHalfMoves(); // promotions are ALWAYS pawn pushes
    }

    @Override
    public boolean isPossible(Board board) {
        if (!from.isValid() || !to.isValid()) return false;
        if (from.rank() != getPlayer().opponent().pawnRank()) return false;
        if (to.rank() != getPlayer().opponent().homeRank()) return false;
        if (!board.isEmpty(to) && board.pieceAt(to).owner() == getPlayer()) return false;

        if (oldPiece == null) return false;
        if (newPiece == null) return false;
        if (oldPiece.type() != PieceType.PAWN) return false;
        if (!oldPiece.equals(board.pieceAt(from))) return false;
        if (oldPiece.owner() != newPiece.owner()) return false;
        if (newPiece.type() == PieceType.PAWN) return false;
        if (newPiece.type() == PieceType.KING) return false;

        return true;
    }

    @Override
    public Piece piece() {
        return oldPiece;
    }

    @Override
    public BoardCoordinate from() {
        return from;
    }

    @Override
    public BoardCoordinate to() {
        return to;
    }

    @Override
    public Player getPlayer() {
        return oldPiece.owner();
    }

    public Piece getNewPiece() {
        return newPiece;
    }

    @Override
    public String toString() {
        return String.format("%s%s=%s", from, to, newPiece.toChar());
    }
}
