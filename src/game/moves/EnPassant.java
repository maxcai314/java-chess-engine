package game.moves;

import game.*;

public final class EnPassant extends PlayerMove {
    private final Piece capturedPawn;
    private final BoardCoordinate capturedPawnCoordinates;

    private EnPassant(Piece pawn, BoardCoordinate from, BoardCoordinate to, Piece capturedPawn, BoardCoordinate capturedPawnCoordinates) {
        super(pawn, from, to);
        this.capturedPawn = capturedPawn;
        this.capturedPawnCoordinates = capturedPawnCoordinates;
    }

    public static EnPassant enPassant(Player player, BoardCoordinate from, BoardCoordinate to) {
        return new EnPassant(new Piece(player, PieceType.PAWN), from, to, new Piece(player.opponent(), PieceType.PAWN), new BoardCoordinate(from.rank(), to.file()));
    }

    @Override
    public void makeMove(Piece[][] board) {
        super.makeMove(board);
        board[capturedPawnCoordinates.rank()][capturedPawnCoordinates.file()] = null;
    }

    @Override
    public boolean isPossible(Piece[][] board) {
        if (!capturedPawn.equals(board[capturedPawnCoordinates.rank()][capturedPawnCoordinates.file()]))
            return false;
        if (board[getTo().rank()][getTo().file()] != null)
            return false;
        return super.isPossible(board);
    }

    public Piece getCapturedPawn() {
        return capturedPawn;
    }

    public BoardCoordinate getCapturedPawnCoordinates() {
        return capturedPawnCoordinates;
    }
}
