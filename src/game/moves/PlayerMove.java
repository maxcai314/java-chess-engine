package game.moves;

import game.*;

public sealed interface PlayerMove extends BoardCommand permits RegularMove, Castle, EnPassant, Promotion {
	/**
	 * Executes the changes via public methods in Board.
	 * Is not responsible for incrementing move count
	 */
	@Override
	void execute(Board board);

	/**
	 * Low-level basic check to see if the required squares are empty/occupied.
	 * Does not check for legality in terms of tempo or checks.
	 */
	boolean isPossible(Board board);

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
