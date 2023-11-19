package game.moves;

import game.*;

public final class EnPassant extends PlayerMove {
    private final Piece pawn;
    private final BoardCoordinate from;
    private final BoardCoordinate to;
    private final Piece capturedPawn;
    private final BoardCoordinate capturedPawnCoordinates;

    private EnPassant(Piece pawn, BoardCoordinate from, BoardCoordinate to, Piece capturedPawn, BoardCoordinate capturedPawnCoordinates) {
        this.pawn = pawn;
        this.from = from;
        this.to = to;
        this.capturedPawn = capturedPawn;
        this.capturedPawnCoordinates = capturedPawnCoordinates;
    }

    public static EnPassant enPassant(Player player, BoardCoordinate from, BoardCoordinate to) {
        return new EnPassant(new Piece(player, PieceType.PAWN), from, to, new Piece(player.opponent(), PieceType.PAWN), new BoardCoordinate(from.rank(), to.file()));
    }

    @Override
    public void execute(Board board) {
        board.placePiece(pawn, to);
        board.removePiece(from);
        board.removePiece(capturedPawnCoordinates);

        board.switchTurn();

        board.incrementNumMoves();
        board.resetHalfMoves(); // en passant is ALWAYS capture and pawn push
    }

    @Override
    public boolean isPossible(Board board) {
        if (!from.isValid() || !to.isValid()) return false;
        else if (!board.isEmpty(to)) return false;

        if (pawn == null || pawn.type() != PieceType.PAWN) return false;
        if (capturedPawn == null || capturedPawn.type() != PieceType.PAWN) return false;
        if (!pawn.equals(board.pieceAt(from))) return false;
        if (!capturedPawn.equals(board.pieceAt(capturedPawnCoordinates))) return false;
        if (getPlayer().opponent() != capturedPawn.owner()) return false;
        return true;
    }

    @Override
    public Piece getPiece() {
        return pawn;
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
        return pawn.owner();
    }

    public Piece getCapturedPawn() {
        return capturedPawn;
    }

    public BoardCoordinate getCapturedPawnCoordinates() {
        return capturedPawnCoordinates;
    }
}
