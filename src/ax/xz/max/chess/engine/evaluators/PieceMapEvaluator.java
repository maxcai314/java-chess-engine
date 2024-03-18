package ax.xz.max.chess.engine.evaluators;

import ax.xz.max.chess.*;
import ax.xz.max.chess.moves.Castle;
import ax.xz.max.chess.moves.PlayerMove;
import ax.xz.max.chess.moves.Promotion;

import java.util.List;

public class PieceMapEvaluator implements BoardEvaluator {
	@Override
	public double evaluate(Board board) {
		return switch (board.gameState()) {
			case WHITE_WON -> 10000 - board.getNumMoves(); // prioritize tempo: find fastest mate
			case BLACK_WON -> -10000 + board.getNumMoves();
			case DRAW -> 0;
			default -> materialReward(board)
					+ mobilityReward(board)
					- doubledPawnPenalty(board)
					- blockedPawnPenalty(board)
					- isolatedPawnPenalty(board);
		};

	}

	/** should be added to the evaluation */
	private static double materialReward(Board board) {
		double total = 0;
		for (var player : Player.values()) {
			for (var type : PieceType.values()) {
				var piece = new Piece(player, type);
				for (var position : board.boardState().board().allOf(piece)) {
					total += valueOf(position, piece);
				}
			}
		}
		return total;
	}

	private static final List<List<Double>> whitePawnValue = List.of(
			List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
			List.of(1.0, 1.0, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0),
			List.of(1.2, 1.2, 1.3, 1.3, 1.3, 1.2, 1.2, 1.2),
			List.of(1.3, 1.4, 1.4, 1.4, 1.4, 1.4, 1.4, 1.3),
			List.of(1.4, 1.5, 1.6, 1.7, 1.7, 1.5, 1.5, 1.4),
			List.of(1.5, 1.6, 1.7, 1.8, 1.8, 1.6, 1.6, 1.5),
			List.of(1.9, 2.0, 2.1, 2.2, 2.2, 2.1, 2.0, 1.9),
			List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
	);

	private static final List<List<Double>> blackPawnValue = whitePawnValue.reversed();

	private static final List<List<Double>> whiteKnightValue = List.of(
			List.of(1.5, 2.1, 2.5, 2.5, 2.5, 2.5, 2.1, 1.5),
			List.of(1.8, 2.5, 2.5, 3.0, 3.0, 2.5, 2.5, 1.8),
			List.of(2.5, 3.0, 3.2, 3.0, 3.0, 3.2, 3.0, 2.5),
			List.of(2.5, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.5),
			List.of(2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.8),
			List.of(2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.8),
			List.of(2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.8),
			List.of(2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8)
	);

	private static final List<List<Double>> blackKnightValue = whiteKnightValue.reversed();

	private static final List<List<Double>> whiteBishopValue = List.of(
			List.of(2.0, 1.0, 2.1, 2.0, 2.0, 2.1, 1.0, 2.0),
			List.of(2.5, 3.0, 2.5, 2.9, 2.9, 2.5, 3.0, 2.5),
			List.of(2.5, 2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 2.5),
			List.of(2.8, 2.5, 3.6, 3.0, 3.0, 3.6, 2.5, 2.8),
			List.of(3.0, 3.5, 3.0, 3.0, 3.0, 3.0, 3.5, 3.0),
			List.of(3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0),
			List.of(3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0),
			List.of(2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8)
	);

	private static final List<List<Double>> blackBishopValue = whiteBishopValue.reversed();

	private static final List<List<Double>> whiteRookValue = List.of(
			List.of(4.1, 4.5, 4.5, 5.0, 5.0, 5.0, 4.5, 4.1),
			List.of(4.5, 4.5, 4.5, 4.5, 4.5, 4.5, 4.5, 4.5),
			List.of(5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0),
			List.of(5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0),
			List.of(5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0),
			List.of(5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0),
			List.of(5.1, 5.1, 5.1, 5.1, 5.1, 5.1, 5.1, 5.1),
			List.of(5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0)
	);

	private static final List<List<Double>> blackRookValue = whiteRookValue.reversed();

	private static final List<List<Double>> whiteQueenValue = List.of(
			List.of(9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0),
			List.of(9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0),
			List.of(9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0),
			List.of(9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0),
			List.of(9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0),
			List.of(9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0),
			List.of(9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0),
			List.of(9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0)
	);

