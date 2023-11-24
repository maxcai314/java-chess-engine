package game.moves;

import game.*;

public sealed interface PlayerMove extends BoardCommand permits RegularMove, Castle, EnPassant, Promotion {
    @Override
    public abstract void execute(Board board);

    public abstract boolean isPossible(Board board);

    public abstract Piece piece();

    public abstract BoardCoordinate from();

    public abstract BoardCoordinate to();

    public abstract Player getPlayer();

    @Override
    public abstract String toString();
}
