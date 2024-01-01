package ax.xz.max.chess.engine;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.GameState;
import ax.xz.max.chess.moves.PlayerMove;

public class SearchTest {
	public static void main(String[] args) {
//		Board board = Board.fromFEN("5Kbk/6pp/6P1/8/8/8/8/7R w - - 0 1");
//		System.out.println("Puzzle- mate in 2:");

		Board board = new Board();

		var evaluator = new ShannonEvaluator();
		var searchAlgorithm = new AlphaBetaSearch(evaluator, 3);

		finishGame(board, searchAlgorithm);
	}

	private static void finishGame(Board board, AlphaBetaSearch searchAlgorithm) {
		while (board.gameState() == GameState.UNFINISHED) {
			System.out.println(board);
			System.out.printf("%s to move%n", board.currentTurn());
			PlayerMove move;

			move = searchAlgorithm.findBestMove(board);

			var moveRecord = board.makeMove(move);
			System.out.printf("Making move %s:%n", moveRecord);
		}
		System.out.println(board);
		System.out.printf("End result: %s%n", board.gameState());
		System.out.println("Good game!");
	}
}
