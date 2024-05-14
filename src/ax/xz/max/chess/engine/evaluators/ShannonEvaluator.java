package ax.xz.max.chess.engine.evaluators;

import ax.xz.max.chess.*;

/**
 * The basic chess position evaluation formulated by Claude Shannon in 1949:
 * <pre><code>
 * f(p) = 200(K-K')
 *        + 9(Q-Q')
 *        + 5(R-R')
 *        + 3(B-B' + N-N')
 *        + 1(P-P')
 *        - 0.5(D-D' + S-S' + I-I')
 *        + 0.1(M-M') + ...
 *
 * KQRBNP = number of kings, queens, rooks, bishops, knights and pawns
 * D,S,I = doubled, blocked and isolated pawns
 * M = Mobility (the number of legal moves)
 * </code></pre>
 */
public class ShannonEvaluator implements BoardEvaluator {
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
		double total = 0;
		for (BoardCoordinate coordinate : board.boardState().board().allPieces()) {
			total += valueOf(board.boardState().pieceAt(coordinate));
		}
		return total;
	}

	private static int valueOf(PieceType piece) {
		return switch (piece) {
			case PAWN -> 1;
			case KNIGHT, BISHOP -> 3;
			case ROOK -> 5;
			case QUEEN -> 9;
			case KING -> 200;
		};
	}

	private static int valueOf(Piece piece) {
		return valueOf(piece.type()) * switch (piece.owner()) {
			case WHITE -> 1;
			case BLACK -> -1;
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
		return 0.5 * count;
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
		return 0.5 * count;
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
		return 0.1 * (board.getLegalMoves(Player.WHITE).size() - board.getLegalMoves(Player.BLACK).size());
	}

	private static double tempoReward(Board board) {
		return switch (board.currentTurn()) {
			case WHITE -> 0.05;
			case BLACK -> -0.05;
		};
	}
}
