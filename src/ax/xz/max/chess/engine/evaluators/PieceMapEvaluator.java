package ax.xz.max.chess.engine.evaluators;

import ax.xz.max.chess.*;
import ax.xz.max.chess.moves.Castle;
import ax.xz.max.chess.moves.PlayerMove;
import ax.xz.max.chess.moves.Promotion;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class PieceMapEvaluator implements BoardEvaluator {
	@Override
	public double evaluate(Board board) {
		switch (board.gameState()) {
			case WHITE_WON -> {
				return 1000 - board.getNumMoves(); // prioritize tempo: find fastest mate
			}
			case BLACK_WON -> {
				return -1000 + board.getNumMoves();
			}
			case DRAW -> {
				return 0;
			}
		}

		return materialReward(board)
				+ mobilityReward(board)
				- doubledPawnPenalty(board)
				- blockedPawnPenalty(board)
				- isolatedPawnPenalty(board);
	}

	/** should be added to the evaluation */
	private static double materialReward(Board board) {
		return IntStream.range(0, 8)
				.mapToObj(BoardCoordinate::allFromRank)
				.flatMap(Set::stream)
				.filter(coordinate -> board.pieceAt(coordinate) != null)
				.mapToDouble(coordinate -> valueOf(coordinate, board.pieceAt(coordinate)))
				.sum();
	}

	private static final List<Double> whitePawnValue = List.of(
			0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.1, 1.1, 1.2, 1.3, 1.3, 1.1, 1.1, 1.1,
			1.2, 1.3, 1.4, 1.4, 1.4, 1.3, 1.3, 1.2,
			1.3, 1.4, 1.4, 1.4, 1.4, 1.4, 1.4, 1.3,
			1.4, 1.5, 1.4, 1.5, 1.5, 1.4, 1.4, 1.4,
			1.8, 1.9, 2.0, 2.0, 2.0, 2.0, 2.0, 1.8,
			0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
	);

	private static final List<Double> blackPawnValue = List.of(
			0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
			1.8, 1.9, 2.0, 2.0, 2.0, 2.0, 2.0, 1.8,
			1.4, 1.5, 1.4, 1.5, 1.5, 1.4, 1.4, 1.4,
			1.3, 1.4, 1.4, 1.4, 1.4, 1.4, 1.4, 1.3,
			1.2, 1.3, 1.4, 1.4, 1.4, 1.3, 1.3, 1.2,
			1.1, 1.1, 1.2, 1.3, 1.3, 1.1, 1.1, 1.1,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
	);

	private static final List<Double> whiteKnightValue = List.of(
			1.5, 2.8, 2.2, 2.2, 2.2, 2.2, 2.8, 1.5,
			1.8, 2.5, 2.5, 3.0, 3.0, 2.5, 2.5, 1.8,
			2.5, 2.8, 2.8, 3.0, 3.0, 3.0, 2.8, 3.0,
			2.5, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.5,
			2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.8,
			2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.8,
			2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.8,
			2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8
	);

	private static final List<Double> blackKnightValue = List.of(
			2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8,
			2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.8,
			2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.8,
			2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.8,
			2.5, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.5,
			2.5, 2.8, 2.8, 3.0, 3.0, 3.0, 2.8, 3.0,
			1.8, 2.5, 2.5, 3.0, 3.0, 2.5, 2.5, 1.8,
			1.5, 2.8, 2.2, 2.2, 2.2, 2.2, 2.8, 1.5
	);

	private static final List<Double> whiteBishopValue = List.of(
			2.0, 2.0, 2.8, 2.0, 2.0, 2.8, 2.0, 2.0,
			2.5, 3.1, 2.5, 2.9, 2.9, 2.5, 3.1, 2.5,
			2.5, 2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 2.5,
			2.8, 2.5, 3.5, 3.0, 3.0, 3.5, 2.5, 2.8,
			3.0, 3.5, 3.0, 3.0, 3.0, 3.0, 3.5, 3.0,
			3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0,
			3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0,
			2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8
	);

	private static final List<Double> blackBishopValue = List.of(
			2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8, 2.8,
			3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0,
			3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0,
			3.0, 3.5, 3.0, 3.0, 3.0, 3.0, 3.5, 3.0,
			2.8, 2.5, 3.5, 3.0, 3.0, 3.5, 2.5, 2.8,
			2.5, 2.8, 3.0, 3.0, 3.0, 3.0, 3.0, 2.5,
			2.5, 3.1, 2.5, 2.9, 2.9, 2.5, 3.1, 2.5,
			2.0, 2.0, 2.8, 2.0, 2.0, 2.8, 2.0, 2.0
	);

	private static final List<Double> whiteRookValue = List.of(
			4.5, 4.5, 4.5, 5.0, 5.0, 5.0, 4.5, 4.5,
			4.5, 4.5, 4.5, 4.5, 4.5, 4.5, 4.5, 4.5,
			5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
			5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
			5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
			5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
			5.1, 5.1, 5.1, 5.1, 5.1, 5.1, 5.1, 5.1,
			5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0
	);

	private static final List<Double> blackRookValue = List.of(
			5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
			5.1, 5.1, 5.1, 5.1, 5.1, 5.1, 5.1, 5.1,
			5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
			5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
			5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
			5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
			4.5, 4.5, 4.5, 4.5, 4.5, 4.5, 4.5, 4.5,
			4.5, 4.5, 4.5, 5.0, 5.0, 5.0, 4.5, 4.5
	);

	private static final List<Double> whiteQueenValue = List.of(
			9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0,
			9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0,
			9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0,
			9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0,
			9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0,
			9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0,
			9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0,
			9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0
	);

	private static final List<Double> blackQueenValue = whiteQueenValue;

	private static double valueOf(BoardCoordinate coordinate, Piece piece) {
		int index = coordinate.rank() * 8 + coordinate.file();

		return switch (piece.owner()) {
			case WHITE -> switch (piece.type()) {
				case PAWN -> whitePawnValue.get(index);
				case KNIGHT -> whiteKnightValue.get(index);
				case BISHOP -> whiteBishopValue.get(index);
				case ROOK -> whiteRookValue.get(index);
				case QUEEN -> whiteQueenValue.get(index);
				case KING -> 0; // todo: make map
			};

			case BLACK -> -1 * switch (piece.type()) {
				case PAWN -> blackPawnValue.get(index);
				case KNIGHT -> blackKnightValue.get(index);
				case BISHOP -> blackBishopValue.get(index);
				case ROOK -> blackRookValue.get(index);
				case QUEEN -> blackQueenValue.get(index);
				case KING -> 0;
			};
		};
	}

	private static int numPawnsOnFile(Board board, Player player, int file) {
		Piece pawn = new Piece(player, PieceType.PAWN);
		var tiles = BoardCoordinate.allFromFile(file);
		tiles.removeIf(Predicate.not(BoardCoordinate::isValid));
		tiles.removeIf(coordinate -> board.pieceAt(coordinate) != pawn);
		return tiles.size();
	}

	/** should be subtracted from the evaluation */
	private static double doubledPawnPenalty(Board board) {
		return doubledPawnPenalty(board, Player.WHITE) - doubledPawnPenalty(board, Player.BLACK);
	}

	private static double doubledPawnPenalty(Board board, Player player) {
		return 0.05 * IntStream.range(0, 8)
				.map(file -> numPawnsOnFile(board, player, file))
				.map(numPawns -> numPawns - 1)
				.map(doubledPawns -> Math.max(0, doubledPawns))
				.sum();
	}

	/** should be subtracted from the evaluation */
	private static double blockedPawnPenalty(Board board) {
		return blockedPawnPenalty(board, Player.WHITE) - blockedPawnPenalty(board, Player.BLACK);
	}

	private static double blockedPawnPenalty(Board board, Player player) {
		Piece pawn = new Piece(player, PieceType.PAWN);
		return 0.05 * IntStream.range(0, 8)
				.map(file -> {
					var tiles = BoardCoordinate.allFromFile(file);
					tiles.removeIf(coordinate -> board.pieceAt(coordinate) != pawn);
					tiles.removeIf(coordinate -> {
						var forward = coordinate.step(player.pawnDirection(), 0);
						return forward.isValid() && board.pieceAt(forward) != null;
					});
					return tiles.size();
				})
				.sum();
	}

	/** should be subtracted from the evaluation */
	private static double isolatedPawnPenalty(Board board) {
		return isolatedPawnPenalty(board, Player.WHITE) - isolatedPawnPenalty(board, Player.BLACK);
	}

	private static double isolatedPawnPenalty(Board board, Player player) {
		return 0.05 * IntStream.range(0, 8)
				.map(file -> {
					if (numPawnsOnFile(board, player, file - 1) == 0) return 0;
					if (numPawnsOnFile(board, player, file + 1) == 0) return 0;
					return numPawnsOnFile(board, player, file);
				})
				.sum();
	}

	private static double mobilityReward(Board board) {
		return board.getLegalMoves(Player.WHITE).stream().map(PieceMapEvaluator::moveReward).reduce(0.0, Double::sum)
				- board.getLegalMoves(Player.BLACK).stream().map(PieceMapEvaluator::moveReward).reduce(0.0, Double::sum);
	}

	private static double moveReward(PlayerMove move) {
		double total = 0.05;
		if (move instanceof Castle || move instanceof Promotion)
			total += 0.05;
		total += switch (move.piece().type()) {
			case KNIGHT, BISHOP -> 0.1;
			case ROOK -> 0.15;
			case QUEEN -> 0.01;
			default -> 0;
		};
		return total;
	}
}
