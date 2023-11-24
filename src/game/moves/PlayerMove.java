package game.moves;

import game.*;

public sealed interface PlayerMove extends BoardCommand permits RegularMove, Castle, EnPassant, Promotion {
    @Override
    void execute(Board board);

    boolean isPossible(Board board);

    Piece piece();

    BoardCoordinate from();

    BoardCoordinate to();

    Player getPlayer();

    @Override
    String toString();
}
