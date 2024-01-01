package ax.xz.max.chess.engine;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.GameState;
import ax.xz.max.chess.moves.PlayerMove;

public class SearchTest {
	public static void main(String[] args) {
		Board board = Board.fromFEN("5Kbk/6pp/6P1/8/8/8/8/7R w - - 0 1");
		System.out.println("Puzzle- mate in 2:");

		var evaluator = new ShannonEvaluator();
		var searchAlgorithm = new AlphaBetaSearch(evaluator, 3);

		// let the computer finish the game!
		while (board.getState() == GameState.UNFINISHED) {
			System.out.println(board);
			PlayerMove move;
			while (true) {
				try {
					System.out.printf("It is currently %s's turn.%n", board.currentTurn());
					System.out.println("Enter your move in algebraic notation:");
					move = searchAlgorithm.findBestMove(board);
					break;
				} catch (IllegalArgumentException e) {
					System.out.println("\nThat's not a legal move. Please try again:");
				}
			}

			String moveName = board.makeMove(move).toString();
			System.out.printf("Making move %s:%n", moveName);
		}
		System.out.printf("%n%nEnd result: %s%n", board.getState());
		System.out.println("Good game!");
	}

}
