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
					+ endgameReward(board)
					- doubledPawnPenalty(board)
					- blockedPawnPenalty(board)
					- isolatedPawnPenalty(board);
		};

	}

	/** should be added to the evaluation */
	private static double materialReward(Board board) {
		boolean isEndGame = isEndgame(board);
		double whiteMaterial = 0;
		double blackMaterial = 0;
		for (var type : PieceType.values()) {
			var whitePiece = new Piece(Player.WHITE, type);
			for (var position : board.boardState().board().allOf(whitePiece)) {
				whiteMaterial += valueOf(position, whitePiece, isEndGame);
			}

			var blackPiece = new Piece(Player.BLACK, type);
			for (var position : board.boardState().board().allOf(blackPiece)) {
				blackMaterial += valueOf(position, blackPiece, isEndGame);
			}
		}

		double total = whiteMaterial + blackMaterial;
		double netMaterial = whiteMaterial - blackMaterial;

		double scaleFactor = 1 + Math.exp(-0.1 * total);
		return scaleFactor * netMaterial;
	}

	private static final List<List<Double>> whitePawnValue = List.of(
			List.of(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
			List.of(1.05, 1.10, 1.10, 0.80, 0.80, 1.10, 1.10, 1.05),
			List.of(1.05, 0.95, 0.90, 1.00, 1.00, 0.90, 0.95, 1.05),
			List.of(1.00, 1.00, 1.00, 1.20, 1.20, 1.00, 1.00, 1.00),
			List.of(1.05, 1.05, 1.10, 1.25, 1.25, 1.10, 1.05, 1.05),
			List.of(1.10, 1.10, 1.20, 1.30, 1.30, 1.20, 1.10, 1.10),
			List.of(1.50, 1.50, 1.50, 1.50, 1.50, 1.50, 1.50, 1.50),
			List.of(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00)
	);

	private static final List<List<Double>> blackPawnValue = whitePawnValue.reversed();

	private static final List<List<Double>> whiteKnightValue = List.of(
			List.of(2.70, 2.80, 2.90, 2.90, 2.90, 2.90, 2.80, 2.70),
			List.of(2.80, 3.00, 3.20, 3.25, 3.25, 3.20, 3.00, 2.80),
			List.of(2.90, 3.25, 3.30, 3.35, 3.35, 3.30, 3.25, 2.90),
			List.of(2.90, 3.20, 3.35, 3.40, 3.40, 3.35, 3.20, 2.90),
			List.of(2.90, 3.25, 3.35, 3.40, 3.40, 3.35, 3.25, 2.90),
			List.of(2.90, 3.20, 3.30, 3.35, 3.35, 3.30, 3.20, 2.90),
			List.of(2.80, 3.00, 3.20, 3.25, 3.25, 3.20, 3.00, 2.80),
			List.of(2.70, 2.80, 2.90, 2.90, 2.90, 2.90, 2.80, 2.70)
	);

	private static final List<List<Double>> blackKnightValue = whiteKnightValue.reversed();

	private static final List<List<Double>> whiteBishopValue = List.of(
			List.of(3.10, 2.80, 2.90, 2.90, 2.90, 2.90, 2.80, 3.10),
			List.of(3.20, 3.35, 3.30, 3.30, 3.30, 3.30, 3.35, 3.20),
			List.of(3.20, 3.40, 3.40, 3.40, 3.40, 3.40, 3.40, 3.20),
			List.of(3.20, 3.30, 3.40, 3.40, 3.40, 3.40, 3.30, 3.20),
			List.of(3.20, 3.35, 3.35, 3.40, 3.40, 3.35, 3.35, 3.20),
			List.of(3.20, 3.30, 3.35, 3.40, 3.40, 3.35, 3.30, 3.20),
			List.of(3.20, 3.30, 3.30, 3.30, 3.30, 3.30, 3.30, 3.20),
			List.of(3.10, 2.80, 2.90, 2.90, 2.90, 2.90, 2.80, 3.10)
	);

	private static final List<List<Double>> blackBishopValue = whiteBishopValue.reversed();

	private static final List<List<Double>> whiteRookValue = List.of(
			List.of(5.00, 5.00, 5.05, 5.10, 5.10, 5.05, 5.00, 5.00),
			List.of(4.95, 5.00, 5.00, 5.00, 5.00, 5.00, 5.00, 4.95),
			List.of(4.95, 5.00, 5.00, 5.00, 5.00, 5.00, 5.00, 4.95),
			List.of(4.95, 5.00, 5.00, 5.00, 5.00, 5.00, 5.00, 4.95),
			List.of(4.95, 5.00, 5.00, 5.00, 5.00, 5.00, 5.00, 4.95),
			List.of(4.95, 5.00, 5.00, 5.00, 5.00, 5.00, 5.00, 4.95),
			List.of(5.05, 5.10, 5.10, 5.10, 5.10, 5.10, 5.10, 5.05),
			List.of(5.00, 5.00, 5.00, 5.00, 5.00, 5.00, 5.00, 5.00)
	);

	private static final List<List<Double>> blackRookValue = whiteRookValue.reversed();

	private static final List<List<Double>> whiteQueenValue = List.of(
			List.of(8.80, 8.90, 8.90, 8.95, 8.95, 8.90, 8.90, 8.80),
			List.of(8.90, 9.00, 9.05, 9.00, 9.00, 9.00, 9.00, 8.90),
			List.of(8.90, 9.05, 9.05, 9.05, 9.05, 9.05, 9.00, 8.90),
			List.of(9.00, 9.05, 9.05, 9.05, 9.05, 9.05, 9.00, 8.95),
			List.of(8.95, 9.05, 9.05, 9.05, 9.05, 9.05, 9.00, 8.95),
			List.of(8.90, 9.00, 9.05, 9.05, 9.05, 9.05, 9.00, 8.90),
			List.of(8.90, 9.00, 9.00, 9.00, 9.00, 9.00, 9.00, 8.90),
			List.of(8.80, 8.90, 8.90, 8.95, 8.95, 8.90, 8.90, 8.80)
	);

	private static final List<List<Double>> blackQueenValue = whiteQueenValue.reversed();

	private static final List<List<Double>> whiteKingValue = List.of(
			List.of(1.20, 1.30, 1.10, 1.00, 1.00, 1.10, 1.30, 1.20),
			List.of(1.20, 1.20, 1.00, 1.00, 1.00, 1.00, 1.20, 1.20),
			List.of(0.90, 0.80, 0.80, 0.80, 0.80, 0.80, 0.80, 0.90),
			List.of(0.80, 0.70, 0.70, 0.60, 0.60, 0.70, 0.70, 0.80),
			List.of(0.70, 0.60, 0.60, 0.50, 0.50, 0.60, 0.60, 0.70),
			List.of(0.70, 0.60, 0.60, 0.50, 0.50, 0.60, 0.60, 0.70),
			List.of(0.70, 0.60, 0.60, 0.50, 0.50, 0.60, 0.60, 0.70),
			List.of(0.70, 0.60, 0.60, 0.50, 0.50, 0.60, 0.60, 0.70)
	);

	private static final List<List<Double>> blackKingValue = whiteKingValue.reversed();

	private static double valueOf(BoardCoordinate coordinate, Piece piece, boolean isEndGame) {
		return switch (piece.owner()) {
			case WHITE -> switch (piece.type()) {
				case PAWN -> whitePawnValue.get(coordinate.rank()).get(coordinate.file());
				case KNIGHT -> whiteKnightValue.get(coordinate.rank()).get(coordinate.file());
				case BISHOP -> whiteBishopValue.get(coordinate.rank()).get(coordinate.file());
				case ROOK -> whiteRookValue.get(coordinate.rank()).get(coordinate.file());
				case QUEEN -> whiteQueenValue.get(coordinate.rank()).get(coordinate.file());
				case KING -> isEndGame ? 0 : whiteKingValue.get(coordinate.rank()).get(coordinate.file());
			};

			case BLACK -> switch (piece.type()) {
				case PAWN -> blackPawnValue.get(coordinate.rank()).get(coordinate.file());
				case KNIGHT -> blackKnightValue.get(coordinate.rank()).get(coordinate.file());
				case BISHOP -> blackBishopValue.get(coordinate.rank()).get(coordinate.file());
				case ROOK -> blackRookValue.get(coordinate.rank()).get(coordinate.file());
				case QUEEN -> blackQueenValue.get(coordinate.rank()).get(coordinate.file());
				case KING -> isEndGame ? 0 : blackKingValue.get(coordinate.rank()).get(coordinate.file());
			};
		};
	}

	private static final long COLUMN_MASK = 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L;

	private static int[] numPawnsPerFile(Board board, Player player) {
		Piece pawn = new Piece(player, PieceType.PAWN);
		long bitboard = board.boardState().board().bitBoardFor(pawn);
		var result = new int[8];
		for (int i=0; i<8; i++) {
			long mask = COLUMN_MASK << i;
			result[i] = Long.bitCount(bitboard & mask);
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
			if (file > 0 && numPawnsPerFile[file - 1] != 0) continue;
			if (file < 7 && numPawnsPerFile[file + 1] != 0) continue;
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
		double total = 0.005;
		total += switch(move) {
			case Castle __ -> 0.005;
			case Promotion __ -> 0.02;
			default -> 0;
		};
		total += switch (move.piece().type()) {
			case KNIGHT, BISHOP -> 0.001;
			case ROOK -> 0.001;
			case QUEEN -> 0.001;
			default -> 0;
		};
		return total;
	}

	private static boolean isEndgame(Board board) {
		return board.boardState().numExpensivePieces() < 6;
	}

	private static double endgameReward(Board board) {
		if (!isEndgame(board)) return 0;

		double total = 0;

		Piece whitePawn = new Piece(Player.WHITE, PieceType.PAWN);
		for (var position : board.boardState().board().allOf(whitePawn))
			total += 0.15 * position.rank();

		Piece blackPawn = new Piece(Player.BLACK, PieceType.PAWN);
		for (var position : board.boardState().board().allOf(blackPawn))
			total -= 0.15 * (7 - position.rank());

		return total;
	}
}
