package ax.xz.max.chess.engine;

import ax.xz.max.chess.Board;

public interface BoardEvaluator {
	/**
	 * Finds a numerical estimate of the board's current position.
	 * A positive evaluation indicates that white has a better position,
	 * and a negative evaluation indicates that black has a better position.
	 * An evaluation of zero indicates that the position is even.
	 * @param board
	 *        the board which to evaluate the current position of
	 * @return the numerical evaluation of the current position
	 */
	double evaluate(Board board);
}
