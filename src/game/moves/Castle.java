package game.moves;

import game.*;

public final class Castle extends PlayerMove {
    private final Piece king;
    private final BoardCoordinate from;
    private final BoardCoordinate to;
    private final Piece rook;
    private final BoardCoordinate rookFrom;
    private final BoardCoordinate rookTo;

    private Castle(Piece king, BoardCoordinate from, BoardCoordinate to, Piece rook, BoardCoordinate rookFrom, BoardCoordinate rookTo) {
        this.king = king;
        this.from = from;
        this.to = to;
        this.rook = rook;
        this.rookFrom = rookFrom;
        this.rookTo = rookTo;
    }

    public static Castle longCastle(Player player) {
        Piece king = new Piece(player, PieceType.KING);
        Piece rook = new Piece(player, PieceType.ROOK);
        int rank = player.homeRank();
        return new Castle(king, new BoardCoordinate(rank, 4), new BoardCoordinate(rank, 2), rook, new BoardCoordinate(rank, 0), new BoardCoordinate(rank, 3));
    }

    public static Castle shortCastle(Player player) {
        Piece king = new Piece(player, PieceType.KING);
        Piece rook = new Piece(player, PieceType.ROOK);
        int rank = player.homeRank();
        return new Castle(king, new BoardCoordinate(rank, 4), new BoardCoordinate(rank, 6), rook, new BoardCoordinate(rank, 7), new BoardCoordinate(rank, 5));
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
        if (!from.isValid() || !to.isValid()) return false;
        if (!from.equals(new BoardCoordinate(getPlayer().homeRank(), 4))) return false;
        if (!(to.equals(new BoardCoordinate(getPlayer().homeRank(), 0)) || to.equals(new BoardCoordinate(getPlayer().homeRank(), 7)))) return false;
        else if (board.pieceAt(to) != null) return false;

        if (king == null) return false;
        if (rook == null) return false;
        if (king.type() != PieceType.KING) return false;
        if (rook.type() != PieceType.ROOK) return false;
        if (king.owner() != rook.owner()) return false;
        if (!king.equals(board.pieceAt(from))) return false;
        if (!rook.equals(board.pieceAt(rookFrom))) return false;
        for (int i = Math.min(from.file(), to.file()) + 1; i < Math.max(from.file(), to.file()); i++) {
            if (board.pieceAt(new BoardCoordinate(from.rank(), i)) != null) return false;
        }
        // todo: check if the squares are defended

        switch (rookFrom.file()) {
            case 0 -> {
                if (!board.canLongCastle(getPlayer())) return false;
            }
            case 7 -> {
                if (!board.canShortCastle(getPlayer())) return false;
            }
            default -> throw new IllegalStateException("Rook is not on the edge of the board: " + rookFrom);
        }

        return true;
    }

    @Override
    public Piece getPiece() {
        return king;
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
}
