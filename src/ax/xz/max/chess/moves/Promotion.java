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
	public BoardState apply(BoardState board) {
		return board
				.placePiece(newPiece, to)
				.removePiece(from)
				.prepareNextMove(true);
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

	@Override
	public String toString() {
		return String.format("%s%s=%s", from, to, newPiece.toChar());
	}

	@Override
	public String toUCI() {
		return String.format("%s%s%c", from, to, Character.toLowerCase(newPiece.type().toChar()));
	}
}
