package game.moves;

import game.*;

public final class Promotion extends PlayerMove {
    private final Piece newPiece;
    private final Piece oldPiece;
    private final BoardCoordinate from;
    private final BoardCoordinate to;

    public Promotion(Piece newPiece, Piece oldPiece, BoardCoordinate from, BoardCoordinate to) {
        this.newPiece = newPiece;
        this.oldPiece = oldPiece;
        this.from = from;
        this.to = to;
    }

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
        else if (board.pieceAt(to) != null) return false;

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
    public Piece getPiece() {
        return oldPiece;
    }

    @Override
    public BoardCoordinate getFrom() {
        return from;
    }

    @Override
    public BoardCoordinate getTo() {
        return to;
    }

    @Override
    public Player getPlayer() {
        return oldPiece.owner();
    }

    public Piece getNewPiece() {
        return newPiece;
    }
}
