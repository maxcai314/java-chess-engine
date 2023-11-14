package game.moves;

import game.*;

public final class Promotion extends PlayerMove {
    private final Piece newPiece;

    private Promotion(Piece piece, BoardCoordinate from, BoardCoordinate to, Piece newPiece) {
        super(piece, from, to);
        this.newPiece = newPiece;
    }

    public static Promotion fromFile(Player player, int oldFile, int newFile, Piece newPiece) {
        return new Promotion(new Piece(player, PieceType.PAWN), new BoardCoordinate(player.opponent().pawnRank(), oldFile), new BoardCoordinate(player.opponent().homeRank(), newFile), newPiece);
    }

    @Override
    public void makeMove(Piece[][] board) {
        board[to.rank()][to.file()] = newPiece;
        board[from.rank()][from.file()] = null;
    }

    @Override
    public boolean isPossible(Piece[][] board) {
        return super.isPossible(board) && newPiece.type() != PieceType.PAWN && newPiece.type() != PieceType.KING && newPiece.owner() == getPlayer();
    }

    public static Promotion[] allPromotions(Player player, int oldFile, int newFile) {
        return new Promotion[] {
            Promotion.fromFile(player, oldFile, newFile, new Piece(player, PieceType.QUEEN)),
            Promotion.fromFile(player, oldFile, newFile, new Piece(player, PieceType.ROOK)),
            Promotion.fromFile(player, oldFile, newFile, new Piece(player, PieceType.BISHOP)),
            Promotion.fromFile(player, oldFile, newFile, new Piece(player, PieceType.KNIGHT))
        };
    }
}