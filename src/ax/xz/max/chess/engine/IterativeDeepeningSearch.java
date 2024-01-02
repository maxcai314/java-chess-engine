package ax.xz.max.chess.engine;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.GameState;
import ax.xz.max.chess.moves.*;

import java.util.*;

public record IterativeDeepeningSearch(
		BoardEvaluator evaluator,
		int depth
) implements MovePicker {
	public IterativeDeepeningSearch {
		if (depth <= 0)
			throw new IllegalArgumentException("Search depth must be positive");
	}
	@Override
	public PlayerMove chooseNextMove(Board board) {
		SingleThreadedSearch search = new SingleThreadedSearch(board, depth);
		return switch (board.currentTurn()) {
			case WHITE -> search.findMax();
			case BLACK -> search.findMin();
		};
	}
	private class MoveTreeNode {
		private final Board board;
		private final MoveTreeNode parent;
		private final Map<PlayerMove, MoveTreeNode> children = new HashMap<>();
		private final int depthRemaining;
		private double score = Double.NaN;

		private MoveTreeNode(Board board, MoveTreeNode parent, int depthRemaining) {
			this.board = board;
			this.parent = parent;
			this.depthRemaining = depthRemaining;
		}

		public MoveTreeNode childFor(PlayerMove move) {
			return children.computeIfAbsent(move, this::childFor0);
		}

		private MoveTreeNode childFor0(PlayerMove move) {
			var copy = board.copy();
			copy.makeMove(move);
			return new MoveTreeNode(copy, this, depthRemaining - 1);
		}

		public double evaluate() {
			if (Double.isNaN(score))
				score = evaluator.evaluate(board);
			return score;
		}
	}

	private class SingleThreadedSearch {
		private final Board board;
//		private final MoveTreeNode root;
		private final int depth;

		public SingleThreadedSearch(Board board, int depth) {
			this.board = board.copy();
//			this.root = new MoveTreeNode(this.board, null, depth);
			this.depth = depth;
		}

		public double evaluate(Board board) {
			return evaluator.evaluate(board);
		}

		private List<PlayerMove> orderedLegalMoves(Board board, int depth) {
			var moves = new ArrayList<>(board.getLegalMoves().stream()
					.sorted(Comparator.<PlayerMove>comparingDouble(move -> {
						var copy = board.copy();
						copy.makeMove(move); // todo: wasteful, you have to spend tome computing a whole other board
						// todo: should use some stored/precomputed value by computeIfAbsent from a tree map or smth
						return evaluationOf(copy, depth - 1);
					}).reversed()).toList());

			return switch (board.currentTurn()) {
				case WHITE -> moves;
				case BLACK -> moves.reversed();
			};
		}

		public PlayerMove findMax() {
			PlayerMove bestMove = null;
			double highestEval = Double.NEGATIVE_INFINITY;
			var copy = board.copy();
			for (PlayerMove move : orderedLegalMoves(board, depth)) {
				var moveRecord = copy.makeMove(move);
				try {
					double score = evaluationOf(copy, depth - 1);
					if (score > highestEval) {
						highestEval = score;
						bestMove = move;
					}
				} finally {
					copy.unmakeMove(moveRecord);
				}
			}
			return bestMove;
		}

		public PlayerMove findMin() {
			PlayerMove bestMove = null;
			double lowestEval = Double.POSITIVE_INFINITY;
			var copy = board.copy();
			for (PlayerMove move : orderedLegalMoves(board, depth)) {
				var moveRecord = copy.makeMove(move);
				try {
					double score = evaluationOf(copy, depth);
					if (score < lowestEval) {
						lowestEval = score;
						bestMove = move;
					}
				} finally {
					copy.unmakeMove(moveRecord);
				}
			}
			return bestMove;
		}

		// store values in some type of object using a tree or smth so we don't need to constantly recompute
		public double evaluationOf(Board board, int depth) {
			double alpha = Double.NEGATIVE_INFINITY;
			double beta = Double.POSITIVE_INFINITY;
			return switch (board.currentTurn()) {
				case WHITE -> alphaBetaMax(board, alpha, beta, depth);
				case BLACK -> alphaBetaMin(board, alpha, beta, depth);
			};
		}

		private double alphaBetaMax(Board board, double alpha, double beta, int depthRemaining) {
			if (depthRemaining == 0 || board.gameState() != GameState.UNFINISHED) return evaluate(board);
			for (PlayerMove move : orderedLegalMoves(board, depthRemaining)) {
				var moveRecord = board.makeMove(move);
				try {
					double score = alphaBetaMin(board, alpha, beta, depthRemaining - 1);
					if (score >= beta)
						return beta; // hard beta cutoff
					if (score > alpha)
						alpha = score; // alpha acts like max
				} finally {
					board.unmakeMove(moveRecord);
				}
			}
			return alpha;
		}

		private double alphaBetaMin(Board board, double alpha, double beta, int depthRemaining) {
			if (depthRemaining == 0 || board.gameState() != GameState.UNFINISHED) return evaluate(board);
			for (PlayerMove move : orderedLegalMoves(board, depthRemaining)) {
				var moveRecord = board.makeMove(move);
				try {
					double score = alphaBetaMax(board, alpha, beta, depthRemaining - 1);
					if (score <= alpha)
						return alpha; // hard alpha cutoff
					if (score < beta)
						beta = score; // beta acts like min
				} finally {
					board.unmakeMove(moveRecord);
				}
			}
			return beta;
		}
	}
}
