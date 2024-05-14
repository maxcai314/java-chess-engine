package ax.xz.max.chess.engine.choice;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.GameState;
import ax.xz.max.chess.engine.evaluators.BoardEvaluator;
import ax.xz.max.chess.moves.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicReference;

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
		try {
			return switch (board.currentTurn()) {
				case WHITE -> findMax(board);
				case BLACK -> findMin(board);
			};
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private double evaluate(Board board) {
		return evaluator.evaluate(board);
	}

	private Collection<PlayerMove> orderedLegalMoves(Board board) {
//		return board.getLegalMoves();
		return board.getLegalMoves().stream()
				.sorted(Comparator.<PlayerMove>comparingDouble(move -> movePriority(board, move)).reversed())
				.toList();
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

	public PlayerMove findMax(Board board) throws InterruptedException {
		Map<PlayerMove, Double> moveScores = new HashMap<>();

		var alpha = Double.NEGATIVE_INFINITY;
		var beta = Double.POSITIVE_INFINITY;

		try (var scope = new StructuredTaskScope.ShutdownOnFailure("Find Max", Thread.ofPlatform().factory())) { // platform threads
			var moveTasks = new HashMap<PlayerMove, StructuredTaskScope.Subtask<Double>>();

			for (PlayerMove move : orderedLegalMoves(board)) {
				var copy = board.copy();
				copy.makeMove(move);

				moveTasks.put(move, scope.fork(() -> alphaBetaMin(copy, alpha, beta, depth - 1)));
			}

			scope.join();
			scope.throwIfFailed();

			moveTasks.forEach((move, task) -> moveScores.put(move, task.get()));
		} catch (ExecutionException e) {
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

	public PlayerMove findMin(Board board) throws InterruptedException {
		Map<PlayerMove, Double> moveScores = new HashMap<>();

		var alpha = Double.NEGATIVE_INFINITY;
		var beta = Double.POSITIVE_INFINITY; // should be shared

		try (var scope = new StructuredTaskScope.ShutdownOnFailure("Find Min", Thread.ofPlatform().factory())) { // platform threads
			var moveTasks = new HashMap<PlayerMove, StructuredTaskScope.Subtask<Double>>();

			for (PlayerMove move : orderedLegalMoves(board)) {
				var copy = board.copy();
				copy.makeMove(move);

				moveTasks.put(move, scope.fork(() -> alphaBetaMax(copy, alpha, beta, depth - 1)));
			}

			scope.join();
			scope.throwIfFailed();

			moveTasks.forEach((move, task) -> moveScores.put(move, task.get()));
		} catch (ExecutionException e) {
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

	private PlayerMove concurrentFindMax(Board board) throws InterruptedException {
		Map<PlayerMove, Double> moveScores = new HashMap<>();

		var alpha = new AtomicReference<>(Double.NEGATIVE_INFINITY); // should be shared
		var beta = Double.POSITIVE_INFINITY;

		try (var scope = new StructuredTaskScope.ShutdownOnFailure("Find Max", Thread.ofPlatform().factory())) { // platform threads
			var moveTasks = new HashMap<PlayerMove, StructuredTaskScope.Subtask<Double>>();

			for (PlayerMove move : orderedLegalMoves(board)) {
				var copy = board.copy();
				copy.makeMove(move);

				moveTasks.put(move, scope.fork(() -> concurrentAlphaBetaMin(copy, alpha, beta, depth - 1)));
			}

			scope.join();
			scope.throwIfFailed();

			moveTasks.forEach((move, task) -> moveScores.put(move, task.get()));
		} catch (ExecutionException e) {
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

	private PlayerMove concurrentFindMin(Board board) throws InterruptedException {
		Map<PlayerMove, Double> moveScores = new HashMap<>();

		var alpha = Double.NEGATIVE_INFINITY;
		var beta = new AtomicReference<>(Double.POSITIVE_INFINITY); // should be shared

		try (var scope = new StructuredTaskScope.ShutdownOnFailure("Find Min", Thread.ofPlatform().factory())) { // platform threads
			var moveTasks = new HashMap<PlayerMove, StructuredTaskScope.Subtask<Double>>();

			for (PlayerMove move : orderedLegalMoves(board)) {
				var copy = board.copy();
				copy.makeMove(move);

				moveTasks.put(move, scope.fork(() -> concurrentAlphaBetaMax(copy, alpha, beta, depth - 1)));
			}

			scope.join();
			scope.throwIfFailed();

			moveTasks.forEach((move, task) -> moveScores.put(move, task.get()));
		} catch (ExecutionException e) {
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

	private double concurrentAlphaBetaMax(Board board, double alpha, AtomicReference<Double> beta, int depthRemaining) {
		if (depthRemaining == 0 || board.gameState() != GameState.UNFINISHED) return evaluate(board);
		for (PlayerMove move : orderedLegalMoves(board)) {
			var moveRecord = board.makeMove(move);
			try {
				double score = alphaBetaMin(board, alpha, beta.get(), depthRemaining - 1);
				double prevMin = beta.get();
				if (score >= prevMin) return prevMin; // hard beta cutoff
				if (score > alpha)
					alpha = score; // alpha acts like max
			} finally {
				board.unmakeMove(moveRecord);
			}
		}
		return alpha;
	}

	private double concurrentAlphaBetaMin(Board board, AtomicReference<Double> alpha, double beta, int depthRemaining) {
		if (depthRemaining == 0 || board.gameState() != GameState.UNFINISHED) return evaluate(board);
		for (PlayerMove move : orderedLegalMoves(board)) {
			var moveRecord = board.makeMove(move);
			try {
				double score = alphaBetaMax(board, alpha.get(), beta, depthRemaining - 1);
				double prevMax = alpha.get();
				if (score <= prevMax) return prevMax; // hard alpha cutoff
				if (score < beta)
					beta = score; // beta acts like min
			} finally {
				board.unmakeMove(moveRecord);
			}
		}
		return beta;
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
