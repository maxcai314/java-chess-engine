package game.moves;

import game.*;

public sealed class PlayerMove permits RegularMove, Castle, EnPassant, Promotion {
    protected final Piece piece;
    protected final BoardCoordinate from;
    protected final BoardCoordinate to;
    public PlayerMove(Piece piece, BoardCoordinate from, BoardCoordinate to) {
        this.piece = piece;
        this.from = from;
        this.to = to;
    }

    public void makeMove(Piece[][] board) {
        board[to.rank()][to.file()] = board[from.rank()][from.file()];
        board[from.rank()][from.file()] = null;
    }

    public Piece getPiece() {
        return piece;
    }

    public BoardCoordinate getFrom() {
        return from;
    }

    public BoardCoordinate getTo() {
        return to;
    }

    public Player getPlayer() {
        return piece.owner();
    }
}
