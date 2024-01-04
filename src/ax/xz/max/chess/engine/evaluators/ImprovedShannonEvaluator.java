package ax.xz.max.chess.engine.evaluators;

import ax.xz.max.chess.*;
import ax.xz.max.chess.moves.Castle;
import ax.xz.max.chess.moves.PlayerMove;
import ax.xz.max.chess.moves.Promotion;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class ImprovedShannonEvaluator implements BoardEvaluator {
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
				+ tempoReward(board)
				- doubledPawnPenalty(board)
				- blockedPawnPenalty(board)
				- isolatedPawnPenalty(board);
	}

	/** should be added to the evaluation */
	private static double materialReward(Board board) {
		return IntStream.range(0, 8)
				.mapToObj(BoardCoordinate::allFromRank)
				.flatMap(Set::stream)
				.map(board::pieceAt)
				.filter(Objects::nonNull)
				.mapToDouble(ImprovedShannonEvaluator::valueOf)
				.sum();
	}

	private static double valueOf(PieceType piece) {
		return switch (piece) {
			case PAWN -> 1;
			case KNIGHT -> 3;
			case BISHOP -> 3.1;
			case ROOK -> 5;
			case QUEEN -> 9;
			case KING -> 200;
		};
	}

	private static double valueOf(Piece piece) {
		return valueOf(piece.type()) * switch (piece.owner()) {
			case WHITE -> 1;
			case BLACK -> -1;
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
		return 0.5 * IntStream.range(0, 8)
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
		return 0.5 * IntStream.range(0, 8)
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
		return 0.5 * IntStream.range(0, 8)
				.map(file -> {
					if (numPawnsOnFile(board, player, file - 1) == 0) return 0;
					if (numPawnsOnFile(board, player, file + 1) == 0) return 0;
					return numPawnsOnFile(board, player, file);
				})
				.sum();
	}

	private static double mobilityReward(Board board) {
		return board.getLegalMoves(Player.WHITE).stream().map(ImprovedShannonEvaluator::moveReward).reduce(0.0, Double::sum)
				- board.getLegalMoves(Player.BLACK).stream().map(ImprovedShannonEvaluator::moveReward).reduce(0.0, Double::sum);
	}

	private static double moveReward(PlayerMove move) {
		double total = 0.1;
		if (move instanceof Castle || move instanceof Promotion)
			total += 0.1;
		total += switch (move.piece().type()) {
			case PAWN, KING -> 0;
			case KNIGHT, BISHOP -> 0.2;
			case ROOK -> 0.3;
			case QUEEN -> 0.02;
		};
		return total;
	}

	private static double tempoReward(Board board) {
		return switch (board.currentTurn()) {
			case WHITE -> 0.05;
			case BLACK -> -0.05;
		};
	}
}
