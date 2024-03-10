package ax.xz.max.chess.engine.choice;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.GameState;
import ax.xz.max.chess.engine.evaluators.BoardEvaluator;
import ax.xz.max.chess.moves.*;

import java.util.*;
import java.util.concurrent.StructuredTaskScope;

public record FasterAlphaBetaSearch(
		BoardEvaluator evaluator,
		int depth
) implements MovePicker {
	public FasterAlphaBetaSearch {
		if (depth <= 0)
			throw new IllegalArgumentException("Search depth must be positive");
	}
	@Override
	public PlayerMove chooseNextMove(Board board) {
		return switch (board.currentTurn()) {
			case WHITE -> findMax(board);
			case BLACK -> findMin(board);
		};
	}

	private double evaluate(Board board) {
		return evaluator.evaluate(board);
	}

	private Collection<PlayerMove> orderedLegalMoves(Board board) {
		return board.getLegalMoves();
//		return board.getLegalMoves().stream()
//				.sorted(Comparator.<PlayerMove>comparingDouble(move -> movePriority(board, move)).reversed())
//				.toList();
	}

	private double movePriority(Board board, PlayerMove move) {
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

	public PlayerMove findMax(Board board) {
//		int fakeDepth = depth;
//		if (board.boardState().numPieces() < 10) {
//			fakeDepth += 2;
//		}
//		int depth = fakeDepth;
		Map<PlayerMove, Double> moveScores = new HashMap<>();
		try (var scope = new StructuredTaskScope<>("Find Max", Thread::new)) { // platform threads
			for (PlayerMove move : orderedLegalMoves(board)) {
				var copy = board.copy();
				copy.makeMove(move);
				scope.fork(() -> {
					double score = alphaBetaMin(copy, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, depth - 1);
					moveScores.put(move, score);
					return null;
				});
			}
			scope.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		PlayerMove bestMove = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		for (var entry : moveScores.entrySet()) {
			if (entry.getValue() > bestScore) {
				bestScore = entry.getValue();
				bestMove = entry.getKey();
			}
		}
		return bestMove;
	}

	public PlayerMove findMin(Board board) {
//		int fakeDepth = depth;
//		if (board.boardState().numPieces() < 10) {
//			fakeDepth += 2;
//		}
//		int depth = fakeDepth;
		Map<PlayerMove, Double> moveScores = new HashMap<>();
		try (var scope = new StructuredTaskScope<>("Find Min", Thread::new)) { // platform threads
			for (PlayerMove move : orderedLegalMoves(board)) {
				var copy = board.copy();
				copy.makeMove(move);
				scope.fork(() -> {
					double score = alphaBetaMax(copy, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, depth - 1);
					moveScores.put(move, score);
					return null;
				});
			}
			scope.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		PlayerMove bestMove = null;
		double bestScore = Double.POSITIVE_INFINITY;
		for (var entry : moveScores.entrySet()) {
			if (entry.getValue() < bestScore) {
				bestScore = entry.getValue();
				bestMove = entry.getKey();
			}
		}
		return bestMove;
	}

	private double alphaBetaMax(Board board, double alpha, double beta, int depthRemaining) {
		if (depthRemaining == 0 || board.gameState() != GameState.UNFINISHED) return evaluate(board);
		for (PlayerMove move : orderedLegalMoves(board)) {
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
		for (PlayerMove move : orderedLegalMoves(board)) {
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
