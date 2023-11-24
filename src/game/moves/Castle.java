package game.moves;

import game.*;

public enum Castle implements PlayerMove {
    WHITE_LONG(
            new Piece(Player.WHITE, PieceType.KING),
            new BoardCoordinate(0, 4), new BoardCoordinate(0, 2),
            new Piece(Player.WHITE, PieceType.ROOK), new BoardCoordinate(0, 0),
            new BoardCoordinate(0, 3),
            "O-O-O"
    ),
    WHITE_SHORT(
            new Piece(Player.WHITE, PieceType.KING),
            new BoardCoordinate(0, 4),
            new BoardCoordinate(0, 6),
            new Piece(Player.WHITE, PieceType.ROOK),
            new BoardCoordinate(0, 7),
            new BoardCoordinate(0, 5),
            "O-O"
    ),
    BLACK_LONG(
            new Piece(Player.BLACK, PieceType.KING),
            new BoardCoordinate(7, 4),
            new BoardCoordinate(7, 2),
            new Piece(Player.BLACK, PieceType.ROOK),
            new BoardCoordinate(7, 0),
            new BoardCoordinate(7, 3),
            "O-O-O"
    ),
    BLACK_SHORT(
            new Piece(Player.BLACK, PieceType.KING),
            new BoardCoordinate(7, 4),
            new BoardCoordinate(7, 6),
            new Piece(Player.BLACK, PieceType.ROOK),
            new BoardCoordinate(7, 7),
            new BoardCoordinate(7, 5),
            "O-O"
    );

    private final Piece king;
    private final BoardCoordinate from;
    private final BoardCoordinate to;
    private final Piece rook;
    private final BoardCoordinate rookFrom;
    private final BoardCoordinate rookTo;

    private final String notation;

    Castle(Piece king, BoardCoordinate from, BoardCoordinate to, Piece rook, BoardCoordinate rookFrom, BoardCoordinate rookTo, String notation) {
        this.king = king;
        this.from = from;
        this.to = to;
        this.rook = rook;
        this.rookFrom = rookFrom;
        this.rookTo = rookTo;
        this.notation = notation;
    }

    public static Castle longCastle(Player player) {
        return switch (player) {
            case WHITE -> WHITE_LONG;
            case BLACK -> BLACK_LONG;
        };
    }

    public static Castle shortCastle(Player player) {
        return switch (player) {
            case WHITE -> WHITE_SHORT;
            case BLACK -> BLACK_SHORT;
        };
    }

    @Override
    public void execute(Board board) {
        board.placePiece(king, to);
        board.removePiece(from);
        board.placePiece(rook, rookTo);
        board.removePiece(rookFrom);
        board.revokeShortCastle(getPlayer());
        board.revokeLongCastle(getPlayer());

        board.switchTurn();

        board.incrementNumMoves();
        board.incrementHalfMoves(); // it's impossible for castles to be captures or pawn pushes
    }

    /** Doesn't check if the squares are defended, only if the pieces exist */
    @Override
    public boolean isPossible(Board board) {
        if (!board.hasCastlingRights(getPlayer())) return false;

        if (!king.equals(board.pieceAt(from))) return false;
        if (!rook.equals(board.pieceAt(rookFrom))) return false;
        if (!board.isEmpty(to)) return false;
        for (int i = Math.min(from.file(), to.file()) + 1; i < Math.max(from.file(), to.file()); i++) {
            if (!board.isEmpty(new BoardCoordinate(from.rank(), i))) return false;
        }

        return switch (this) {
            case BLACK_SHORT, WHITE_SHORT -> board.canShortCastle(getPlayer());
            case BLACK_LONG, WHITE_LONG -> board.canLongCastle(getPlayer());
        };
    }

    @Override
    public Piece piece() {
        return king;
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
        return king.owner();
    }

    public Piece getRook() {
        return rook;
    }

    public BoardCoordinate getRookFrom() {
        return rookFrom;
    }

    public BoardCoordinate getRookTo() {
        return rookTo;
    }

    @Override
    public String toString() {
        return notation;
    }
}
