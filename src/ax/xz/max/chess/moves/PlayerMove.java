package ax.xz.max.chess.moves;

import ax.xz.max.chess.*;

public sealed interface PlayerMove extends BoardUpdater permits RegularMove, Castle, EnPassant, Promotion {
	/**
	 * Executes the changes via public methods in Board.
	 * Is not responsible for incrementing move count
	 */
	@Override
	BoardState apply(BoardState board);

	/**
	 * @return the primary piece which makes the move
	 */
	Piece piece();

	/**
	 * @return the coordinate which the piece moves from
	 */
	BoardCoordinate from();

	/**
	 * @return the coordinate which the primary piece moves to
	 */
	BoardCoordinate to();

	/**
	 * @return the player which makes the move (the owner of the piece)
	 */
	Player getPlayer();

	/**
	 * @return A basic representation of the move.
	 * Is not guaranteed to satisfy the requirements for Algebraic Notation
	 */
	@Override
	String toString();
}
