package ax.xz.max.chess.engine;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.GameState;
import ax.xz.max.chess.engine.choice.FasterAlphaBetaSearch;
import ax.xz.max.chess.engine.choice.HumanInput;
import ax.xz.max.chess.engine.choice.MovePicker;
import ax.xz.max.chess.engine.evaluators.PieceMapEvaluator;
import ax.xz.max.chess.engine.evaluators.ShannonEvaluator;
import ax.xz.max.chess.moves.PlayerMove;

import java.time.Duration;
import java.time.Instant;

public class SearchTest {
	public static void main(String[] args) {
//		Board board = Board.fromFEN("5Kbk/6pp/6P1/8/8/8/8/7R w - - 0 1");
//		System.out.println("Puzzle- mate in 2:");

		Instant start = Instant.now();

		Board board = new Board();

		var evaluator = new ShannonEvaluator();
		var blackAlgorithm = new FasterAlphaBetaSearch(evaluator, 4);
		var whiteAlgorithm = new FasterAlphaBetaSearch(evaluator, 4);
//		var whiteAlgorithm = new HumanInput();
//		var blackAlgorithm = new HumanInput();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down...");
			System.out.println("Made " + board.getNumMoves() + " moves in " + Duration.between(start, Instant.now()).toSeconds() + " seconds\n");
			System.out.println(board.toPGN());
		}));

		finishGame(board, whiteAlgorithm, blackAlgorithm);
	}

	private static void finishGame(Board board, MovePicker white, MovePicker black) {
		while (board.gameState() == GameState.UNFINISHED) {
			System.out.println(board);
			System.out.printf("%d. %s to move%n", board.boardState().fullMoveNumber() / 2 + 1, board.currentTurn());

			Instant start = Instant.now();
			PlayerMove move = switch (board.currentTurn()) {
				case WHITE -> white.chooseNextMove(board);
				case BLACK -> black.chooseNextMove(board);
			};
			System.out.println("Time taken: " + Duration.between(start, Instant.now()).toSeconds() + " seconds");

			var moveRecord = board.makeMove(move);
			System.out.printf("Making move %s:%n", moveRecord);
		}
		System.out.println(board);
		System.out.printf("End result: %s%n", board.gameState());
		System.out.println("Good game!");
	}
}
