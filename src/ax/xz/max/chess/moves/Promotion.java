package ax.xz.max.chess.moves;

import ax.xz.max.chess.*;

public record Promotion(
		Piece newPiece,
		Piece oldPiece,
		BoardCoordinate from,
		BoardCoordinate to
) implements PlayerMove {

	public static Promotion[] allPromotions(Piece piece, BoardCoordinate from, BoardCoordinate to) {
		return new Promotion[] {
				new Promotion(new Piece(piece.owner(), PieceType.QUEEN), piece, from, to),
				new Promotion(new Piece(piece.owner(), PieceType.ROOK), piece, from, to),
				new Promotion(new Piece(piece.owner(), PieceType.BISHOP), piece, from, to),
				new Promotion(new Piece(piece.owner(), PieceType.KNIGHT), piece, from, to)
		};
	}

	@Override
	public void execute(Board board) {
		board.placePiece(newPiece, to);
		board.removePiece(from);

		board.switchTurn();

		board.incrementHalfMoves();
		board.resetHalfMoves(); // promotions are ALWAYS pawn pushes
	}

	@Override
	public Piece piece() {
		return oldPiece;
	}

	@Override
	public BoardCoordinate from() {
		return from;
	}

	@Override
	public BoardCoordinate to() {
		return to;
	}

	@Override
	public Player getPlayer() {
		return oldPiece.owner();
	}

	public Piece getNewPiece() {
		return newPiece;
	}

	@Override
	public String toString() {
		return String.format("%s%s=%s", from, to, newPiece.toChar());
	}
}
