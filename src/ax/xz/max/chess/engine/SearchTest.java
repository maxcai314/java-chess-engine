package ax.xz.max.chess.engine;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.GameState;
import ax.xz.max.chess.moves.PlayerMove;

public class SearchTest {
	public static void main(String[] args) {
//		Board board = Board.fromFEN("5Kbk/6pp/6P1/8/8/8/8/7R w - - 0 1");
//		System.out.println("Puzzle- mate in 2:");

		Board board = new Board();

		var evaluator = new ImprovedShannonEvaluator();
		var whiteAlgorithm = new IterativeDeepeningSearch(evaluator, 2);
		var blackAlgorithm = new IterativeDeepeningSearch(evaluator, 1);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down...\n");
			System.out.println(board.toPGN());
		}));

		finishGame(board, whiteAlgorithm, blackAlgorithm);
	}

	private static void finishGame(Board board, MovePicker white, MovePicker black) {
		while (board.gameState() == GameState.UNFINISHED) {
			System.out.println(board);
			System.out.printf("%d. %s to move%n", board.getNumMoves() / 2 + 1, board.currentTurn());

			PlayerMove move = switch (board.currentTurn()) {
				case WHITE -> white.chooseNextMove(board);
				case BLACK -> black.chooseNextMove(board);
			};

			var moveRecord = board.makeMove(move);
			System.out.printf("Making move %s:%n", moveRecord);
		}
		System.out.println(board);
		System.out.printf("End result: %s%n", board.gameState());
		System.out.println("Good game!");
	}
}
