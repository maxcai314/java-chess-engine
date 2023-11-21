package game.moves;

import game.*;

public sealed abstract class PlayerMove implements BoardCommand permits RegularMove, Castle, EnPassant, Promotion {

    @Override
    public abstract void execute(Board board);

    public abstract boolean isPossible(Board board);

    public abstract Piece getPiece();

    public abstract BoardCoordinate getFrom();

    public abstract BoardCoordinate getTo();

    public abstract Player getPlayer();

    @Override
    public abstract String toString();
}
