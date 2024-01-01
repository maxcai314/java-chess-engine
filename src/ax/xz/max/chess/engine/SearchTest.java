package ax.xz.max.chess.engine;

import ax.xz.max.chess.Board;

public class SearchTest {
	public static void main(String[] args) {
		Board board = Board.fromFEN("k3r2r/8/8/8/8/8/8/K3Q3 w");
		System.out.println(board.toFEN());
		System.out.println(board);

		var evaluator = new ShannonEvaluator();
		var searchAlgorithm = new AlphaBetaSearch(evaluator, 2);

		System.out.println("Best move: " + searchAlgorithm.findBestMove(board));
	}
}
