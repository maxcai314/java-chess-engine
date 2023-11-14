package game.moves;

import game.*;

public final class Castle extends PlayerMove {
    private final Piece rook;
    private final BoardCoordinate rookFrom;
    private final BoardCoordinate rookTo;

    private Castle(Piece king, BoardCoordinate from, BoardCoordinate to, Piece rook, BoardCoordinate rookFrom, BoardCoordinate rookTo) {
        super(king, from, to);
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
    public void makeMove(Piece[][] board) {
        super.makeMove(board);
        board[rookTo.rank()][rookTo.file()] = board[rookFrom.rank()][rookFrom.file()];
        board[rookFrom.rank()][rookFrom.file()] = null;
    }

    /** Doesn't check if the squares are defended, only if the pieces exist */
    @Override
    public boolean isPossible(Piece[][] board) {
        // check that the two pieces are the same owner
        Player owner = getPlayer();
        if (owner != getRook().owner()) return false;
        // check if the tiles between the rook and king (exclusive) are all null
        int lower = Math.min(getRookFrom().file(), getFrom().file()) + 1;
        int higher = Math.max(getRookFrom().file(), getFrom().file());
        for (int i=lower;i<higher;i++) {
            if (board[rookFrom.rank()][i] != null) return false;
        }
        return true;
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
