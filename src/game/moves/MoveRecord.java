package game.moves;

import game.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public record MoveRecord(
		Board prevBoard,
		PlayerMove move
) {
	public MoveRecord {
		prevBoard = prevBoard.copy();
	}

	@Override
	public Board prevBoard() {
		return prevBoard.copy();
	}

	public Board resultantBoard() {
		Board board = prevBoard();
		board.makeMove(move());
		return board;
	}

	public Player player() {
		return move.getPlayer();
	}

	public boolean positionEquals(MoveRecord that) {
		return this.resultantBoard().equals(that.resultantBoard());
	}

	public GameState resultantState() {
		return resultantBoard().getState();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MoveRecord that)) return false;
		return this.positionEquals(that);
	}

	@Override
	public int hashCode() {
		return resultantBoard().hashCode();
	}

	public boolean gameEnded() {
		return resultantState() != GameState.UNFINISHED;
	}

	@Override
	public String toString() {
		return null;
//		return switch (move) {
//			case RegularMove regularMove -> {
//				StringBuilder result = new StringBuilder();
//
//				if (!(regularMove.piece().type() == PieceType.PAWN)) {
//					result.append(regularMove.piece().type().toChar());
//					// check how many pieces are in the given file
//					int fileCount = (int) IntStream.range(0, 8)
//						.mapToObj(i -> board[i][regularMove.from().file()])
//						.filter(move.piece()::equals)
//						.count();
//					int rankCount = (int) Arrays.stream(board[regularMove.from().rank()])
//						.filter(move.piece()::equals)
//						.count();
//
//					if (fileCount > 1)
//						result.append(regularMove.from().toString().charAt(0)); // specify file
//					if (rankCount > 1)
//						result.append(regularMove.from().toString().charAt(1)); // specify rank
//				} else if (isCapture) result.append(regularMove.from().toString().charAt(0)); // specify file
//
//				if (isCapture) result.append("x");
//
//				result.append(regularMove.to().toString());
//
//				if (isMate) result.append("#");
//				else if (isCheck) result.append("+");
//
//				yield result.toString();
//			}
//
//			case Promotion promotion -> {
//				StringBuilder result = new StringBuilder();
//
//				if (isCapture()) {
//					result.append(promotion.from().toString().charAt(0)) // specify file
//							.append("x");
//				}
//
//				result.append(promotion.to().toString())
//						.append("=")
//						.append(promotion.getNewPiece().type().toChar());
//
//				if (isMate) result.append("#");
//				else if (isCheck) result.append("+");
//
//				yield result.toString();
//			}
//
//			default -> move.toString(); // we trust these other types to have correct toString methods
//		};
	}
}
