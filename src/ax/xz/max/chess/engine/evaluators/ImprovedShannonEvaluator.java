package ax.xz.max.chess.engine.evaluators;

import ax.xz.max.chess.*;
import ax.xz.max.chess.moves.Castle;
import ax.xz.max.chess.moves.PlayerMove;
import ax.xz.max.chess.moves.Promotion;

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
		double total = 0;
		for (BoardCoordinate coordinate : board.boardState().board().allPieces()) {
			total += valueOf(board.pieceAt(coordinate));
		}
		return total;
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
