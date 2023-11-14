package moves;

import game.*;

public final class EnPassant extends PlayerMove {
    private final Piece capturedPawn;
    private final BoardCoordinate capturedPawnCoordinate;

    private EnPassant(Piece pawn, BoardCoordinate from, BoardCoordinate to, Piece capturedPawn, BoardCoordinate capturedPawnCoordinate) {
        super(pawn, from, to);
        this.capturedPawn = capturedPawn;
        this.capturedPawnCoordinate = capturedPawnCoordinate;
    }

    public static EnPassant enPassant(Player player, BoardCoordinate from, BoardCoordinate to) {
        return new EnPassant(new Piece(player, PieceType.PAWN), from, to, new Piece(player.opponent(), PieceType.PAWN), new BoardCoordinate(from.rank(), to.file()));
    }

    @Override
    public void makeMove(Piece[][] board) {
        super.makeMove(board);
        board[capturedPawnCoordinate.rank()][capturedPawnCoordinate.file()] = null;
    }

    public Piece getCapturedPawn() {
        return capturedPawn;
    }

    public BoardCoordinate getCapturedPawnCoordinate() {
        return capturedPawnCoordinate;
    }
}
