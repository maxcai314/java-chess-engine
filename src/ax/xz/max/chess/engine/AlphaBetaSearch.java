package ax.xz.max.chess.engine;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.GameState;
import ax.xz.max.chess.moves.*;

import java.util.Comparator;
import java.util.List;

// todo: interface MoveAlgorithm for next-move generator?
public record AlphaBetaSearch(
		BoardEvaluator evaluator,
		int depth
) {
	public AlphaBetaSearch {
		if (depth <= 0)
			throw new IllegalArgumentException("Search depth must be positive");
	}

	// @Override
	public PlayerMove findBestMove(Board board) {
		SingleThreadedSearch search = new SingleThreadedSearch(board);
		return switch (board.currentTurn()) {
			case WHITE -> search.findMax();
			case BLACK -> search.findMin();
		};
	}

	private class SingleThreadedSearch {
		private final Board board;

		public SingleThreadedSearch(Board board) {
			this.board = board.copy();
		}

		public double evaluate() {
			return evaluator.evaluate(board);
		}

		private List<PlayerMove> orderedLegalMoves() {
			return board.getLegalMoves().stream()
					.sorted(Comparator.comparingDouble(this::movePriority).reversed())
					.toList();
		}

		private double movePriority(PlayerMove move) {
			double result = 0;
			double piecePriority = switch (move.piece().type()) {
				case KNIGHT, BISHOP -> 10;
				case ROOK, QUEEN -> 15;
				case KING -> 5;
				case PAWN -> 1;
			};
			result += piecePriority;
			var record = new MoveRecord(board.boardState(), move);

			if (record.isCheck()) {
				result += 100; // ALWAYS LOOK FOR CHECKS
			}

			if (
					record.isCapture()
							|| move instanceof EnPassant
							|| move instanceof Castle
							|| move instanceof Promotion
			) {
				result += 50;
			}

			return result;
		}

		public PlayerMove findMax() {
			PlayerMove bestMove = null;
			double alpha = Double.NEGATIVE_INFINITY;
			double beta = Double.POSITIVE_INFINITY;
			for (PlayerMove move : board.getLegalMoves()) {
				var moveRecord = board.makeMove(move);
				try {
					double score = alphaBetaMin(alpha, beta, depth - 1);
					if (score > alpha) {
						alpha = score; // alpha acts like max
						bestMove = move;
					}
				} finally {
					board.unmakeMove(moveRecord);
				}
			}
			return bestMove;
		}

		public PlayerMove findMin() {
			PlayerMove bestMove = null;
			double alpha = Double.NEGATIVE_INFINITY;
			double beta = Double.POSITIVE_INFINITY;
			for (PlayerMove move : board.getLegalMoves()) {
				var moveRecord = board.makeMove(move);
				try {
					double score = alphaBetaMax(alpha, beta, depth - 1);
					if (score < beta) {
						beta = score; // beta acts like min
						bestMove = move;
					}
				} finally {
					board.unmakeMove(moveRecord);
				}
			}
			return bestMove;
		}

		private double alphaBetaMax(double alpha, double beta, int depthRemaining) {
			if (depthRemaining == 0 || board.gameState() != GameState.UNFINISHED) return evaluate();
			for (PlayerMove move : board.getLegalMoves()) {
				var moveRecord = board.makeMove(move);
				try {
					double score = alphaBetaMin(alpha, beta, depthRemaining - 1);
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

		private double alphaBetaMin(double alpha, double beta, int depthRemaining) {
			if (depthRemaining == 0 || board.gameState() != GameState.UNFINISHED) return evaluate();
			for (PlayerMove move : board.getLegalMoves()) {
				var moveRecord = board.makeMove(move);
				try {
					double score = alphaBetaMax(alpha, beta, depthRemaining - 1);
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