	private static final List<List<Double>> blackQueenValue = whiteQueenValue.reversed();

	private static double valueOf(BoardCoordinate coordinate, Piece piece) {
		return switch (piece.owner()) {
			case WHITE -> switch (piece.type()) {
				case PAWN -> whitePawnValue.get(coordinate.rank()).get(coordinate.file());
				case KNIGHT -> whiteKnightValue.get(coordinate.rank()).get(coordinate.file());
				case BISHOP -> whiteBishopValue.get(coordinate.rank()).get(coordinate.file());
				case ROOK -> whiteRookValue.get(coordinate.rank()).get(coordinate.file());
				case QUEEN -> whiteQueenValue.get(coordinate.rank()).get(coordinate.file());
				case KING -> 0; // todo: make map
			};

			case BLACK -> -1 * switch (piece.type()) {
				case PAWN -> blackPawnValue.get(coordinate.rank()).get(coordinate.file());
				case KNIGHT -> blackKnightValue.get(coordinate.rank()).get(coordinate.file());
				case BISHOP -> blackBishopValue.get(coordinate.rank()).get(coordinate.file());
				case ROOK -> blackRookValue.get(coordinate.rank()).get(coordinate.file());
				case QUEEN -> blackQueenValue.get(coordinate.rank()).get(coordinate.file());
				case KING -> 0;
			};
		};
	}

	private static int[] numPawnsPerFile(Board board, Player player) {
		Piece pawn = new Piece(player, PieceType.PAWN);
		var result = new int[8];
		for (BoardCoordinate position : board.boardState().board().allOf(pawn)) {
			result[position.file()]++;
		}
		return result;
	}

	/** should be subtracted from the evaluation */
	private static double doubledPawnPenalty(Board board) {
		return doubledPawnPenalty(board, Player.WHITE) - doubledPawnPenalty(board, Player.BLACK);
	}

	private static double doubledPawnPenalty(Board board, Player player) {
		var numPawnsPerFile = numPawnsPerFile(board, player);
		int count = 0;
		for (int numPawns : numPawnsPerFile) {
			if (numPawns > 1) count += numPawns - 1;
		}
		return 0.2 * count;
	}

	/** should be subtracted from the evaluation */
	private static double blockedPawnPenalty(Board board) {
		return blockedPawnPenalty(board, Player.WHITE) - blockedPawnPenalty(board, Player.BLACK);
	}

	private static double blockedPawnPenalty(Board board, Player player) {
		Piece pawn = new Piece(player, PieceType.PAWN);
		int count = 0;
		for (BoardCoordinate position : board.boardState().board().allOf(pawn)) {
			var step = position.step(player.pawnDirection(), 0);
			if (step.isValid() && !board.boardState().isEmpty(step)) count++;
		}
		return 0.1 * count;
	}

	/** should be subtracted from the evaluation */
	private static double isolatedPawnPenalty(Board board) {
		return isolatedPawnPenalty(board, Player.WHITE) - isolatedPawnPenalty(board, Player.BLACK);
	}

	private static double isolatedPawnPenalty(Board board, Player player) {
		var numPawnsPerFile = numPawnsPerFile(board, player);
		int count = 0;
		for (int file = 0; file < 8; file++) {
			if (numPawnsPerFile[file] == 0) continue;
			if (file > 0 && numPawnsPerFile[file - 1] == 0) continue;
			if (file < 7 && numPawnsPerFile[file + 1] == 0) continue;
			count += numPawnsPerFile[file];
			file++; // the next file cannot be isolated if this one is
		}

		return 0.5 * count;
	}

	private static double mobilityReward(Board board) {
		return board.getLegalMoves(Player.WHITE).stream().map(PieceMapEvaluator::moveReward).reduce(0.0, Double::sum)
				- board.getLegalMoves(Player.BLACK).stream().map(PieceMapEvaluator::moveReward).reduce(0.0, Double::sum);
	}

	private static double moveReward(PlayerMove move) {
		double total = 0.01;
		total += switch(move) {
			case Castle __ -> 0.02;
			case Promotion __ -> 0.01;
			default -> 0;
		};
		total += switch (move.piece().type()) {
			case KNIGHT, BISHOP -> 0.03;
			case ROOK -> 0.02;
			case QUEEN -> 0.001;
			default -> 0;
		};
		return total;
	}
}
